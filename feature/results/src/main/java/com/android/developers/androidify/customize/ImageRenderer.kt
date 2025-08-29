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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)

package com.android.developers.androidify.customize

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.fastRoundToInt
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LocalAnimateBoundsScope
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImageResult(
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
    outerChromeModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(
                    exportImageCanvas.aspectRatioOption.aspectRatio,
                    matchHeightConstraintsFirst = true,
                )
                .then(Modifier.safeAnimateBounds())
                .then(outerChromeModifier)
                .clipToBounds(),
        ) {
            BackgroundLayout(
                exportImageCanvas,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (exportImageCanvas.showSticker &&
                    exportImageCanvas.imageBitmapRemovedBackground != null
                ) {
                    Image(
                        bitmap = exportImageCanvas.imageBitmapRemovedBackground.asImageBitmap(),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                } else if (exportImageCanvas.imageWithEdit != null) {
                    Image(
                        bitmap = exportImageCanvas.imageWithEdit.asImageBitmap(),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                } else if (exportImageCanvas.imageBitmap != null) {
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
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backgroundModifier = if (exportImageCanvas.backgroundColor != null) {
        Modifier.background(exportImageCanvas.backgroundColor)
    } else {
        Modifier
    }
    Box(
        modifier = modifier.fillMaxSize()
            .then(backgroundModifier),
    ) {
        if (exportImageCanvas.selectedBackgroundDrawable != null) {
            Image(
                bitmap = ImageBitmap.imageResource(id = exportImageCanvas.selectedBackgroundDrawable),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        val rotationAnimation by animateFloatAsState(
            targetValue = exportImageCanvas.imageRotation,
            label = "rotation",
            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        )
        val safeAnimateBounds = Modifier.safeAnimateBounds()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val offsetValue = exportImageCanvas.imageOffset
                    val imageSizeValue = exportImageCanvas.imageSize
                    val exportCanvasSizeAnimation = exportImageCanvas.canvasSize

                    val actualWidth = constraints.maxWidth
                    val actualHeight = constraints.maxHeight

                    val scale = if (exportCanvasSizeAnimation.width > 0f) {
                        actualWidth / exportCanvasSizeAnimation.width
                    } else {
                        1f
                    }

                    val scaledImageWidth = imageSizeValue.width * scale
                    val scaledImageHeight = imageSizeValue.height * scale
                    val scaledOffsetX = offsetValue.x * scale
                    val scaledOffsetY = offsetValue.y * scale

                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = scaledImageWidth.fastRoundToInt(),
                            maxWidth = scaledImageWidth.fastRoundToInt(),
                            minHeight = scaledImageHeight.fastRoundToInt(),
                            maxHeight = scaledImageHeight.fastRoundToInt(),
                        ),
                    )
                    layout(actualWidth, actualHeight) {
                        placeable.placeRelative(scaledOffsetX.fastRoundToInt(), scaledOffsetY.fastRoundToInt())
                    }
                }
                .then(safeAnimateBounds)
                .rotate(rotationAnimation),
        ) {
            val clip = if (exportImageCanvas.selectedBackgroundOption == BackgroundOption.None ||
                exportImageCanvas.selectedBackgroundOption.aiBackground
            ) {
                Modifier
            } else {
                Modifier.clip(RoundedCornerShape(6))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(clip),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun Modifier.safeAnimateBounds(): Modifier {
    val spec = MaterialTheme.motionScheme.slowEffectsSpec<Rect>()
    return if (LocalAnimateBoundsScope.current != null) {
        this.animateBounds(
            LocalAnimateBoundsScope.current!!,
            boundsTransform = { _, _ ->
                spec
            },
        )
    } else {
        this
    }
}

@Preview
@Composable
private fun ImageRendererPreviewSquare() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    AndroidifyTheme {
        ImageResult(
            ExportImageCanvas(
                imageBitmap = bitmap.asAndroidBitmap(),
                canvasSize = Size(1000f, 1000f),
                aspectRatioOption = SizeOption.Square,
                selectedBackgroundOption = BackgroundOption.IO,
            )
                .updateAspectRatioAndBackground(
                    backgroundOption = BackgroundOption.IO,
                    sizeOption = SizeOption.Square,
                ),
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun ImageRendererPreviewBanner() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
    AndroidifyTheme {
        ImageResult(
            ExportImageCanvas(
                imageBitmap = bitmap.asAndroidBitmap(),
                canvasSize = Size(1000f, 1000f),
                aspectRatioOption = SizeOption.Banner,
                selectedBackgroundOption = BackgroundOption.Lightspeed,
            ).updateAspectRatioAndBackground(
                backgroundOption = BackgroundOption.Lightspeed,
                sizeOption = SizeOption.Banner,
            ),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(SizeOption.Banner.aspectRatio),
        )
    }
}

@Preview
@Composable
private fun ImageRendererPreviewWallpaper() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
    AndroidifyTheme {
        ImageResult(
            ExportImageCanvas(
                imageBitmap = bitmap.asAndroidBitmap(),
                canvasSize = Size(1000f, 1000f),
                aspectRatioOption = SizeOption.Wallpaper,
                selectedBackgroundOption = BackgroundOption.Lightspeed,
            ).updateAspectRatioAndBackground(
                backgroundOption = BackgroundOption.Lightspeed,
                sizeOption = SizeOption.Wallpaper,
            ),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(SizeOption.Wallpaper.aspectRatio),
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun ImageRendererPreviewWallpaperTablet() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
    AndroidifyTheme {
        ImageResult(
            ExportImageCanvas(
                imageBitmap = bitmap.asAndroidBitmap(),
                canvasSize = Size(1280f, 800f),
                aspectRatioOption = SizeOption.WallpaperTablet,
                selectedBackgroundOption = BackgroundOption.Lightspeed,
            ).updateAspectRatioAndBackground(
                backgroundOption = BackgroundOption.Lightspeed,
                sizeOption = SizeOption.WallpaperTablet,
            ),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(SizeOption.WallpaperTablet.aspectRatio),
        )
    }
}

@Preview
@Composable
private fun ImageRendererPreviewWallpaperSocial() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
    AndroidifyTheme {
        ImageResult(
            ExportImageCanvas(
                imageBitmap = bitmap.asAndroidBitmap(),
                canvasSize = Size(1600f, 900f),
                aspectRatioOption = SizeOption.SocialHeader,
                selectedBackgroundOption = BackgroundOption.Lightspeed,
            ).updateAspectRatioAndBackground(
                backgroundOption = BackgroundOption.Lightspeed,
                sizeOption = SizeOption.SocialHeader,
            ),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(SizeOption.SocialHeader.aspectRatio),
        )
    }
}

@Preview
@Composable
fun ImageRendererPreviewWallpaperIO() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
    AndroidifyTheme {
        ImageResult(
            ExportImageCanvas(
                imageBitmap = bitmap.asAndroidBitmap(),
                canvasSize = Size(1600f, 900f),
                aspectRatioOption = SizeOption.SocialHeader,
                selectedBackgroundOption = BackgroundOption.IO,

            ).updateAspectRatioAndBackground(
                backgroundOption = BackgroundOption.IO,
                sizeOption = SizeOption.SocialHeader,
            ),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(SizeOption.SocialHeader.aspectRatio),
        )
    }
}
