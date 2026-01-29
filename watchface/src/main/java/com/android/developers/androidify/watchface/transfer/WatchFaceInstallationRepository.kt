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
package com.android.developers.androidify.watchface.transfer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.concurrent.futures.await
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.watchface.creator.WatchFaceCreator
import com.android.developers.androidify.wear.common.ConnectedWatch
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for watch face installation.
 */
interface WatchFaceInstallationRepository {
    /**
     * Flow of currently connected device. Only one device is reported - the scenario of having
     * multiple devices connected is not currently supported.
     */
    val connectedWatch: Flow<ConnectedWatch?>

    /**
     * Flow of status updates from the watch as installation proceeds.
     */
    val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus>

    /**
     * Creates and transmits a watch face to the connected device. The bitmap is added into the
     * template watch face.
     *
     * @param connectedWatch The device to install the watch face on.
     * @param bitmap The bitmap to add to the watch face.
     * @return The result of the transfer.
     */
    suspend fun createAndTransferWatchFace(
        connectedWatch: ConnectedWatch,
        watchFace: WatchFaceAsset,
        bitmap: Bitmap,
    ): WatchFaceInstallError

    /**
     * Retrieves a list of available watch faces.
     *
     * @return A result containing a list of watch face assets.
     */
    suspend fun getAvailableWatchFaces(): Result<List<WatchFaceAsset>>

    suspend fun resetInstallationStatus()

    suspend fun prepareForTransfer()

    suspend fun installAndroidify(nodeId: String)
}

class WatchFaceInstallationRepositoryImpl @Inject constructor(
    private val wearAssetTransmitter: WearAssetTransmitter,
    private val wearDeviceRepository: WearDeviceRepository,
    private val watchFaceCreator: WatchFaceCreator,
    @ApplicationContext val context: Context,
) : WatchFaceInstallationRepository {
    override val connectedWatch = wearDeviceRepository.connectedWatch

    private val manualStatusUpdates = MutableSharedFlow<WatchFaceInstallationStatus>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus> = merge(
        manualStatusUpdates,
        wearAssetTransmitter.watchFaceInstallationUpdates,
    )

    override suspend fun createAndTransferWatchFace(
        connectedWatch: ConnectedWatch,
        watchFace: WatchFaceAsset,
        bitmap: Bitmap,
    ): WatchFaceInstallError {
        return withContext(Dispatchers.IO) {
            manualStatusUpdates.tryEmit(WatchFaceInstallationStatus.Sending)
            val watchFacePackage = watchFaceCreator
                .createWatchFacePackage(watchFaceName = watchFace.id, botBitmap = bitmap)

            wearAssetTransmitter.doTransfer(connectedWatch.nodeId, watchFacePackage)
        }
    }

    override suspend fun getAvailableWatchFaces(): Result<List<WatchFaceAsset>> {
        return withContext(Dispatchers.IO) { // Move asset scanning to a background thread
            try {
                val assetManager = context.assets
                val rootFolders = assetManager.list("") ?: emptyArray()

                val watchFaceList = rootFolders.filter { folderName ->
                    (assetManager.list(folderName)?.contains("res") == true)
                }.map { watchFaceId ->
                    WatchFaceAsset(
                        id = watchFaceId,
                        previewPath = "file:///android_asset/$watchFaceId/res/drawable/preview.png",
                    )
                }
                Result.success(watchFaceList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun resetInstallationStatus() {
        wearAssetTransmitter.resetTransferId()
        manualStatusUpdates.tryEmit(WatchFaceInstallationStatus.NotStarted)
    }

    override suspend fun prepareForTransfer() {
        manualStatusUpdates.tryEmit(WatchFaceInstallationStatus.Preparing)
    }

    override suspend fun installAndroidify(nodeId: String) {
        val backgroundExecutor = Dispatchers.IO.asExecutor()
        val remoteActivityHelper = RemoteActivityHelper(context, backgroundExecutor)

        remoteActivityHelper.startRemoteActivity(
            Intent(Intent.ACTION_VIEW)
                .setData(
                    Uri.parse("market://details?id=${context.packageName}"),
                )
                .addCategory(Intent.CATEGORY_BROWSABLE),
            nodeId,
        ).await()
    }
}
