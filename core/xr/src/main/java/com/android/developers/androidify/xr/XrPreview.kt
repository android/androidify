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
package com.android.developers.androidify.xr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7_PRO
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.platform.SpatialCapabilities
import androidx.xr.compose.platform.SpatialConfiguration

/**
 * Preview for a layout that could go into Full Space Mode.
 */
@Composable
fun SupportsFullSpaceModeRequestProvider(contents: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpatialConfiguration provides HasSpatialFeatureSpatialConfiguration) {
        CompositionLocalProvider(LocalSpatialCapabilities provides SupportsFullSpaceModeRequestCapabilities) {
            CompositionLocalProvider(LocalSession provides null) {
                contents()
            }
        }
    }
}

/**
 * Workaround for b/441901724.
 * Any composable referencing LocalSpatialConfiguration or LocalSpatialCapabilities will
 * fail to preview instead of gracefully degrading due to failing to resolve XR capabilities.
 *
 * This can be removed when the default for XR capabilities under the preview is no capabilities
 * instead of throwing an exception.
 * */
@Composable
fun NoXrSupportPreview(contents: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpatialConfiguration provides LacksSpatialFeatureSpatialConfiguration) {
        CompositionLocalProvider(LocalSpatialCapabilities provides NoSpatialCapabilities) {
            CompositionLocalProvider(LocalSession provides null) {
                contents()
            }
        }
    }
}

private object SupportsFullSpaceModeRequestCapabilities : SpatialCapabilities {
    override val isSpatialUiEnabled: Boolean
        get() = false
    override val isContent3dEnabled: Boolean
        get() = true
    override val isAppEnvironmentEnabled: Boolean
        get() = true
    override val isPassthroughControlEnabled: Boolean
        get() = true
    override val isSpatialAudioEnabled: Boolean
        get() = true
}

private object NoSpatialCapabilities : SpatialCapabilities {
    override val isSpatialUiEnabled: Boolean
        get() = false
    override val isContent3dEnabled: Boolean
        get() = false
    override val isAppEnvironmentEnabled: Boolean
        get() = false
    override val isPassthroughControlEnabled: Boolean
        get() = false
    override val isSpatialAudioEnabled: Boolean
        get() = false
}

private object HasSpatialFeatureSpatialConfiguration : SpatialConfiguration {
    override val hasXrSpatialFeature: Boolean
        get() = true
}

private object LacksSpatialFeatureSpatialConfiguration : SpatialConfiguration {
    override val hasXrSpatialFeature: Boolean
        get() = false
}

@Preview(device = PIXEL_TABLET, name = "Android XR (Home Space Mode)")
annotation class XrHomeSpaceMediumPreview

@Preview(device = PIXEL_7_PRO, name = "Android XR (Home Space Mode)")
annotation class XrHomeSpaceCompactPreview
