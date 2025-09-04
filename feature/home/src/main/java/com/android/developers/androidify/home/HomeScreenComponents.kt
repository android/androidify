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
package com.android.developers.androidify.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.developers.androidify.theme.Blue
import com.android.developers.androidify.theme.R as ThemeR

@Composable
fun MainHomeContent(
    dancingBotLink: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        DecorativeSquiggleLimeGreen()
        DancingBotHeadlineText(
            dancingBotLink,
            modifier = Modifier.weight(1f),
        )
        DecorativeSquiggleLightGreen()
    }
}

@Composable
fun ColumnScope.DecorativeSquiggleLightGreen() {
    val infiniteAnimation = rememberInfiniteTransition()
    val rotationAnimation = infiniteAnimation.animateFloat(
        0f,
        720f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Image(
        painter = rememberVectorPainter(
            ImageVector.vectorResource(ThemeR.drawable.decorative_squiggle_2),
        ),
        contentDescription = null, // decorative element
        modifier = Modifier
            .padding(start = 60.dp)
            .size(60.dp)
            .align(Alignment.Start)
            .graphicsLayer {
                rotationZ = rotationAnimation.value
            },
    )
}

@Composable
fun ColumnScope.DecorativeSquiggleLimeGreen() {
    val infiniteAnimation = rememberInfiniteTransition()
    val rotationAnimation = infiniteAnimation.animateFloat(
        0f,
        -720f,
        animationSpec = infiniteRepeatable(
            tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Image(
        painter = rememberVectorPainter(
            ImageVector.vectorResource(ThemeR.drawable.decorative_squiggle),
        ),
        contentDescription = null, // decorative element
        modifier = Modifier
            .padding(end = 80.dp)
            .size(60.dp)
            .align(Alignment.End)
            .graphicsLayer {
                rotationZ = rotationAnimation.value
            },

    )
}

@Preview
@Composable
fun HomePageButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors().copy(containerColor = Blue),
    onClick: () -> Unit = {},
) {
    val style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight(700),
        letterSpacing = .15f.sp,
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
    ) {
        Text(
            stringResource(R.string.home_button_label),
            style = style,
        )
    }
}

@Composable
private fun DancingBot(
    dancingBotLink: String?,
    modifier: Modifier,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(id = R.drawable.dancing_droid_gif_placeholder),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = dancingBotLink,
            modifier = modifier,
            contentDescription = null,
        )
    }
}

@Composable
fun DancingBotHeadlineText(
    dancingBotLink: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        val animatedBot = "animatedBot"
        val text = buildAnnotatedString {
            append(stringResource(R.string.customize_your_own))
            // Attach "animatedBot" annotation on the placeholder
            appendInlineContent(animatedBot)
            append(stringResource(R.string.into_an_android_bot))
        }
        var placeHolderSize by remember {
            mutableStateOf(220.sp)
        }
        val inlineContent = mapOf(
            Pair(
                animatedBot,
                InlineTextContent(
                    Placeholder(
                        width = placeHolderSize,
                        height = placeHolderSize,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                    ),
                ) {
                    DancingBot(
                        dancingBotLink,
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .fillMaxSize(),
                    )
                },
            ),
        )
        BasicText(
            text,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            autoSize = TextAutoSize.StepBased(maxFontSize = 220.sp),
            maxLines = 5,
            onTextLayout = { result ->
                placeHolderSize = result.layoutInput.style.fontSize * 3.5f
            },
            inlineContent = inlineContent,
        )
    }
}
