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
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.runtime.math.Vector3
import com.android.developers.androidify.results.FlippableState

@Composable
fun FlippablePanel(
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    modifier: SubspaceModifier = SubspaceModifier,
    flipDurationMillis: Int = 1000,
    flippableState: FlippableState = FlippableState.Front,
    onFlipStateChanged: ((FlippableState) -> Unit)? = null,
) {
    val transition = updateTransition(flippableState)
    val frontRotation by getRotation(transition, flipDurationMillis)

    val onClickFlip = {
        onFlipStateChanged?.invoke(flippableState.toggle()) ?: Unit
    }
    val interactionSource = remember { MutableInteractionSource() }

    Subspace {
        SpatialBox(modifier.rotate(Vector3.Up, frontRotation)) {
            SpatialPanel(
                SubspaceModifier
                    .offset(z = 100.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClickFlip,
                        ),
                ) {
                    front()
                }
            }
            SpatialPanel(
                SubspaceModifier
                    .offset(z = (-100).dp)
                    .rotate(Vector3.Up, 180f),
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClickFlip,
                        ),
                ) {
                    back()
                }
            }
        }
    }
}

@Composable
private fun getRotation(
    transition: Transition<FlippableState>,
    flipMs: Int,
) = transition.animateFloat(
    transitionSpec = {
        when {
            FlippableState.Front isTransitioningTo FlippableState.Back -> {
                keyframes {
                    durationMillis = flipMs
                    0f at 0
                    180f at flipMs
                }
            }

            FlippableState.Back isTransitioningTo FlippableState.Front -> {
                keyframes {
                    durationMillis = flipMs
                    180f at 0
                    0f at flipMs
                }
            }

            else -> snap()
        }
    },
    label = "Panel Rotation",
) { state ->
    when (state) {
        FlippableState.Front -> 0f
        FlippableState.Back -> 180f
    }
}
