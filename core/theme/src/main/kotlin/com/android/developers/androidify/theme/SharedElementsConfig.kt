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
@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.developers.androidify.theme

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlin.math.max

sealed interface SharedElementKey {
    object CameraButtonToFullScreenCamera : SharedElementKey
    object CaptureImageToDetails : SharedElementKey
    object AboutKey : SharedElementKey
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    throw IllegalStateException("No SharedTransitionScope provided")
}

val LocalAnimateBoundsScope = compositionLocalOf<LookaheadScope?> {
    null
}

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
val MotionScheme.sharedElementTransitionBounds: BoundsTransform
    @Composable
    get() = BoundsTransform { _, _ -> sharedElementTransitionSpec() }

fun <T> MotionScheme.sharedElementTransitionSpec(): FiniteAnimationSpec<T> {
    return tween(600)
}

@Composable
fun Modifier.sharedBoundsReveal(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedContentScope.current,
    boundsTransform: BoundsTransform = MaterialTheme.motionScheme.sharedElementTransitionBounds,
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
    clipShape: Shape = MaterialTheme.shapes.large,
    renderInOverlayDuringTransition: Boolean = true,
): Modifier {
    with(sharedTransitionScope) {
        return this@sharedBoundsReveal
            .sharedBoundsWithDefaults(
                sharedContentState,
                sharedTransitionScope,
                animatedVisibilityScope,
                boundsTransform,
                resizeMode,
                clipShape,
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            )
            .skipToLookaheadSize()
            .skipToLookaheadPosition()
    }
}

@Composable
fun Modifier.sharedBoundsWithDefaults(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedContentScope.current,
    boundsTransform: BoundsTransform = MaterialTheme.motionScheme.sharedElementTransitionBounds,
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
    clipShape: Shape = MaterialTheme.shapes.large,
    renderInOverlayDuringTransition: Boolean = true,
): Modifier {
    with(sharedTransitionScope) {
        return this@sharedBoundsWithDefaults
            .sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
                resizeMode = resizeMode,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            )
    }
}

class MorphOverlayClip(val morph: Morph, private val animatedProgress: () -> Float) :
    SharedTransitionScope.OverlayClip {
    private val matrix = Matrix()

    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path? {
        matrix.reset()
        val max = max(bounds.width, bounds.height)
        matrix.scale(max, max)

        val path = morph.toPath(progress = animatedProgress.invoke())
        path.transform(matrix)
        path.translate(bounds.center + Offset(-max / 2f, -max / 2f))
        return path
    }
}

@Composable
fun Modifier.sharedBoundsRevealWithShapeMorph(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedContentScope.current,
    boundsTransform: BoundsTransform = MaterialTheme.motionScheme.sharedElementTransitionSpec,
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
    restingShape: RoundedPolygon = RoundedPolygon.rectangle().normalized(),
    targetShape: RoundedPolygon = RoundedPolygon.circle().normalized(),
    renderInOverlayDuringTransition: Boolean = true,
    targetValueByState: @Composable (state: EnterExitState) -> Float = {
        when (it) {
            EnterExitState.PreEnter -> 1f
            EnterExitState.Visible -> 0f
            EnterExitState.PostExit -> 1f
        }
    },
    keepChildrenSizePlacement: Boolean = true,
): Modifier {
    with(sharedTransitionScope) {
        val animatedProgress =
            animatedVisibilityScope.transition.animateFloat(targetValueByState = targetValueByState)

        val morph = remember {
            Morph(restingShape, targetShape)
        }
        val morphClip = MorphOverlayClip(morph, { animatedProgress.value })
        val modifier = if (keepChildrenSizePlacement) {
            Modifier
                .skipToLookaheadSize()
                .skipToLookaheadPosition()
        } else {
            Modifier
        }
        return this@sharedBoundsRevealWithShapeMorph
            .sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
                resizeMode = resizeMode,
                clipInOverlayDuringTransition = morphClip,
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            )
            .then(modifier)
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun SharedElementContextPreview(content: @Composable () -> Unit) {
    AndroidifyTheme {
        AnimatedContent(targetState = Unit) {
            CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun SharedElementBoundsWithShapeMorph() {
    AndroidifyTheme {
        var showFullScreen by remember {
            mutableStateOf(false)
        }
        AnimatedContent(showFullScreen) { targetState ->
            CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                val sharedContentScope = LocalSharedTransitionScope.current
                with(sharedContentScope) {
                    if (targetState) {
                        FullScreenSample({
                            showFullScreen = false
                        })
                    } else {
                        SmallScreenSample({
                            showFullScreen = true
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.SmallScreenSample(onButtonPress: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        val sharedContentState =
            rememberSharedContentState("Test")
        Box(
            modifier = Modifier
                .sharedBoundsRevealWithShapeMorph(
                    sharedContentState,
                    restingShape = MaterialShapes.Cookie9Sided,
                    targetShape = MaterialShapes.Square,
                    targetValueByState = {
                        when (it) {
                            EnterExitState.PreEnter -> 0f
                            EnterExitState.Visible -> 1f
                            EnterExitState.PostExit -> 1f
                        }
                    },
                    keepChildrenSizePlacement = false,
                )
                .align(Alignment.BottomCenter)
                .clickable {
                    onButtonPress()
                }
                .size(48.dp)
                .aspectRatio(1f)
                .background(
                    Color.Red,
                    MaterialShapes.Cookie9Sided.toShape(),
                ),
        )
    }
}

@Composable
private fun SharedTransitionScope.FullScreenSample(onButtonPress: () -> Unit) {
    val sharedContentState =
        rememberSharedContentState("Test")
    Box(
        modifier = Modifier
            .sharedBoundsRevealWithShapeMorph(
                sharedContentState,
                targetShape = MaterialShapes.Cookie9Sided,
                restingShape = MaterialShapes.Square,
                targetValueByState = {
                    when (it) {
                        EnterExitState.PreEnter -> 1f
                        EnterExitState.Visible -> 0f
                        EnterExitState.PostExit -> 1f
                    }
                },
            )
            .fillMaxSize()
            .background(Color.Blue)
            .clickable {
                onButtonPress()
            },
    )
}
