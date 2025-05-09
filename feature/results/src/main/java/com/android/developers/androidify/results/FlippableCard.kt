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
package com.android.developers.androidify.results

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex

enum class FlippableState {
    Front,
    Back,
    ;

    fun toggle(): FlippableState {
        return when (this) {
            Front -> Back
            Back -> Front
        }
    }
}

@Composable
fun FlippableCard(
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    flipDurationMillis: Int = 1000,
    flippableState: FlippableState = FlippableState.Front,
    onFlipStateChanged: ((FlippableState) -> Unit)? = null,
) {
    val transition = updateTransition(flippableState)
    val frontRotation by getFrontRotation(transition, flipDurationMillis)
    val backRotation by getBackRotation(transition, flipDurationMillis)
    val opacityFront by getFrontOpacitySpec(transition, flipDurationMillis)
    val opacityBack by getBackOpacitySpec(transition, flipDurationMillis)

    val cameraDistance = 30f

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onFlipStateChanged?.invoke(flippableState.toggle())
            },
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    this.cameraDistance = cameraDistance
                    rotationY = backRotation
                    alpha = opacityBack
                }
                .zIndex(1F - opacityBack),
        ) {
            back()
        }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    this.cameraDistance = cameraDistance
                    rotationY = frontRotation
                    alpha = opacityFront
                }
                .zIndex(1F - opacityFront),
        ) {
            front()
        }
    }
}

@Composable
private fun getFrontRotation(
    transition: Transition<FlippableState>,
    flipMs: Int,
) = transition.animateFloat(
    transitionSpec = {
        when {
            FlippableState.Front isTransitioningTo FlippableState.Back -> {
                keyframes {
                    durationMillis = flipMs
                    0f at 0
                    90f at flipMs / 2
                    90f at flipMs
                }
            }

            FlippableState.Back isTransitioningTo FlippableState.Front -> {
                keyframes {
                    durationMillis = flipMs
                    90f at 0
                    90f at flipMs / 2
                    0f at flipMs
                }
            }

            else -> snap()
        }
    },
    label = "front Rotation",
) { state ->
    when (state) {
        FlippableState.Front -> 0f
        FlippableState.Back -> 180f
    }
}

@Composable
private fun getFrontOpacitySpec(
    transition: Transition<FlippableState>,
    flipMs: Int,
) = transition.animateFloat(
    transitionSpec = {
        when {
            FlippableState.Front isTransitioningTo FlippableState.Back -> {
                keyframes {
                    durationMillis = flipMs
                    1f at 0
                    1f at (flipMs / 2) - 1
                    0f at flipMs / 2
                    0f at flipMs
                }
            }

            FlippableState.Back isTransitioningTo FlippableState.Front -> {
                keyframes {
                    durationMillis = flipMs
                    0f at 0
                    0f at (flipMs / 2) - 1
                    1f at flipMs / 2
                    1f at flipMs
                }
            }

            else -> snap()
        }
    },
    label = "animate front",
) { state ->
    when (state) {
        FlippableState.Front -> 1f
        FlippableState.Back -> 0f
    }
}

@Composable
private fun getBackOpacitySpec(
    transition: Transition<FlippableState>,
    flipMs: Int,
) =
    transition.animateFloat(
        transitionSpec = {
            // Create a spec that when its at the halfway point, it snaps to the actual opacity to make the back visible
            when {
                FlippableState.Front isTransitioningTo FlippableState.Back -> {
                    keyframes {
                        durationMillis = flipMs
                        0f at 0
                        0f at (flipMs / 2) - 1
                        1f at flipMs / 2
                        1f at flipMs
                    }
                }

                FlippableState.Back isTransitioningTo FlippableState.Front -> {
                    keyframes {
                        durationMillis = flipMs
                        1f at 0
                        1f at (flipMs / 2) - 1
                        0f at flipMs / 2
                        0f at flipMs
                    }
                }

                else -> snap()
            }
        },
        label = "Back Opacity",
    ) { state ->
        when (state) {
            FlippableState.Front -> 0f
            FlippableState.Back -> 1f
        }
    }

@Composable
private fun getBackRotation(
    transition: Transition<FlippableState>,
    flipMs: Int,
) =
    transition.animateFloat(
        transitionSpec = {
            // TransitionSpec that keeps the rotation at -90 until halfway in the animation, after that point, it animates from -90 to 0
            when {
                FlippableState.Front isTransitioningTo FlippableState.Back -> {
                    keyframes {
                        durationMillis = flipMs
                        -90f at 0
                        -90f at flipMs / 2
                        0f at flipMs
                    }
                }

                FlippableState.Back isTransitioningTo FlippableState.Front -> {
                    keyframes {
                        durationMillis = flipMs
                        0f at 0
                        -90f at flipMs / 2
                        -90f at flipMs
                    }
                }

                else -> snap()
            }
        },
        label = "Back Rotation",
    ) { state ->
        when (state) {
            FlippableState.Front -> 180f
            FlippableState.Back -> 0f
        }
    }
