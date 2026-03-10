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
package com.android.developers.androidify.results.xr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterOffsetType
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import com.android.developers.androidify.results.ResultsScreenScaffold
import com.android.developers.androidify.xr.BackgroundSubspace
import com.android.developers.androidify.xr.DisableSharedTransition
import com.android.developers.androidify.xr.MainPanelWorkaround

@Composable
fun ResultsScreenSpatial(
    backgroundQuotes: @Composable (Modifier) -> Unit,
    botResultCard: @Composable (Modifier) -> Unit,
    buttonRow: @Composable (Modifier) -> Unit,
    topBar: @Composable (Modifier) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var offsetPose by remember { mutableStateOf(Pose()) }
    DisableSharedTransition {
        MainPanelWorkaround()
        Subspace {
            SpatialPanel(
                SubspaceModifier
                    .offset(z = 5.dp)
                    .transform(offsetPose)
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f),
            ) {
                backgroundQuotes(Modifier.featheredEdges(Size(0.1f, 0f)))
            }
            SpatialBox(SubspaceModifier.fillMaxWidth(0.6f)) {
                BackgroundSubspace(
                    aspectRatio = 1.1f,
                    drawable = com.android.developers.androidify.results.R.drawable.background_results,
                    minimumHeight = 500.dp,
                    onMove = { moveEvent ->
                        offsetPose = moveEvent.pose
                        false
                    },
                ) {
                    Orbiter(ContentEdge.Top, offsetType = OrbiterOffsetType.InnerEdge) {
                        Box(
                            Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    shape = MaterialTheme.shapes.large,
                                )
                                .padding(bottom = 16.dp),
                        ) {
                            topBar(Modifier)
                        }
                    }

                    SpatialPanel(
                        SubspaceModifier
                            .offset(z = 10.dp)
                            .rotate(Vector3.Forward, 5f)
                            .fillMaxHeight(0.7f)
                            .fillMaxWidth(0.5f),
                    ) {
                        ResultsScreenScaffold(snackbarHostState, topBar = {}, containerColor = Color.Transparent) {
                            botResultCard(Modifier)
                        }
                    }

                    Orbiter(
                        position = ContentEdge.Bottom,
                        offsetType = OrbiterOffsetType.InnerEdge,
                        alignment = Alignment.End,
                    ) {
                        buttonRow(Modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubspaceModifier.transform(pose: Pose): SubspaceModifier {
    val density = LocalDensity.current
    fun floatToDp(float: Float) = with(density) { float.toDp() }

    return this.offset(
        floatToDp(pose.translation.x),
        floatToDp(pose.translation.y),
        floatToDp(pose.translation.z),
    ).rotate(pose.rotation)
}
