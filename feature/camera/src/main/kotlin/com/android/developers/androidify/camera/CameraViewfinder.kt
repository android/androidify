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

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.android.developers.androidify.theme.Error
import com.android.developers.androidify.theme.LimeGreen
import com.android.developers.androidify.theme.Surface

private val TAP_TO_FOCUS_INDICATOR_SIZE = 48.dp

@Composable
internal fun CameraViewfinder(
    surfaceRequest: SurfaceRequest,
    autofocusUiState: AutofocusUiState,
    tapToFocus: (tapCoords: Offset) -> Unit,
    onScaleZoom: (zoomScaleFactor: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onScaleCurrentZoom by rememberUpdatedState(onScaleZoom)
    val currentTapToFocus by rememberUpdatedState(tapToFocus)
    val coordinateTransformer = remember { MutableCoordinateTransformer() }
    CameraXViewfinder(
        surfaceRequest = surfaceRequest,
        coordinateTransformer = coordinateTransformer,
        modifier = modifier
            .pointerInput(coordinateTransformer) {
                detectTapGestures { tapCoords ->
                    with(coordinateTransformer) {
                        currentTapToFocus(tapCoords.transform())
                    }
                }
            }
            .transformable(
                rememberTransformableState(
                    onTransformation = { zoomChange, _, _ ->
                        onScaleCurrentZoom(zoomChange)
                    },
                ),
            ),
    )

    if (autofocusUiState is AutofocusUiState.Specified) {
        // Show the autofocus indicator while the autofocus routine is running
        val showAutofocusIndicator = autofocusUiState.status == AutofocusUiState.Status.RUNNING
        // Map coordinates from surface coordinates back to screen coordinates
        val tapCoords =
            remember(coordinateTransformer.transformMatrix, autofocusUiState.surfaceCoordinates) {
                Matrix().run {
                    setFrom(coordinateTransformer.transformMatrix)
                    invert()
                    map(autofocusUiState.surfaceCoordinates)
                }
            }
        AnimatedVisibility(
            visible = showAutofocusIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .offset { tapCoords.round() }
                .offset(-TAP_TO_FOCUS_INDICATOR_SIZE / 2, -TAP_TO_FOCUS_INDICATOR_SIZE / 2),
        ) {
            Spacer(
                Modifier
                    .border(
                        2.dp,
                        when (autofocusUiState.status) {
                            AutofocusUiState.Status.SUCCESS -> LimeGreen
                            AutofocusUiState.Status.FAILURE -> Error
                            else -> Surface
                        },
                        CircleShape,
                    )
                    .size(TAP_TO_FOCUS_INDICATOR_SIZE),
            )
        }
    }
}
