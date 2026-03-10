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
package com.android.developers.androidify.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme

@Composable
internal fun CameraControls(
    captureImageClicked: () -> Unit,
    canFlipCamera: Boolean,
    flipCameraDirectionClicked: () -> Unit,
    detectedPose: Boolean,
    defaultZoomOptions: List<Float>,
    zoomLevel: () -> Float,
    onZoomLevelSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZoomToolbar(
            defaultZoomOptions = defaultZoomOptions,
            zoomLevel = zoomLevel,
            onZoomLevelSelected = onZoomLevelSelected,
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (canFlipCamera) {
                    CameraDirectionButton(
                        flipCameraDirection = flipCameraDirectionClicked,
                    )
                }
            }
            CameraCaptureButton(
                captureImageClicked = captureImageClicked,
                enabled = detectedPose,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview
@Composable
private fun CameraControlsPreview() {
    AndroidifyTheme {
        CameraControls(
            captureImageClicked = { },
            canFlipCamera = true,
            flipCameraDirectionClicked = { },
            detectedPose = true,
            zoomLevel = { 0.4f },
            onZoomLevelSelected = {},
            defaultZoomOptions = listOf(.6f, 1f),
        )
    }
}
