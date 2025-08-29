package com.android.developers.androidify.customize

import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus

data class WatchFaceSelectionState(
    val watchFaces: List<WatchFaceAsset> = emptyList(),
    val selectedWatchFace: WatchFaceAsset? = null,
    val isLoadingWatchFaces: Boolean = true,
)