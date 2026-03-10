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
package com.android.developers.androidify.creation.xr

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialLayoutSpacer
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import com.android.developers.androidify.creation.AndroidBotColorPicker
import com.android.developers.androidify.creation.BotColor
import com.android.developers.androidify.creation.CreationState
import com.android.developers.androidify.creation.EditScreenScaffold
import com.android.developers.androidify.creation.MainCreationPane
import com.android.developers.androidify.creation.PromptType
import com.android.developers.androidify.creation.PromptTypeToolbar
import com.android.developers.androidify.creation.TransformButton
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.theme.components.AboutButton
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.xr.DisableSharedTransition
import com.android.developers.androidify.xr.MainPanelWorkaround
import com.android.developers.androidify.xr.RequestHomeSpaceIconButton
import com.android.developers.androidify.xr.SquiggleBackgroundSubspace
import com.android.developers.androidify.creation.R as CreationR

@Composable
fun EditScreenSpatial(
    dropBehaviourFactory: DropBehaviourFactory,
    onCameraPressed: () -> Unit,
    onBackPressed: () -> Unit,
    onAboutPressed: () -> Unit,
    uiState: CreationState,
    snackbarHostState: SnackbarHostState,
    onChooseImageClicked: (PickVisualMedia.VisualMediaType) -> Unit,
    onPromptOptionSelected: (PromptType) -> Unit,
    onUndoPressed: () -> Unit,
    onPromptGenerationPressed: () -> Unit,
    onBotColorSelected: (BotColor) -> Unit,
    onStartClicked: () -> Unit,
    onDropCallback: (Uri) -> Unit = {},
) {
    DisableSharedTransition {
        MainPanelWorkaround()
        SquiggleBackgroundSubspace(minimumHeight = 600.dp) {
            SpatialColumn(SubspaceModifier.fillMaxWidth()) {
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
                        AndroidifyTopAppBar(
                            backEnabled = true,
                            isMediumWindowSize = true,
                            onBackPressed = onBackPressed,
                            actions = {
                                AboutButton {
                                    onAboutPressed()
                                }
                                RequestHomeSpaceIconButton()
                            },
                            expandedCenterButtons = {
                                PromptTypeToolbar(
                                    uiState.selectedPromptOption,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                    onOptionSelected = onPromptOptionSelected,
                                )
                            },
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                SpatialRow(SubspaceModifier.fillMaxWidth(0.7f)) {
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .offset(z = 10.dp)
                            .weight(1.3f)
                            .fillMaxHeight(0.8f),
                    ) {
                        Box(
                            Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = MaterialTheme.shapes.large,
                            ),
                        ) {
                            EditScreenScaffold(
                                snackbarHostState = snackbarHostState,
                                topBar = {},
                            ) {
                                MainCreationPane(
                                    uiState = uiState,
                                    dropBehaviourFactory = dropBehaviourFactory,
                                    onCameraPressed = onCameraPressed,
                                    onChooseImageClicked = {
                                        onChooseImageClicked(PickVisualMedia.ImageOnly)
                                    },
                                    onUndoPressed = onUndoPressed,
                                    onPromptGenerationPressed = onPromptGenerationPressed,
                                    onSelectedPromptOptionChanged = onPromptOptionSelected,
                                    onDropCallback = onDropCallback,
                                )
                            }
                        }
                    }
                    SpatialLayoutSpacer(SubspaceModifier.width(48.dp))
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .offset(z = 10.dp)
                            .weight(1f)
                            .fillMaxHeight(0.8f),
                    ) {
                        Box(
                            Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = MaterialTheme.shapes.large,
                            ),
                        ) {
                            AndroidBotColorPicker(
                                selectedBotColor = uiState.botColor,
                                modifier = Modifier.padding(16.dp),
                                onBotColorSelected = onBotColorSelected,
                                listBotColor = uiState.listBotColors,
                            )
                        }
                    }

                    Orbiter(
                        position = ContentEdge.Bottom,
                        alignment = Alignment.End,
                        offset = 16.dp,
                    ) {
                        TransformButton(
                            buttonText = stringResource(CreationR.string.start_transformation_button),
                            onClicked = onStartClicked,
                        )
                    }
                }
            }
        }
    }
}
