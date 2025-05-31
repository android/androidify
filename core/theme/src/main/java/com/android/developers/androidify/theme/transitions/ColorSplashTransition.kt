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
package com.android.developers.androidify.theme.transitions

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.spatial.RelativeLayoutBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastRoundToInt
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.Blue
import com.android.developers.androidify.theme.components.PrimaryButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ColorSplashTransitionScreen(
    modifier: Modifier = Modifier,
    enterSpec: AnimationSpec<DpSize> = MaterialTheme.motionScheme.fastSpatialSpec(),
    exitSpec: AnimationSpec<DpSize> = MaterialTheme.motionScheme.slowSpatialSpec(),
    startPoint: IntOffset = IntOffset(0, 0),
    color: Color = Blue,
    onTransitionMidpoint: () -> Unit = {},
    onTransitionFinished: () -> Unit = {},
) {
    val sizeAnimation = remember {
        Animatable(DpSize(0.dp, 0.dp), DpSizeToVector)
    }
    BoxWithConstraints(
        modifier.fillMaxSize(),
    ) {
        val maxSize =
            max(
                this.constraints.maxWidth.dp,
                this.constraints.maxHeight.dp,
            )
        val targetSize = DpSize(
            maxSize * 1.1f,
            maxSize * 1.1f,
        )
        LaunchedEffect(startPoint) {
            sizeAnimation.animateTo(targetSize, enterSpec)
            onTransitionMidpoint()
            sizeAnimation.animateTo(DpSize.Zero, exitSpec)
            onTransitionFinished()
        }
        val currentSize = sizeAnimation.value
        Box(
            modifier = Modifier
                .size(currentSize)
                .drawBehind {
                    translate(startPoint.x.toFloat(), startPoint.y.toFloat()) {
                        translate(-size.width / 2f, -size.height / 2f) {
                            drawCircle(
                                color = color,
                                radius = currentSize.width.value,
                            )
                        }
                    }
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorSplashPreview() {
    AndroidifyTheme {
        var buttonBounds by remember {
            mutableStateOf<RelativeLayoutBounds?>(null)
        }
        var showColorSplash by remember {
            mutableStateOf(false)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            PrimaryButton(
                buttonText = "Go",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onLayoutRectChanged(
                        callback = { bounds ->
                            buttonBounds = bounds
                        },
                    ),
                onClick = {
                    showColorSplash = true
                },
            )
        }
        if (showColorSplash) {
            ColorSplashTransitionScreen(
                modifier = Modifier.fillMaxSize(),
                startPoint = buttonBounds?.boundsInRoot?.center ?: IntOffset.Zero,
                onTransitionFinished = {
                    showColorSplash = false
                },
            )
        }
    }
}

private val DpSizeToVector: TwoWayConverter<DpSize, AnimationVector2D> =
    TwoWayConverter(
        { AnimationVector2D(it.width.value, it.height.value) },
        {
            DpSize(
                width = it.v1.fastRoundToInt().fastCoerceAtLeast(0).dp,
                height = it.v2.fastRoundToInt().fastCoerceAtLeast(0).dp,
            )
        },
    )
