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
package com.android.developers.androidify.home.xr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterOffsetType
import androidx.xr.compose.subspace.SpatialPanel
import com.android.developers.androidify.home.AboutScreenMedium
import com.android.developers.androidify.home.BackButton
import com.android.developers.androidify.xr.MainPanelWorkaround
import com.android.developers.androidify.xr.SquiggleBackgroundSubspace

@Composable
fun AboutScreenSpatial(onBackPressed: () -> Unit, bottomButtons: @Composable () -> Unit) {
    MainPanelWorkaround()
    SquiggleBackgroundSubspace(500.dp) {
        SpatialPanel {
            Orbiter(
                ContentEdge.Top,
                offset = 16.dp,
                offsetType = OrbiterOffsetType.InnerEdge,
                alignment = Alignment.Start,
            ) {
                BackButton(onBackPressed)
            }
            AboutScreenMedium(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
                padding = PaddingValues(32.dp),
                bottomButtons = null,
            )
            Orbiter(
                position = ContentEdge.Bottom,
                offset = 48.dp,
                offsetType = OrbiterOffsetType.InnerEdge,
                elevation = 0.dp,
            ) {
                bottomButtons()
            }
        }
    }
}
