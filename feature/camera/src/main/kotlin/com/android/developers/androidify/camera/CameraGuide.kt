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
package com.android.developers.androidify.camera

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LimeGreen
import com.android.developers.androidify.theme.R
import com.android.developers.androidify.util.dashedRoundedRectBorder
import com.android.developers.androidify.util.dpToPx

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CameraGuide(
    detectedPose: Boolean,
    modifier: Modifier = Modifier,
    defaultAspectRatio: Float = 9f / 16f,
) {
    Crossfade(
        detectedPose,
        modifier,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
    ) {
        if (it) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(28.dp),
                    ),
            ) {
                DecorativeSquiggle(
                    color = LimeGreen,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 20.dp),
                )
                DecorativeSquiggle(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 27.dp, vertical = 128.dp),
                    alignment = Alignment.BottomStart,
                )
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .dashedRoundedRectBorder(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        cornerRadius = 28.dp,
                        intervals = floatArrayOf(10.dp.dpToPx(), 10.dp.dpToPx()),
                    ),
            )
        }
    }
}

@Composable
private fun BoxScope.DecorativeSquiggle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    alignment: Alignment = Alignment.TopEnd,
) {
    val infiniteAnimation = rememberInfiniteTransition()
    val rotationAnimation = infiniteAnimation.animateFloat(
        0f,
        720f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Icon(
        painter = rememberVectorPainter(
            ImageVector.vectorResource(R.drawable.decorative_squiggle),
        ),
        tint = color,
        contentDescription = null, // decorative element
        modifier = modifier
            .size(60.dp)
            .align(alignment)
            .graphicsLayer {
                rotationZ = rotationAnimation.value
            },
    )
}

@Preview(showBackground = true)
@Composable
private fun CameraGuidePreview_DetectedPose() {
    AndroidifyTheme {
        CameraGuide(true)
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraGuidePreview_NoDetectedPose() {
    AndroidifyTheme {
        CameraGuide(false)
    }
}
