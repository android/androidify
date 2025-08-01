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

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.unit.dp
import kotlin.math.max

private val ColorScheme.highlightLoading: Color
    get() =
        Color(0xFFAB9FF6)

@Composable
fun Modifier.loadingShimmerOverlay(
    visible: Boolean,
    highlightColor: Color = MaterialTheme.colorScheme.highlightLoading,
    clipShape: Shape = RoundedCornerShape(8.dp),
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(1500),
    ),
): Modifier {
    var highlightProgress: Float by remember { mutableFloatStateOf(0f) }
    if (visible) {
        val infiniteTransition = rememberInfiniteTransition()
        highlightProgress = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = animationSpec,
        ).value
    }
    return drawWithCache {
        if (visible) {
            val brush = Brush.radialGradient(
                colors = listOf(
                    highlightColor.copy(alpha = 0f),
                    highlightColor.copy(alpha = 0.8f),
                    highlightColor.copy(alpha = 0f),
                ),
                center = Offset(x = 0f, y = 0f),
                radius = (max(size.width, size.height) * highlightProgress * 2.5f).coerceAtLeast(0.01f),
            )
            onDrawWithContent {
                drawContent()
                val outline = clipShape.createOutline(size, layoutDirection, this)

                drawOutline(
                    outline = outline,
                    brush = brush,
                )
            }
        } else {
            onDrawWithContent {
                drawContent()
            }
        }
    }
}
