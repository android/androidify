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
package com.android.developers.androidify.creation

import androidx.compose.runtime.Composable
import com.android.developers.androidify.util.areBothWindowDimensionsAtLeastMedium
import com.android.developers.androidify.xr.LocalSpatialCapabilities

enum class EditScreenLayoutType {
    Compact,
    Medium,
    Spatial,
}

@Composable
fun calculateLayoutType(enableXr: Boolean = false): EditScreenLayoutType {
    return when {
        LocalSpatialCapabilities.current.isSpatialUiEnabled && enableXr -> EditScreenLayoutType.Spatial
        areBothWindowDimensionsAtLeastMedium() -> EditScreenLayoutType.Medium
        else -> EditScreenLayoutType.Compact
    }
}
