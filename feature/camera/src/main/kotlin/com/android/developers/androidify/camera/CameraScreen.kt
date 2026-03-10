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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.developers.androidify.camera

import android.Manifest
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.camera.core.SurfaceRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import com.android.developers.androidify.theme.LocalSharedTransitionScope
import com.android.developers.androidify.theme.SharedElementKey
import com.android.developers.androidify.theme.sharedBoundsRevealWithShapeMorph
import com.android.developers.androidify.theme.sharedBoundsWithDefaults
import com.android.developers.androidify.util.calculateCorrectAspectRatio
import com.android.developers.androidify.util.isTableTopPosture
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel(),
    onImageCaptured: (Uri) -> Unit,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        Box(
            modifier
                .fillMaxSize()
                .sharedBoundsRevealWithShapeMorph(
                    rememberSharedContentState(SharedElementKey.CameraButtonToFullScreenCamera),
                    targetShape = MaterialShapes.Cookie9Sided,
                    restingShape = RoundedPolygon.rectangle().normalized(),
                    targetValueByState = {
                        when (it) {
                            EnterExitState.PreEnter -> 1f
                            EnterExitState.Visible -> 0f
                            EnterExitState.PostExit -> 1f
                        }
                    },
                )
                .sharedBoundsWithDefaults(rememberSharedContentState(SharedElementKey.CaptureImageToDetails)),
        ) {
            val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
            if (cameraPermissionState.status.isGranted) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val activity = LocalActivity.current as ComponentActivity
                val foldingFeature by viewModel.foldingFeature.collectAsState()

                LaunchedEffect(uiState.imageUri) {
                    // this is used to navigate to the next screen, when an image is captured, it signals to NavController that we should do something with the URI,
                    // once the signal has been sent, the value is set to null to not trigger navigation over and over again.
                    val uri = uiState.imageUri
                    if (uri != null) {
                        onImageCaptured(uri)
                        viewModel.setCapturedImage(null)
                    }
                }
                val scope = rememberCoroutineScope()
                LifecycleStartEffect(viewModel) {
                    val job = scope.launch { viewModel.bindToCamera() }
                    onStopOrDispose { job.cancel() }
                }

                LaunchedEffect(Unit) {
                    viewModel.calculateFoldingFeature(activity)
                    viewModel.initRearDisplayFeature(activity)
                }

                uiState.surfaceRequest?.let { surface ->
                    CameraPreviewContent(
                        modifier = Modifier.fillMaxSize(),
                        surfaceRequest = surface,
                        autofocusUiState = uiState.autofocusUiState,
                        tapToFocus = viewModel::tapToFocus,
                        detectedPose = uiState.detectedPose,
                        defaultZoomOptions = uiState.zoomOptions,
                        requestFlipCamera = viewModel::flipCameraDirection,
                        canFlipCamera = uiState.canFlipCamera,
                        requestCaptureImage = viewModel::captureImage,
                        zoomRange = uiState.zoomMinRatio..uiState.zoomMaxRatio,
                        zoomLevel = { uiState.zoomLevel },
                        onChangeZoomLevel = viewModel::setZoomLevel,
                        foldingFeature = foldingFeature,
                        shouldShowRearCameraFeature = viewModel::shouldShowRearDisplayFeature,
                        toggleRearCameraFeature = { viewModel.toggleRearDisplayFeature(activity) },
                        isRearCameraEnabled = uiState.isRearCameraActive,
                        cameraSessionId = uiState.cameraSessionId,
                        xrEnabled = uiState.xrEnabled,
                        surfaceAspectRatio = uiState.surfaceAspectRatio,
                    )
                }
            } else {
                CameraPermissionGrant(
                    launchPermissionRequest = {
                        cameraPermissionState.launchPermissionRequest()
                    },
                    showRationale = cameraPermissionState.status.shouldShowRationale,
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionGrant(
    launchPermissionRequest: () -> Unit,
    showRationale: Boolean,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(showRationale) {
        if (!showRationale) {
            launchPermissionRequest()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.camera_permission_rationale), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { launchPermissionRequest() }) {
            Text(stringResource(R.string.camera_permission_grant_button))
        }
    }
}

/**
 * A stateless version of the Camera Preview Content, designed for easier testing.
 * It accepts a generic viewfinder composable slot.
 */
@Composable
fun StatelessCameraPreviewContent(
    viewfinder: @Composable (Modifier) -> Unit,
    canFlipCamera: Boolean,
    requestFlipCamera: () -> Unit,
    detectedPose: Boolean,
    defaultZoomOptions: List<Float>,
    zoomLevel: () -> Float,
    onAnimateZoom: (Float) -> Unit,
    requestCaptureImage: () -> Unit,
    modifier: Modifier = Modifier,
    surfaceAspectRatio: Float = 9f / 16f,
    xrEnabled: Boolean = false,
    foldingFeature: FoldingFeature? = null,
    shouldShowRearCameraFeature: () -> Boolean = { false },
    toggleRearCameraFeature: () -> Unit = {},
    isRearCameraEnabled: Boolean = false,
) {
    var layoutAspectRatio by remember { mutableFloatStateOf(9f / 16f) }
    val emptyComposable: @Composable (Modifier) -> Unit = {}
    val rearCameraButton: @Composable (Modifier) -> Unit = { rearModifier ->
        RearCameraButton(
            isRearCameraEnabled = isRearCameraEnabled,
            toggleRearCamera = toggleRearCameraFeature,
            modifier = rearModifier,
        )
    }

    CameraLayout(
        viewfinder = viewfinder,
        captureButton = { captureModifier ->
            CameraCaptureButton(
                modifier = captureModifier,
                enabled = detectedPose,
                captureImageClicked = requestCaptureImage,
            )
        },
        flipCameraButton = { flipModifier ->
            if (canFlipCamera) {
                CameraDirectionButton(
                    flipCameraDirection = requestFlipCamera,
                    modifier = flipModifier,
                )
            } else {
                emptyComposable(flipModifier) // Pass modifier even if empty
            }
        },
        zoomButton = { zoomModifier ->
            ZoomToolbar(
                defaultZoomOptions = defaultZoomOptions,
                onZoomLevelSelected = onAnimateZoom,
                modifier = zoomModifier,
                zoomLevel = zoomLevel,
            )
        },
        guideText = { guideTextModifier ->
            AnimatedVisibility(
                !detectedPose,
                enter = fadeIn(MaterialTheme.motionScheme.slowEffectsSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.slowEffectsSpec()),
                modifier = guideTextModifier,
            ) {
                CameraGuideText()
            }
        },
        guide = { guideModifier ->
            CameraGuide(
                detectedPose = detectedPose,
                modifier = guideModifier,
                defaultAspectRatio = layoutAspectRatio,
            )
        },
        rearCameraButton = (
            if (shouldShowRearCameraFeature()) {
                rearCameraButton
            } else {
                emptyComposable
            }
            ),
        isTabletop = isTableTopPosture(foldingFeature),
        modifier = modifier.onSizeChanged { size ->
            if (size.height > 0) {
                // Recalculate aspect ratio based on the overall layout size
                layoutAspectRatio =
                    calculateCorrectAspectRatio(size.height, size.width, layoutAspectRatio)
            }
        },
        surfaceAspectRatio = surfaceAspectRatio,
        xrEnabled = xrEnabled,
    )
}

/**
 * Displays the camera preview and controls. This version is stateful and interacts
 * directly with CameraX components like SurfaceRequest. It now delegates the layout
 * to StatelessCameraPreviewContent.
 */
@Composable
private fun CameraPreviewContent(
    surfaceRequest: SurfaceRequest,
    autofocusUiState: AutofocusUiState,
    tapToFocus: (Offset) -> Unit,
    cameraSessionId: Int,
    canFlipCamera: Boolean,
    requestFlipCamera: () -> Unit,
    detectedPose: Boolean,
    defaultZoomOptions: List<Float>,
    zoomRange: ClosedFloatingPointRange<Float>,
    zoomLevel: () -> Float,
    onChangeZoomLevel: (zoomLevel: Float) -> Unit,
    requestCaptureImage: () -> Unit,
    surfaceAspectRatio: Float,
    modifier: Modifier = Modifier,
    foldingFeature: FoldingFeature? = null,
    shouldShowRearCameraFeature: () -> Boolean = { false },
    toggleRearCameraFeature: () -> Unit = {},
    isRearCameraEnabled: Boolean = false,
    xrEnabled: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val zoomState = remember(cameraSessionId) {
        ZoomState(
            initialZoomLevel = zoomLevel(),
            onChangeZoomLevel = onChangeZoomLevel,
            zoomRange = zoomRange,
        )
    }
    // Delegate the layout to the stateless version
    StatelessCameraPreviewContent(
        viewfinder = { viewfinderModifier ->
            // Provide the actual CameraViewfinder implementation for the slot
            var aspectRatio by remember { mutableFloatStateOf(9f / 16f) } // Keep aspect ratio logic here if needed by viewfinder
            CameraViewfinder(
                surfaceRequest = surfaceRequest,
                autofocusUiState = autofocusUiState,
                tapToFocus = tapToFocus,
                onScaleZoom = { scope.launch { zoomState.scaleZoom(it) } },
                modifier = viewfinderModifier.onSizeChanged { size -> // Apply modifier from slot
                    if (size.height > 0) {
                        aspectRatio =
                            calculateCorrectAspectRatio(size.height, size.width, aspectRatio)
                    }
                },
            )
        },
        // Pass down all other state and callbacks
        canFlipCamera = canFlipCamera,
        requestFlipCamera = requestFlipCamera,
        detectedPose = detectedPose,
        zoomLevel = zoomLevel,
        defaultZoomOptions = defaultZoomOptions,
        onAnimateZoom = { scope.launch { zoomState.animatedZoom(it) } },
        requestCaptureImage = requestCaptureImage,
        foldingFeature = foldingFeature,
        shouldShowRearCameraFeature = shouldShowRearCameraFeature,
        toggleRearCameraFeature = toggleRearCameraFeature,
        isRearCameraEnabled = isRearCameraEnabled,
        surfaceAspectRatio = surfaceAspectRatio,
        xrEnabled = xrEnabled,
        modifier = modifier,
    )
}
