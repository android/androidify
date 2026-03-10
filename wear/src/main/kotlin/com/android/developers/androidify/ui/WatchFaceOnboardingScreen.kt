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
@file:OptIn(ExperimentalPermissionsApi::class)

package com.android.developers.androidify.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import com.android.developers.androidify.WatchFaceOnboardingViewModel
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun WatchFaceOnboardingScreen(
    modifier: Modifier = Modifier,
    launchedFromWatchFaceTransfer: Boolean,
    viewModel: WatchFaceOnboardingViewModel = viewModel(factory = WatchFaceOnboardingViewModel.Factory),
) {
    AppScaffold {
        val state by viewModel.state.collectAsStateWithLifecycle()

        when (state) {
            is WatchFaceInstallationStatus.Receiving,
            is WatchFaceInstallationStatus.Preparing,
            is WatchFaceInstallationStatus.Sending,
            -> {
                TransmissionScreen()
            }

            is WatchFaceInstallationStatus.Unknown,
            WatchFaceInstallationStatus.NotStarted,
            -> {
                if (launchedFromWatchFaceTransfer) {
                    TransmissionScreen()
                } else {
                    WelcomeToAndroidifyScreen()
                }
            }

            is WatchFaceInstallationStatus.Complete -> {
                val completeStatus = state as WatchFaceInstallationStatus.Complete
                if (completeStatus.success) {
                    WatchFaceGuidance(
                        strategy = completeStatus.activationStrategy,
                        onPermissionsChange = { granted, shouldShowRationale ->
                            viewModel.maybeSendUpdateOnPermissionsChange(
                                granted,
                                shouldShowRationale,
                            )
                        },
                        onAllDone = {
                            viewModel.resetWatchFaceTransferState()
                        },
                    )
                } else {
                    ErrorScreen(
                        onAllDoneClick = {
                            viewModel.resetWatchFaceTransferState()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun WatchFaceGuidance(
    strategy: WatchFaceActivationStrategy,
    onPermissionsChange: (Boolean, Boolean) -> Unit,
    onAllDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activePermission =
        rememberPermissionState("com.google.wear.permission.SET_PUSHED_WATCH_FACE_AS_ACTIVE") { }
    var previousPermissionStatus by remember {
        mutableStateOf(activePermission.status)
    }
    var previousShouldShowRationale by remember {
        mutableStateOf(activePermission.status.shouldShowRationale)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (activePermission.status != previousPermissionStatus ||
                    activePermission.status.shouldShowRationale != previousShouldShowRationale
                ) {
                    onPermissionsChange(
                        activePermission.status.isGranted,
                        activePermission.status.shouldShowRationale,
                    )
                    previousPermissionStatus = activePermission.status
                    previousShouldShowRationale = activePermission.status.shouldShowRationale
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (strategy) {
        WatchFaceActivationStrategy.GO_TO_WATCH_SETTINGS -> OpenSettingsScreen()
        WatchFaceActivationStrategy.LONG_PRESS_TO_SET -> LongPressScreen(onAllDoneClick = onAllDone)
        WatchFaceActivationStrategy.FOLLOW_PROMPT_ON_WATCH -> PermissionsPromptScreen(
            launchPermissionRequest = { activePermission.launchPermissionRequest() },
        )

        else -> AllDoneScreen(onAllDone = onAllDone)
    }
}
