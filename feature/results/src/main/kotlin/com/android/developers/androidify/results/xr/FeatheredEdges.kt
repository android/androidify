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

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.Primary

/**
 * Applies an edge feathering effect to the [ContentDrawScope].
 *
 * This behavior is a little incorrect around the corners, which have alpha transparency applied
 * twice over the area that is affected by the horizontal and vertical rectangles.
 */
fun ContentDrawScope.featheredEdges(edgeSize: Size) {
    drawContent()

    drawRect(
        topLeft = Offset(0f, 0f),
        size = Size(edgeSize.width, size.height),
        brush =
        Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = 0f,
            endX = edgeSize.width,
        ),
        blendMode = BlendMode.DstIn,
    )
    drawRect(
        topLeft = Offset(size.width - edgeSize.width, 0f),
        size = Size(edgeSize.width, size.height),
        brush =
        Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = size.width,
            endX = size.width - edgeSize.width,
        ),
        blendMode = BlendMode.DstIn,
    )

    drawRect(
        topLeft = Offset(0f, 0f),
        size = Size(size.width, edgeSize.height),
        brush =
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startY = 0f,
            endY = edgeSize.height,
        ),
        blendMode = BlendMode.DstIn,
    )

    drawRect(
        topLeft = Offset(0f, size.height - edgeSize.height),
        size = Size(size.width, edgeSize.height),
        brush =
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startY = size.width,
            endY = size.width - edgeSize.height,
        ),
        blendMode = BlendMode.DstIn,
    )
}

fun Modifier.featheredEdges(edgeSize: DpSize) = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithContent { featheredEdges(edgeSize.toSize()) }

/**
 * Feathers the edges.
 *
 * The size is expressed between [0, 1], where 0 represents 0 feathering,
 * and 1 represents a full feathering from edge to edge.
 */
fun Modifier.featheredEdges(edgeSizeFraction: Size) = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithContent {
    featheredEdges(Size(size.width * edgeSizeFraction.width, size.height * edgeSizeFraction.height))
}

@Preview
@Composable
private fun FeatheredEdgePreview() {
    Box(
        Modifier
            .size(200.dp)
            .background(Primary)
            .featheredEdges(DpSize(10.dp, 10.dp)),
    ) {
    }
}

@Preview
@Composable
private fun FeatheredEdgeAnimatedPreview() {
    Box(
        Modifier
            .size(200.dp)
            .background(Primary),
    ) {
        Box(
            Modifier
                .featheredEdges(DpSize(100.dp, 0.dp))
                .fillMaxSize(),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .basicMarquee(Int.MAX_VALUE),
                text = "Test marquee with feathered edges!",
                maxLines = 1,
            )
        }
    }
}
