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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Stable

@Stable
class ZoomState(
    initialZoomLevel: Float,
    val zoomRange: ClosedFloatingPointRange<Float>,
    val onChangeZoomLevel: (Float) -> Unit,
) {
    private var functionalZoom = initialZoomLevel

    private val mutatorMutex = MutatorMutex()

    /**
     * Immediately set the current zoom level to [targetZoomLevel].
     */
    suspend fun absoluteZoom(targetZoomLevel: Float) {
        mutatorMutex.mutate {
            functionalZoom = targetZoomLevel.coerceIn(zoomRange)
            onChangeZoomLevel(functionalZoom)
        }
    }

    /**
     * Scale the current zoom level.
     */
    suspend fun scaleZoom(scalingFactor: Float) {
        absoluteZoom(scalingFactor * functionalZoom)
    }

    /**
     * Ease towards a specific zoom level
     *
     * @param animationSpec [AnimationSpec] used for the animation, default to tween over 500ms
     */
    suspend fun animatedZoom(
        targetZoomLevel: Float,
        animationSpec: AnimationSpec<Float> = tween(durationMillis = 500),
    ) {
        mutatorMutex.mutate {
            Animatable(initialValue = functionalZoom).animateTo(
                targetValue = targetZoomLevel,
                animationSpec = animationSpec,
            ) {
                // this is called every animation frame
                functionalZoom = value.coerceIn(zoomRange)
                onChangeZoomLevel(functionalZoom)
            }
        }
    }
}
