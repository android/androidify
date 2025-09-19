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
package com.android.developers.androidify.customize.xr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterOffsetType
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialLayoutSpacer
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import com.android.developers.androidify.customize.CustomizeExportScreenScaffold
import com.android.developers.androidify.customize.CustomizeExportState
import com.android.developers.androidify.customize.ExportImageCanvas
import com.android.developers.androidify.customize.ToolDetailComposable
import com.android.developers.androidify.customize.ToolSelectorComposable
import com.android.developers.androidify.theme.LocalAnimateBoundsScope
import com.android.developers.androidify.xr.DisableSharedTransition
import com.android.developers.androidify.xr.MainPanelWorkaround
import com.android.developers.androidify.xr.SquiggleBackgroundSubspace

@Composable
fun CustomizeExportLayoutSpatial(
    state: CustomizeExportState,
    snackbarHostState: SnackbarHostState,
    imageResult: @Composable (ExportImageCanvas.() -> Unit),
    toolDetail: ToolDetailComposable,
    toolSelector: ToolSelectorComposable,
    actionButtons: @Composable (Modifier) -> Unit,
    topBar: @Composable () -> Unit,
) {
    DisableSharedTransition {
        SquiggleBackgroundSubspace(minimumHeight = 600.dp) {
            MainPanelWorkaround()
            SpatialColumn(SubspaceModifier.fillMaxWidth()) {
                Orbiter(position = ContentEdge.Bottom, alignment = Alignment.End) {
                    actionButtons(Modifier)
                }
                SpatialPanel(
                    SubspaceModifier.offset(z = 10.dp)
                        .fillMaxWidth(0.5f),
                ) {
                    Column(
                        Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.large,
                        ),
                    ) {
                        topBar()
                        Spacer(Modifier.size(16.dp))
                    }
                }

                SpatialRow(SubspaceModifier.fillMaxWidth(0.7f)) {
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .offset(z = 10.dp)
                            .weight(1f, fill = true)
                            .fillMaxHeight(0.8f),
                    ) {
                        CustomizeExportScreenScaffold(
                            snackbarHostState = snackbarHostState,
                            topBar = {},
                            containerColor = Color.Transparent,
                        ) {
                            LookaheadScope {
                                CompositionLocalProvider(LocalAnimateBoundsScope provides this) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        imageResult(state.exportImageCanvas)
                                    }
                                }
                            }
                        }
                    }
                    SpatialLayoutSpacer(SubspaceModifier.width(48.dp))
                    SpatialBox(SubspaceModifier.fillMaxHeight(0.7f)) {
                        SpatialPanel(
                            modifier = SubspaceModifier.offset(z = 10.dp).fillMaxWidth(0.3f),
                        ) {
                            toolDetail(Modifier, false)
                            Orbiter(
                                position = ContentEdge.End,
                                offsetType = OrbiterOffsetType.InnerEdge,
                            ) {
                                toolSelector(Modifier, false)
                            }
                        }
                    }
                }
            }
        }
    }
}
