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
package com.android.developers.androidify.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathSegment
import androidx.core.graphics.flatten

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Modifier.backgroundRepeatX(
    painter: Painter,
    desiredTileWidth: Float = painter.intrinsicSize.width,
) = this.then(
    Modifier.drawWithContent {
        drawRepeatX(painter, desiredTileWidth)
        drawContent()
    },
)

private fun DrawScope.drawRepeatX(painter: Painter, desiredTileWidth: Float) {
    val imageWidth = desiredTileWidth
    val imageHeight = painter.intrinsicSize.height
    val canvasWidth = size.width
    var x = 0f
    while (x < canvasWidth) {
        with(painter) {
            translate(left = x) {
                draw(
                    size = Size(width = imageWidth, height = imageHeight),
                )
            }
        }
        x += imageWidth
    }
}

fun Modifier.backgroundRepeatY(
    painter: Painter,
    desiredTileHeight: Float = painter.intrinsicSize.height,
) = this.then(
    Modifier.drawWithContent {
        drawRepeatY(painter, desiredTileHeight)
        drawContent()
    },
)

private fun DrawScope.drawRepeatY(painter: Painter, desiredTileHeight: Float) {
    val imageHeight = desiredTileHeight
    val imageWidth = painter.intrinsicSize.width
    val canvasHeight = size.height
    var y = 0f
    val ratio = imageHeight / imageWidth
    val itemHeight = size.width * ratio
    while (y < canvasHeight) {
        with(painter) {
            translate(top = y) {
                draw(
                    size = Size(width = size.width, height = itemHeight),
                )
            }
        }
        y += itemHeight
    }
}

@Composable
fun Modifier.dashedRoundedRectBorder(
    width: Dp,
    color: Color,
    cornerRadius: Dp,
    intervals: FloatArray = floatArrayOf(10f.dp.dpToPx(), 10f.dp.dpToPx()),
    phase: Float = 0f,
): Modifier =
    this.then(
        Modifier.drawBehind {
            val stroke = Stroke(
                width = width.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(intervals, phase),
            )
            drawRoundRect(
                color,
                style = stroke,
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
            )
        },
    )

fun Path.flatten(error: Float = 0.5f): Iterable<PathSegment> {
    return this.asAndroidPath().flatten(error)
}
