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
package com.android.developers.androidify.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.window.WindowSdkExtensions
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.FoldingFeature
import androidx.window.layout.SupportedPosture
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import androidx.window.layout.adapter.computeWindowSizeClass
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

@Composable
fun calculateWindowSizeClass(): WindowSizeClass {
    val currentWindowMetrics =
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(LocalContext.current)
    return WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(currentWindowMetrics)
}

@Composable
fun isWidthAtLeastMedium(): Boolean {
    val sizeClass = calculateWindowSizeClass()
    return sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
}

@Composable
fun areBothWindowDimensionsAtLeastMedium(): Boolean {
    val sizeClass = calculateWindowSizeClass()
    return sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) &&
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
}

/***
 * This function is useful to limit the number of buttons when the window is too small to show
 * everything that should otherwise appear on the screen.
 */
@Composable
fun allowsFullContent(): Boolean {
    val context = LocalContext.current
    val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
    val isSmallHeight = windowMetrics.heightDp < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    return !isSmallHeight
}

@OptIn(ExperimentalContracts::class)
fun isTableTopPosture(foldFeature: FoldingFeature?): Boolean {
    contract { returns(true) implies (foldFeature != null) }
    return foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
        foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
}

@SuppressLint("RequiresWindowSdk")
@Composable
fun supportsTabletop(): Boolean {
    return if (WindowSdkExtensions.getInstance().extensionVersion >= 6) {
        val postures = WindowInfoTracker.getOrCreate(LocalContext.current).supportedPostures
        postures.contains(SupportedPosture.TABLETOP)
    } else {
        false
    }
}

/**
 * This function is a wrapper to explain the logic needed for the following issue:
 * older devices that do not support Window SDK Extensions greater or equal than 6, might return
 * false for tabletop support, as they cannot list the available postures yet, but they can still
 * be able to work on tabletop mode. We need to react to these devices and show the appropriate
 * layout.
 *
 * Of course, this can be verified only when they are in tabletop mode, as the decision tree is the
 * following:
 * - if it supports tabletop but it's not in tabletop mode, the layout is divided at the hinge, with
 * the preview on the left and controls on the right
 * - if the device is actually in tabletop mode, the layout is once again divided at the hinge, but
 * this time the preview will be on top, and the controls at the bottom
 *
 * Given the explanation, some devices that are able to use tabletop, will not have the first layout
 * as long as they report they cannot support tabletop
 */
fun shouldShowTabletopLayout(supportsTabletop: Boolean, isTabletop: Boolean) =
    supportsTabletop || isTabletop

/**
 * This function is used to calculate the correct aspect ratio for the camera preview.
 * The tolerance has been manually set to a value that could exclude the majority of devices, but
 * still include the older generation of foldables (like Samsung Fold and Fold 2) which aspect ratio
 * would not let the guide render correctly
 */
fun calculateCorrectAspectRatio(height: Int, width: Int, defaultAspectRatio: Float): Float {
    val newAspectRatio = width.toFloat() / height.toFloat()
    val tolerance = .13f
    val distance = abs(newAspectRatio - defaultAspectRatio)

    return if (distance < tolerance) {
        defaultAspectRatio
    } else {
        newAspectRatio
    }
}

data class FoldablePreviewParameters(val supportsTabletop: Boolean, val isTabletop: Boolean)
class FoldablePreviewParametersProvider : PreviewParameterProvider<FoldablePreviewParameters> {
    override val values: Sequence<FoldablePreviewParameters> = sequenceOf(
        FoldablePreviewParameters(supportsTabletop = true, isTabletop = true),
        FoldablePreviewParameters(supportsTabletop = true, isTabletop = false),
        FoldablePreviewParameters(supportsTabletop = false, isTabletop = false),

    )
}
