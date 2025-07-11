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
package com.android.developers.androidify.customize

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageResult(
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .aspectRatio(
                    exportImageCanvas.aspectRatioOption.aspectRatio,
                    matchHeightConstraintsFirst = true,
                ),
        ) {
            BackgroundLayout(
                exportImageCanvas.aspectRatioOption,
                exportImageCanvas.selectedBackgroundOption,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (exportImageCanvas.imageBitmap != null) {
                    Image(
                        bitmap = exportImageCanvas.imageBitmap.asImageBitmap(),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundLayout(
    sizeOption: SizeOption,
    backgroundOption: BackgroundOption,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        var whiteBoxXFraction = 0.51f
        var whiteBoxYFraction = 0.31f
        var whiteBoxWidthFraction = 0.23f
        var whiteBoxHeightFraction = 0.35f
        var rotation = 0f
        when (sizeOption) {
            SizeOption.Banner -> {
                // Background image for the banner
                val image = when (backgroundOption) {
                    BackgroundOption.IO -> R.drawable.background_banner_square
                    BackgroundOption.Lightspeed -> R.drawable.background_banner_lightspeed
                    BackgroundOption.None -> R.drawable.background_banner_plain
                }
                Image(
                    bitmap = ImageBitmap.imageResource(id = image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
                whiteBoxXFraction = 0.51f
                whiteBoxYFraction = 0.31f
                whiteBoxWidthFraction = 0.23f
                whiteBoxHeightFraction = 0.35f
                rotation = -11f
            }

            SizeOption.Square -> {
                val image = when (backgroundOption) {
                    BackgroundOption.IO -> R.drawable.background_square_blocks
                    BackgroundOption.Lightspeed -> R.drawable.background_square_lightspeed
                    BackgroundOption.None -> R.drawable.background_square_none
                }
                Image(
                    bitmap = ImageBitmap.imageResource(id = image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null,
                )
                whiteBoxXFraction = 0.2f
                whiteBoxYFraction = 0.16f
                whiteBoxWidthFraction = 0.60f
                whiteBoxHeightFraction = 0.55f
                rotation = 0f
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val fullWidth = constraints.maxWidth
                    val fullHeight = constraints.maxHeight

                    val whiteBoxWidth = (fullWidth * whiteBoxWidthFraction).roundToInt()
                    val whiteBoxHeight = (fullHeight * whiteBoxHeightFraction).roundToInt()

                    val whiteBoxX = (fullWidth * whiteBoxXFraction).roundToInt()
                    val whiteBoxY = (fullHeight * whiteBoxYFraction).roundToInt()

                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = whiteBoxWidth,
                            maxWidth = whiteBoxWidth,
                            minHeight = whiteBoxHeight,
                            maxHeight = whiteBoxHeight,
                        ),
                    )
                    layout(fullWidth, fullHeight) {
                        placeable.placeRelative(whiteBoxX, whiteBoxY)
                    }
                }
                .aspectRatio(0.88f)
                .rotate(rotation),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(percent = 8)),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }

    }
}

suspend fun renderToBitmap(
    context: Context,
    currentCanvasState: ExportImageCanvas,
): Bitmap? {
    return useVirtualDisplay(context) { display ->
        val outputDensity = Density(2f)

        val logicalHeightDp = currentCanvasState.canvasSize.height.dp
        val logicalWidthDp = (currentCanvasState.canvasSize.height * currentCanvasState.aspectRatioOption.aspectRatio).dp
        
        val captureDpSize = DpSize(width = logicalWidthDp, height = logicalHeightDp)

        captureComposable(
            context = context,
            density = outputDensity,
            size = captureDpSize,
            display = display,
        ) {
            LaunchedEffect(Unit) {
                capture()
            }
            ImageResult(
                exportImageCanvas = currentCanvasState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@PhonePreview
@LargeScreensPreview
@Composable
private fun ImageRendererPreview() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = androidx.compose.ui.geometry.Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Square,
            selectedBackgroundOption = BackgroundOption.IO,
        ),
        modifier = Modifier
            .fillMaxSize(),
    )
}

@PhonePreview
@LargeScreensPreview
@Composable
private fun ImageRendererPreviewBanner() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = androidx.compose.ui.geometry.Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Banner,
            selectedBackgroundOption = BackgroundOption.Lightspeed,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.Banner.aspectRatio),
    )

}
