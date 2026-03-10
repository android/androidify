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

import android.graphics.Bitmap
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.wear.common.ConnectedWatch
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

// The minimum supported version of Android for a watch face installation support. This is currently
const val MIN_WATCH_FACE_SDK_VERSION = 28

class EmptyWatchFaceInstallationRepositoryImpl @Inject constructor() : WatchFaceInstallationRepository {
    override val connectedWatch = flowOf<ConnectedWatch?>(null)

    override val watchFaceInstallationUpdates = flowOf<WatchFaceInstallationStatus>(
        WatchFaceInstallationStatus.NotStarted,
    )

    override suspend fun createAndTransferWatchFace(
        connectedWatch: ConnectedWatch,
        watchFace: WatchFaceAsset,
        bitmap: Bitmap,
    ): WatchFaceInstallError {
        return WatchFaceInstallError.WATCH_FACE_INSTALL_ERROR
    }

    override suspend fun getAvailableWatchFaces(): Result<List<WatchFaceAsset>> {
        return Result.success(emptyList())
    }

    override suspend fun resetInstallationStatus() { }

    override suspend fun prepareForTransfer() { }

    override suspend fun installAndroidify(nodeId: String) { }
}
