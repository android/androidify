/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalAtomicApi::class)

package com.android.developers.androidify.service

import android.annotation.SuppressLint
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import androidx.core.net.toUri
import com.android.developers.androidify.MainActivity
import com.android.developers.androidify.data.WatchFacePushStateManager
import com.android.developers.androidify.watchfacepush.LAUNCHED_FROM_WATCH_FACE_TRANSFER
import com.android.developers.androidify.watchfacepush.WatchFaceOnboardingRepository
import com.android.developers.androidify.wear.common.InitialRequest
import com.android.developers.androidify.wear.common.InitialResponse
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.android.developers.androidify.wear.common.WearableConstants
import com.android.developers.androidify.wear.common.WearableConstants.TRANSFER_TIMEOUT_MS
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.resume

/**
 * Receives incoming connections from the phone, used for transferring watch faces.
 */
@OptIn(ExperimentalSerializationApi::class)
class AndroidifyDataListenerService : WearableListenerService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val channelClient by lazy { Wearable.getChannelClient(this) }

    private var transferTimeoutJob: Job? = null
    private var receiverJob: Job? = null

    private val watchFacePushStateManager by lazy { WatchFacePushStateManager(this) }
    private val watchFaceOnboardingRepository by lazy { WatchFaceOnboardingRepository(this) }

    /**
     * The initial request to start a transfer is sent via message client from the phone to the
     * watch. The watch responds to the phone to either confirm or deny the transfer.
     *
     * The watch will only accept one transfer at a time. Each transfer is started with a unique
     * transfer ID which is passed in the [InitialRequest].
     */
    override fun onRequest(nodeId: String, path: String, data: ByteArray): Task<ByteArray?>? {
        super.onRequest(nodeId, path, data)
        if (path == WearableConstants.ANDROIDIFY_INITIATE_TRANSFER_PATH) {
            return doTransferReceiveSetup(nodeId, data)
        }
        return Tasks.forResult(null)
    }

    /**
     * Receives the watch face payload and initiates installation.
     */
    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        receiverJob = serviceScope.launch {
            // Check that the watch is expecting a watch face transfer and that the transfer ID of
            // the incoming APK matches that which was sent in the initial request.
            val transferState = watchFacePushStateManager.watchFaceInstallationStatus.first()
            if (transferState !is WatchFaceInstallationStatus.Receiving ||
                !channel.path.contains(transferState.transferId)
            ) {
                return@launch
            }
            // This job was set after the initial request in case the phone never followed up with
            // the APK payload, so it's ok to now cancel that.
            transferTimeoutJob?.cancel()
            transferTimeoutJob = null

            val result = withTimeoutOrNull(TRANSFER_TIMEOUT_MS) {
                suspendCancellableCoroutine<WatchFaceInstallError> { continuation ->
                    val resultChannel = Channel<WatchFaceInstallError>(Channel.CONFLATED)

                    val tempFile = File.createTempFile("temp", ".apk")
                    tempFile.deleteOnExit()

                    // The [onInputClosed] callback method is called when a file has successfully
                    // been received by the device, or the channel closed for another reason.
                    val callback = object : ChannelClient.ChannelCallback() {
                        override fun onInputClosed(
                            channel: ChannelClient.Channel,
                            closeReason: Int,
                            appErrorCode: Int,
                        ) {
                            super.onInputClosed(channel, closeReason, appErrorCode)
                            val transferResult = if (closeReason == CLOSE_REASON_NORMAL) {
                                WatchFaceInstallError.NO_ERROR
                            } else {
                                WatchFaceInstallError.TRANSFER_ERROR
                            }
                            resultChannel.trySend(transferResult)
                        }
                    }

                    continuation.invokeOnCancellation {
                        isTransferInProgress.store(false)
                        channelClient.unregisterChannelCallback(callback)
                    }

                    val continuationScope = CoroutineScope(continuation.context)
                    continuationScope.launch {
                        try {
                            channelClient.registerChannelCallback(channel, callback).await()
                            channelClient.receiveFile(channel, tempFile.toUri(), false)

                            val finalResult = resultChannel.receive()

                            if (finalResult == WatchFaceInstallError.NO_ERROR) {
                                installAndSetWatchFace(
                                    tempFile,
                                    transferState.validationToken,
                                    transferState.activationStrategy,
                                )
                            }

                            if (continuation.isActive) {
                                continuation.resume(finalResult)
                            }
                        } catch (e: Exception) {
                            if (continuation.isActive) {
                                continuation.resume(WatchFaceInstallError.TRANSFER_ERROR)
                            }
                        } finally {
                            channelClient.unregisterChannelCallback(callback)
                            resultChannel.close()
                        }
                    }
                }
            } ?: WatchFaceInstallError.TRANSFER_TIMEOUT

            val completedStatus = WatchFaceInstallationStatus.Complete(
                success = result == WatchFaceInstallError.NO_ERROR,
                transferId = transferState.transferId,
                validationToken = transferState.validationToken,
                activationStrategy = transferState.activationStrategy,
                installError = result,
                otherNodeId = channel.nodeId,
            )
            // Update the local status of the transfer, which is then reflected on the watch UI.
            watchFaceOnboardingRepository.watchFacePushStateManager
                .setWatchFaceInstallationStatus(completedStatus)
            sendInstallResponse(channel.nodeId, completedStatus)
            isTransferInProgress.store(false)
        }
    }

    private fun doTransferReceiveSetup(nodeId: String, data: ByteArray): Task<ByteArray?>? {
        val initialRequest = ProtoBuf.decodeFromByteArray<InitialRequest>(data)
        // An atomic boolean is used to ensure only one transfer is in progress.
        val canProceed = isTransferInProgress.compareAndSet(false, true)

        val response = runBlocking {
            if (canProceed) {
                launchWatchFaceGuidance()

                // The activation strategy is determined *before* the watch is transferred and
                // installed, because installing / changing watch faces can lead to temporary
                // inaccuracy in the Watch Face Push API reporting whether the app has the
                // active watch face or not. So this is determined ahead of time and stored.
                val strategy = watchFaceOnboardingRepository.getWatchFaceActivationStrategy()

                watchFacePushStateManager.setWatchFaceInstallationStatus(
                    WatchFaceInstallationStatus.Receiving(
                        activationStrategy = strategy,
                        transferId = initialRequest.transferId,
                        validationToken = initialRequest.token,
                        otherNodeId = nodeId,
                    ),
                )

                // A timeout job is started in case, having initiated the transfer, the phone
                // does not follow up by actually sending the watch face APK payload.
                transferTimeoutJob = serviceScope.launch {
                    configureTransferTimeout(initialRequest, nodeId, strategy)
                }
            }
            InitialResponse(proceed = canProceed)
        }
        return Tasks.forResult(ProtoBuf.encodeToByteArray(response))
    }

    private suspend fun configureTransferTimeout(initialRequest: InitialRequest, nodeId: String, strategy: WatchFaceActivationStrategy) {
        delay(TRANSFER_TIMEOUT_MS)
        val transferState = watchFacePushStateManager.watchFaceInstallationStatus.first()
        if (transferState is WatchFaceInstallationStatus.Receiving &&
            transferState.transferId == initialRequest.transferId) {
            watchFacePushStateManager.setWatchFaceInstallationStatus(
                WatchFaceInstallationStatus.Complete(
                    success = false,
                    installError = WatchFaceInstallError.TRANSFER_TIMEOUT,
                    activationStrategy = strategy,
                    transferId = initialRequest.transferId,
                    validationToken = initialRequest.token,
                    otherNodeId = nodeId,
                ),
            )
            isTransferInProgress.store(false)
        }
    }

    private suspend fun sendInstallResponse(nodeId: String, state: WatchFaceInstallationStatus.Complete) {
        val byteArray = ProtoBuf.encodeToByteArray(state)
        val path = WearableConstants.ANDROIDIFY_FINALIZE_TRANSFER_TEMPLATE.format(state.transferId)
        messageClient.sendMessage(nodeId, path, byteArray).await()
    }

    /**
     * Installs the watch face. If it is possible to set it as active watch face with no further
     * user interaction, then this is also done.
     */
    private suspend fun installAndSetWatchFace(apkFile: File, token: String, strategy: WatchFaceActivationStrategy): WatchFaceInstallError {
        return FileInputStream(apkFile).use { stream ->
            val installResult = watchFaceOnboardingRepository.updateOrInstallWatchFace(ParcelFileDescriptor.dup(stream.fd), token)
            if (installResult == WatchFaceInstallError.NO_ERROR) {
                if (strategy == WatchFaceActivationStrategy.CALL_SET_ACTIVE_NO_USER_ACTION) {
                    watchFaceOnboardingRepository.setActiveWatchFace()
                }
            }
            installResult
        }
    }

    @SuppressLint("WearRecents")
    private fun launchWatchFaceGuidance() {
        wakeDevice()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(LAUNCHED_FROM_WATCH_FACE_TRANSFER, true)
        startActivity(intent)
    }

    /**
     * Wakes the device. This is important to do when a transfer is incoming as otherwise the UI
     * will not necessarily show to the user.
     */
    private fun wakeDevice() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // FULL_WAKE_LOCK and ACQUIRE_CAUSES_WAKEUP are deprecated, but they remain in use as the
        // approach for achieving screen wakeup across mainstream apps, so are the approach to use
        // for now.
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE,
            WAKELOCK_TAG,
        )

        // Wakelock timeout should not be required as it is being immediately released but
        // linting guidance recommends one so setting it nonetheless.
        wakeLock.acquire(WAKELOCK_TIMEOUT_MS)
        wakeLock.release()
    }

    companion object {
        private val TAG = AndroidifyDataListenerService::class.java.simpleName
        private val isTransferInProgress = AtomicBoolean(false)
        private const val WAKELOCK_TAG = "androidify:wear"
        private const val WAKELOCK_TIMEOUT_MS = 1000L
    }
}
