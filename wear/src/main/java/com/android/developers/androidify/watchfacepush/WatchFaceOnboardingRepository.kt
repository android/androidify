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

package com.android.developers.androidify.watchfacepush

import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.wear.watchfacepush.WatchFacePushManager
import androidx.wear.watchfacepush.WatchFacePushManagerFactory
import com.android.developers.androidify.data.WatchFacePushStateManager
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.android.developers.androidify.wear.common.WearableConstants
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

const val LAUNCHED_FROM_WATCH_FACE_TRANSFER = "launchedFromWatchFaceTransfer"

class WatchFaceOnboardingRepository(
    val context: Context,
    val watchFacePushStateManager: WatchFacePushStateManager = WatchFacePushStateManager(context),
) {
    private val messageClient by lazy { Wearable.getMessageClient(context) }

    /**
     * Determines the activation strategy of the watch face. This means what action the user and/or
     * system will have to take in order to set the watch face as active once it has been installed.
     *
     * For example, the app may already own the active watch face, in which case, no action is
     * required. At the other end of the spectrum, another app may own the active watch face, and
     * this app may have exhausted the API to request the active watch face. In this case, the
     * strategy would be manually setting the watch face.
     *
     * Full range of options shown in [WatchFaceActivationStrategy].
     */
    suspend fun getWatchFaceActivationStrategy(): WatchFaceActivationStrategy {
        val apiUsed = watchFacePushStateManager.activeWatchFaceApiUsed.first()
        val hasActiveWatchFace = hasActiveWatchFace()
        val hasPermission = hasSetWatchFacePermission()
        val canRequestPermission = !watchFacePushStateManager.watchFacePermissionDenied.first()

        return WatchFaceActivationStrategy.fromWatchFaceState(
            hasActiveWatchFace = hasActiveWatchFace,
            hasGrantedSetActivePermission = hasPermission,
            canRequestSetActivePermission = canRequestPermission,
            hasUsedSetActiveApi = apiUsed,
        )
    }
    val watchFaceTransferState = watchFacePushStateManager.watchFaceInstallationStatus

    suspend fun setWatchFaceTransferState(state: WatchFaceInstallationStatus) {
        watchFacePushStateManager.setWatchFaceInstallationStatus(state)
    }

    suspend fun resetWatchFaceTransferState() {
        watchFacePushStateManager.setWatchFaceInstallationStatus(WatchFaceInstallationStatus.NotStarted)
    }

    suspend fun resetWatchFaceTransferStateIfComplete() {
        val currentStatus = watchFacePushStateManager.watchFaceInstallationStatus.first()
        if (currentStatus is WatchFaceInstallationStatus.Complete) {
            watchFacePushStateManager.setWatchFaceInstallationStatus(WatchFaceInstallationStatus.NotStarted)
        }
    }

    suspend fun updateOrInstallWatchFace(apkFd: ParcelFileDescriptor, token: String): WatchFaceInstallError {
        val wfpManager = WatchFacePushManagerFactory.createWatchFacePushManager(context)
        val response = wfpManager.listWatchFaces()

        try {
            if (response.remainingSlotCount > 0) {
                wfpManager.addWatchFace(apkFd, token)
            } else {
                val slotId = response.installedWatchFaceDetails.first().slotId
                wfpManager.updateWatchFace(slotId, apkFd, token)
            }
        } catch (a: WatchFacePushManager.AddWatchFaceException) {
            return WatchFaceInstallError.WATCH_FACE_INSTALL_ERROR
        } catch (u: WatchFacePushManager.UpdateWatchFaceException) {
            return WatchFaceInstallError.WATCH_FACE_INSTALL_ERROR
        }
        return WatchFaceInstallError.NO_ERROR
    }

    suspend fun setActiveWatchFace() {
        val wfpManager = WatchFacePushManagerFactory.createWatchFacePushManager(context)
        val watchFacePushStateManager = WatchFacePushStateManager(context)
        val response = wfpManager.listWatchFaces()

        if (response.installedWatchFaceDetails.isEmpty()) {
            Log.w(TAG, "No watch face to set as active.")
            return
        }

        val slotId = response.installedWatchFaceDetails.first().slotId
        wfpManager.setWatchFaceAsActive(slotId)
        // Record that the one-shot API has been used
        watchFacePushStateManager.setActiveWatchFaceApiUsedKey(true)
    }

    /**
     * If permission has been denied to the SET_PUSHED_WATCH_FACE_AS_ACTIVE permission, then this is
     * stored, as this permission can only be requested and denied once. Keeping track of this helps
     * determine what action the user needs to take to set the active watch face.
     */
    suspend fun updatePermissionStatus(granted: Boolean) {
        val watchFacePushStateManager = WatchFacePushStateManager(context)
        watchFacePushStateManager.setWatchFacePermissionDenied(!granted)
    }

    private suspend fun hasActiveWatchFace(): Boolean {
        val wfpManager = WatchFacePushManagerFactory.createWatchFacePushManager(context)

        val response = wfpManager.listWatchFaces()
        return response.installedWatchFaceDetails.any {
            wfpManager.isWatchFaceActive(it.packageName)
        }
    }

    fun hasSetWatchFacePermission(): Boolean {
        val permission = "com.google.wear.permission.SET_PUSHED_WATCH_FACE_AS_ACTIVE"
        val permissionStatus = ContextCompat.checkSelfPermission(context, permission)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Sends a status update to the phone. This is used where a further "completion" message needs
     * to be sent to the phone after the installation has completed. For example, if once the watch
     * face has completed install, the user needs to grant permission in order to set it as active,
     * then that message is what is first sent to the phone.
     *
     * Once the user has then granted the permission, this method is used to send a further update
     * to the phone with the updated status.
     */
    suspend fun sendInstallUpdate(state: WatchFaceInstallationStatus.Complete) {
        val byteArray = ProtoBuf.encodeToByteArray(state)
        val path = WearableConstants.ANDROIDIFY_FINALIZE_TRANSFER_TEMPLATE.format(state.transferId)
        messageClient.sendMessage(state.validationToken, path, byteArray).await()
    }

    companion object {
        private val TAG = WatchFaceOnboardingRepository::class.java.simpleName
    }
}
