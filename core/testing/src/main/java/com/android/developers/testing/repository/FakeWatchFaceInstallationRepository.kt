package com.android.developers.testing.repository

import android.graphics.Bitmap
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.watchface.transfer.WatchFaceInstallationRepository
import com.android.developers.androidify.wear.common.ConnectedWatch
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FakeWatchFaceInstallationRepository : WatchFaceInstallationRepository {
    private val watch = ConnectedWatch(
        nodeId = "1234",
        displayName = "Pixel Watch",
        hasAndroidify = true,
    )

    private val watchFaceAsset = WatchFaceAsset(
        id = "watch_face_1",
        previewPath = com.android.developers.androidify.results.R.drawable.watch_face_preview,
    )

    private var transferId = generateTransferId()

    private val _connectedWatch = MutableStateFlow<ConnectedWatch?>(null)
    override val connectedWatch = _connectedWatch.asStateFlow()

    private val _watchFaceInstallationStatus =
        MutableStateFlow<WatchFaceInstallationStatus>(WatchFaceInstallationStatus.NotStarted)
    override val watchFaceInstallationUpdates = _watchFaceInstallationStatus.asStateFlow()

    override suspend fun createAndTransferWatchFace(
        connectedWatch: ConnectedWatch,
        watchFace: WatchFaceAsset,
        bitmap: Bitmap,
    ): WatchFaceInstallError {
        transferId = generateTransferId()
        delay(5_000)
        _watchFaceInstallationStatus.value = WatchFaceInstallationStatus.Complete(
            success = true,
            otherNodeId = "5678",
            transferId = transferId,
            activationStrategy = WatchFaceActivationStrategy.NO_ACTION_NEEDED,
            validationToken = "1234abcd",
            installError = WatchFaceInstallError.NO_ERROR,
        )
        return WatchFaceInstallError.NO_ERROR
    }

    override suspend fun getAvailableWatchFaces(): Result<List<WatchFaceAsset>> {
        return Result.success(listOf(watchFaceAsset))
    }

    override suspend fun resetInstallationStatus() {
        transferId = generateTransferId()
        _watchFaceInstallationStatus.value = WatchFaceInstallationStatus.NotStarted
    }

    private fun generateTransferId() = UUID.randomUUID().toString().take(8)

    public fun setWatchAsConnected() {
        _connectedWatch.value = watch
    }
}