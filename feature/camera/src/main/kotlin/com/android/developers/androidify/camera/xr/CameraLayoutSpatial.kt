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
package com.android.developers.androidify.camera.xr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterOffsetType
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.aspectRatio
import androidx.xr.compose.subspace.layout.fillMaxSize
import com.android.developers.androidify.xr.MainPanelWorkaround
import com.android.developers.androidify.xr.RequestHomeSpaceIconButton

@Composable
fun CameraLayoutSpatial(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    surfaceAspectRatio: Float,
) {
    Subspace {
        MainPanelWorkaround()
        SpatialPanel(
            SubspaceModifier
                .fillMaxSize(0.5f)
                .aspectRatio(surfaceAspectRatio),
        ) {
            Orbiter(
                position = ContentEdge.Top,
                offsetType = OrbiterOffsetType.InnerEdge,
                offset = 32.dp,
                alignment = Alignment.End,
            ) {
                RequestHomeSpaceIconButton(
                    modifier = Modifier
                        .size(64.dp, 64.dp)
                        .padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            }
            Orbiter(ContentEdge.Start, offsetType = OrbiterOffsetType.InnerEdge, offset = 16.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    captureButton(Modifier)
                    flipCameraButton(Modifier)
                }
            }
            Orbiter(ContentEdge.Bottom, offsetType = OrbiterOffsetType.InnerEdge) {
                zoomButton(Modifier)
            }
            Box(Modifier.fillMaxSize()) {
                viewfinder(Modifier)
                guide(Modifier.fillMaxSize())
                guideText(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 36.dp, vertical = 64.dp),
                )
            }
        }
    }
}
