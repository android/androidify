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
package com.android.developers.androidify.customize.watchface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.wear.common.ConnectedWatch
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchFaceModalSheet(
    connectedWatch: ConnectedWatch,
    onWatchFaceInstallClick: (String) -> Unit,
    onAndroidifyInstallClick: suspend () -> Boolean,
    installationStatus: WatchFaceInstallationStatus,
    sheetState: SheetState,
    watchFaceSelectionState: WatchFaceSelectionState,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onWatchFaceSelect: (WatchFaceAsset) -> Unit,
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .padding(bottom = 16.dp, top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.watch_24),
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.send_to_watch_device, connectedWatch.displayName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                connectedWatch.hasAndroidify -> {
                    AnimatedContent(
                        targetState = installationStatus,
                        transitionSpec = {
                            if (
                                initialState is WatchFaceInstallationStatus.Preparing &&
                                targetState is WatchFaceInstallationStatus.Sending
                            ) {
                                EnterTransition.None togetherWith ExitTransition.None
                            } else {
                                ContentTransform(
                                    targetContentEnter = fadeIn(
                                        animationSpec = tween(durationMillis = 500),
                                    ),
                                    initialContentExit = fadeOut(
                                        animationSpec = tween(durationMillis = 500),
                                    ),
                                )
                            }
                        },
                    ) { installationStatus ->
                        when (installationStatus) {
                            is WatchFaceInstallationStatus.Complete -> {
                                if (installationStatus.success) {
                                    when (installationStatus.activationStrategy) {
                                        WatchFaceActivationStrategy.LONG_PRESS_TO_SET -> {
                                            GuidanceWatchFacePanel(
                                                selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                                guidanceTextResId = R.string.complete_long_press,
                                                dismissClick = onDismiss,
                                            )
                                        }

                                        WatchFaceActivationStrategy.FOLLOW_PROMPT_ON_WATCH -> {
                                            GuidanceWatchFacePanel(
                                                selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                                guidanceTextResId = R.string.complete_permissions,
                                                dismissClick = onDismiss,
                                            )
                                        }

                                        WatchFaceActivationStrategy.NO_ACTION_NEEDED,
                                        WatchFaceActivationStrategy.CALL_SET_ACTIVE_NO_USER_ACTION,
                                        -> {
                                            AllDoneWatchFacePanel(
                                                selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                                onAllDoneClick = onDismiss,
                                            )
                                        }

                                        WatchFaceActivationStrategy.GO_TO_WATCH_SETTINGS -> {
                                            GuidanceWatchFacePanel(
                                                selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                                guidanceTextResId = R.string.complete_settings,
                                                dismissClick = onDismiss,
                                            )
                                        }
                                    }
                                } else {
                                    ErrorWatchFacePanel(
                                        selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                        errorTextResId = R.string.complete_error_message,
                                        onAllDoneClick = onDismiss,
                                    )
                                }
                            }

                            is WatchFaceInstallationStatus.Preparing -> {
                                TransferringWatchFacePanel(
                                    selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                    transferLabel = stringResource(R.string.preparing_to_send),
                                )
                            }

                            is WatchFaceInstallationStatus.Sending -> {
                                TransferringWatchFacePanel(
                                    selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                                    transferLabel = stringResource(R.string.sending_to_watch),
                                )
                            }

                            else -> {
                                InstallWatchFacePanel(
                                    onInstallClick = {
                                        onWatchFaceInstallClick(connectedWatch.nodeId)
                                    },
                                    watchFaceSelectionState = watchFaceSelectionState,
                                    onWatchFaceSelect = onWatchFaceSelect,
                                )
                            }
                        }
                    }
                }

                else -> {
                    InstallAndroidifyPanel(
                        onInstallClick = onAndroidifyInstallClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WatchFaceModalSheetPreview() {
    val device = ConnectedWatch(
        nodeId = "1234",
        displayName = "Pixel Watch",
        hasAndroidify = true,
    )
    val watchface = WatchFaceAsset(
        id = "watch_face_1",
        previewPath = R.drawable.watch_face_preview,
    )
    val sheetState = SheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true,
        skipPartiallyExpanded = true,
        positionalThreshold = { 0f },
        velocityThreshold = { 0f },
    )
    val watchFaceSelectionState = WatchFaceSelectionState(
        watchFaces = listOf(watchface),
        selectedWatchFace = watchface,
        isLoadingWatchFaces = false,
    )
    AndroidifyTheme {
        WatchFaceModalSheet(
            connectedWatch = device,
            installationStatus = WatchFaceInstallationStatus.NotStarted,
            watchFaceSelectionState = watchFaceSelectionState,
            onWatchFaceSelect = {},
            onLoad = {},
            onDismiss = {},
            onWatchFaceInstallClick = {},
            onAndroidifyInstallClick = { true },
            sheetState = sheetState,
        )
    }
}
