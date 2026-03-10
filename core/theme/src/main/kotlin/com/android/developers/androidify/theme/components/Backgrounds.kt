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
package com.android.developers.androidify.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.R
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.dpToPx
import com.android.developers.androidify.util.isWidthAtLeastMedium

@Composable
fun SquiggleBackground(
    modifier: Modifier = Modifier,
    offsetHeightFraction: Float = 0f,
    isMediumWindowSize: Boolean = isWidthAtLeastMedium(),
) {
    val vectorBackground =
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.squiggle))
    val verticalPadding = if (!isMediumWindowSize) 64.dp else 0.dp

    BoxWithConstraints(
        modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary),
    ) {
        val maxHeight = this@BoxWithConstraints.maxHeight.dpToPx()
        Image(
            painter = vectorBackground,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = if (isMediumWindowSize) 1.3f else 1f
                    scaleY = if (isMediumWindowSize) 1.3f else 1f
                }
                .offset {
                    IntOffset(0, y = (maxHeight * offsetHeightFraction).toInt())
                }
                .padding(top = verticalPadding, bottom = verticalPadding),
            contentScale = ContentScale.FillHeight,
        )
    }
}

@LargeScreensPreview
@Composable
private fun SquiggleBackgroundLargePreview() {
    AndroidifyTheme {
        SquiggleBackground(modifier = Modifier.fillMaxSize(), offsetHeightFraction = 0.5f)
    }
}

@PhonePreview
@Composable
private fun SquiggleBackgroundPreview() {
    AndroidifyTheme {
        SquiggleBackground(modifier = Modifier.fillMaxSize(), offsetHeightFraction = 0.5f)
    }
}

@LargeScreensPreview
@Composable
private fun ResultsBackgroundLargePreview() {
    AndroidifyTheme {
        ResultsBackground(isMediumWindowSize = true, modifier = Modifier.fillMaxSize())
    }
}

@PhonePreview
@Composable
private fun ResultsBackgroundPhonePreview() {
    AndroidifyTheme {
        ResultsBackground(isMediumWindowSize = false, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ResultsBackground(
    modifier: Modifier = Modifier,
    isMediumWindowSize: Boolean = isWidthAtLeastMedium(),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.primary)
            .verticalMarquee(
                iterations = Int.MAX_VALUE,
                repeatDelayMillis = 0,
                spacing = MarqueeSpacing(0.dp),
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            repeat(10) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(if (isMediumWindowSize) 300.dp else 175.dp)
                        .graphicsLayer {
                            scaleX = 1.01f
                        }
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(50),
                        ),
                )
            }
        }
    }
}
