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
@file:OptIn(ExperimentalSerializationApi::class)

package com.android.developers.androidify.watchface.transfer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.developers.androidify.watchface.creator.WatchFacePackage
import com.android.developers.androidify.wear.common.InitialRequest
import com.android.developers.androidify.wear.common.InitialResponse
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.android.developers.androidify.wear.common.WearableConstants
import com.android.developers.androidify.wear.common.WearableConstants.SETUP_TIMEOUT_MS
import com.android.developers.androidify.wear.common.WearableConstants.TRANSFER_TIMEOUT_MS
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface WearAssetTransmitter {
    val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus>

    suspend fun doTransfer(
        nodeId: String,
        watchFacePackage: WatchFacePackage,
    ): WatchFaceInstallError

    fun resetTransferId()
}

@Singleton
class WearAssetTransmitterImpl @Inject constructor(
    @ApplicationContext val context: Context,
) : WearAssetTransmitter {
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }

    private var transferId = generateTransferId()

    /** Sends the watch face to the watch. The approach taken is as follows:
     *
     * 1. Setup        - the phone sends a message to the watch asking for the go-ahead to send the
     *                   watch face. The watch only accepts one ongoing watch face transmission at a
     *                   time. As part of the setup, the phone sends a unique ID which is used in
     *                   the subsequent exchanges, as well as the validation token.
     * 2. Transfer     - the phone sends the watch face
     * 3. Confirmation - The watch sends a confirmation message back to the phone, indicating
     *                   success as well as the activation strategy to use. If there was an error
     *                   then the details of the error are sent.
     */
    override val watchFaceInstallationUpdates = callbackFlow {
        trySend(WatchFaceInstallationStatus.NotStarted)

        var listener: MessageClient.OnMessageReceivedListener? = null

        /**
         * Some devices don't have access to Wearable API via Play Services, so it is necessary to
         * check for this scenario first before trying to use the API.
         */
        val apiAvailable = WearableApiAvailability.isAvailable(messageClient)
        if (apiAvailable) {
            listener = MessageClient.OnMessageReceivedListener { event ->
                if (event.path.contains(transferId)) {
                    val response =
                        ProtoBuf.decodeFromByteArray<WatchFaceInstallationStatus.Complete>(event.data)
                    if (response.transferId == transferId) {
                        trySend(response)
                    }
                }
            }
            messageClient.addListener(listener).await()
        }
        awaitClose {
            listener?.let {
                messageClient.removeListener(it).addOnSuccessListener { }
            }
        }
    }

    override suspend fun doTransfer(
        nodeId: String,
        watchFacePackage: WatchFacePackage,
    ): WatchFaceInstallError {
        var readyToReceive = false
        val maybeTransferId = generateTransferId()
        try {
            readyToReceive =
                assetTransferSetup(nodeId, maybeTransferId, watchFacePackage.validationToken)
        } catch (e: TimeoutCancellationException) {
            return WatchFaceInstallError.SEND_SETUP_TIMEOUT
        } catch (e: Exception) {
            Log.e(TAG, "Error sending request.", e)
            return WatchFaceInstallError.SEND_SETUP_REQUEST_ERROR
        }

        if (!readyToReceive) {
            return WatchFaceInstallError.WATCH_NOT_READY
        }
        transferId = maybeTransferId

        try {
            assetTransfer(nodeId, watchFacePackage.file)
        } catch (e: TimeoutCancellationException) {
            return WatchFaceInstallError.TRANSFER_TIMEOUT
        } catch (e: Exception) {
            Log.e(TAG, "Error transferring watch face", e)
            return WatchFaceInstallError.TRANSFER_ERROR
        } finally {
            watchFacePackage.file.delete()
        }
        return WatchFaceInstallError.NO_ERROR
    }

    override fun resetTransferId() {
        transferId = generateTransferId()
    }

    private suspend fun assetTransferSetup(
        nodeId: String,
        transferId: String,
        token: String,
    ): Boolean {
        val initialRequest = InitialRequest(transferId = transferId, token = token)
        val requestBytes = ProtoBuf.encodeToByteArray(initialRequest)

        val response = withContext(Dispatchers.IO) {
            withTimeout(SETUP_TIMEOUT_MS) {
                val responseBytes = messageClient.sendRequest(
                    nodeId,
                    WearableConstants.ANDROIDIFY_INITIATE_TRANSFER_PATH,
                    requestBytes,
                ).await()
                ProtoBuf.decodeFromByteArray<InitialResponse>(responseBytes)
            }
        }
        return response.proceed
    }

    private suspend fun assetTransfer(nodeId: String, file: File) {
        withContext(Dispatchers.IO) {
            withTimeout(TRANSFER_TIMEOUT_MS) {
                val channelPath =
                    WearableConstants.ANDROIDIFY_TRANSFER_PATH_TEMPLATE.format(transferId)
                val channel = channelClient.openChannel(nodeId, channelPath).await()
                channelClient.sendFile(channel, Uri.fromFile(file)).await()
            }
        }
    }

    private fun generateTransferId() = UUID.randomUUID().toString().take(8)

    companion object {
        private val TAG = WearAssetTransmitterImpl::class.java.simpleName
    }
}
