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
package com.android.developers.androidify.creation

import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_3A_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.PrimaryButton
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.SmallPhonePreview
import com.android.developers.androidify.util.isAtLeastMedium
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.android.developers.androidify.creation.R as CreationR

@Composable
fun LoadingScreen(
    onCancelPress: () -> Unit,
    isMediumScreen: Boolean = isAtLeastMedium(),
) {
    Scaffold(
        topBar = {
            AndroidifyTopAppBar(isMediumWindowSize = isMediumScreen, aboutEnabled = false)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeContentPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                PrimaryButton(
                    onClick = {
                        onCancelPress()
                    },
                    buttonText = stringResource(CreationR.string.cancel),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .fillMaxSize()
            .keepScreenOn(),
    ) { contentPadding ->
        LoadingScreenContents(contentPadding)
    }
}

@Composable
private fun LoadingScreenContents(
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box {
                DecorativeSparkleDarkGreen(modifier = Modifier)
                AndroidOutlineAnimation(modifier = Modifier.align(Alignment.Center))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(com.android.developers.androidify.creation.R.string.generating_your_bot),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
        )

        LoopingDescriptiveText(
            modifier = Modifier
                .padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun LoopingDescriptiveText(modifier: Modifier = Modifier) {
    var currentTextIndex by remember { mutableIntStateOf(0) }
    val listLoadingText = stringArrayResource(CreationR.array.generation_prompts)
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentTextIndex = (currentTextIndex + 1) % listLoadingText.size
        }
    }
    Text(
        listLoadingText[currentTextIndex],
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = modifier,
        maxLines = 3,
        minLines = 3,
    )
}

@Composable
private fun BoxScope.DecorativeSparkleDarkGreen(modifier: Modifier = Modifier) {
    val infiniteAnimation = rememberInfiniteTransition()
    val rotationAnimation = infiniteAnimation.animateFloat(
        0f,
        720f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseOutQuint),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Image(
        painter = rememberVectorPainter(
            ImageVector.vectorResource(CreationR.drawable.gemini_24dp),
        ),
        contentDescription = null, // decorative element
        modifier = modifier
            .padding(start = 60.dp)
            .size(60.dp)
            .align(Alignment.TopEnd)
            .graphicsLayer {
                rotationZ = rotationAnimation.value
            },
    )
}

@LargeScreensPreview
@Composable
fun LoadingScreenLargePreview() {
    AndroidifyTheme {
        LoadingScreen(onCancelPress = {})
    }
}

@PreviewScreenSizes
@SmallPhonePreview
@Preview(device = PIXEL_3A_XL, name = "400x500", heightDp = 400, widthDp = 500)
@Composable
fun LoadingScreenPreview() {
    AndroidifyTheme {
        LoadingScreen(onCancelPress = {}, isMediumScreen = false)
    }
}

@Preview(showBackground = true)
@Composable
fun AndroidifyOutlinePreview() {
    AndroidifyTheme {
        AndroidOutlineAnimation()
    }
}

private val BOT_OUTLINE = """
        M108.881 4.08367C106.972 4.36782 105.211 5.35189 104.186 6.17186C104.13 6.21637 104.067 6.25148 104 6.27583C101.453 7.20173 100.506 9.298 100.389 11.7118C100.27 14.1409 101.013 16.7724 101.748 18.4605L117.446 47.8787C117.617 48.1983 117.495 48.5955 117.175 48.7649C103.994 55.7389 93.2161 69.9382 85.0915 84.9554C80.0946 94.1913 76.1215 103.699 73.2229 111.947C70.7993 118.844 75.3562 125.997 82.5491 127.321L92.7837 129.206C93.149 129.273 93.4076 129.6 93.3885 129.971C93.3692 130.342 93.0781 130.641 92.7079 130.67L74.3719 132.117C71.3864 132.352 69.0838 134.843 69.0838 137.836V150.902C69.0838 151.499 68.3501 151.784 67.9468 151.344C66.7837 150.076 64.7069 148.223 62.2914 146.823C59.859 145.412 57.196 144.524 54.8114 145.001L54.7967 145.004C50.4092 145.778 47.2628 147.009 45.1204 148.217C43.8282 148.946 42.9157 149.657 42.3149 150.244C41.7287 150.816 41.3806 151.571 41.0865 152.335L5.55252 244.676C4.82637 246.969 4.41884 250.748 5.57741 254.574C6.7306 258.381 9.44224 262.262 15.0242 264.8C22.6079 268.243 29.0364 268.017 33.3121 266.877C36.3562 266.064 38.2988 263.358 39.2823 260.366L67.6056 174.21C67.7219 173.857 68.0779 173.64 68.4456 173.699C68.8134 173.758 69.0838 174.075 69.0838 174.447V241.813C69.8459 250.264 72.4512 263.758 77.7898 276.758C83.1385 289.785 91.1884 302.211 102.774 308.65C103.807 309.174 105.74 310.891 105.74 313.807V367.258C105.74 372.722 108.868 377.781 114.111 379.332C124.995 382.554 141.32 384.649 157.683 379.481C162.875 377.841 165.962 372.745 165.962 367.302V337.36C165.962 332.663 169.772 328.856 174.472 328.856C179.172 328.856 182.982 332.663 182.982 337.36V367.302C182.982 372.745 186.068 377.841 191.261 379.481C207.623 384.649 223.948 382.554 234.833 379.332C240.075 377.781 243.203 372.722 243.203 367.258V313.807C243.203 310.891 245.136 309.174 246.171 308.65C257.755 302.211 265.805 289.785 271.154 276.758C276.493 263.758 279.097 250.264 279.861 241.813V174.447C279.861 174.075 280.13 173.758 280.499 173.699C280.865 173.64 281.221 173.857 281.338 174.21L309.662 260.366C310.645 263.358 312.587 266.064 315.632 266.877C319.907 268.017 326.336 268.243 333.92 264.8C339.501 262.262 342.213 258.381 343.366 254.574C344.525 250.748 344.118 246.969 343.391 244.676L307.857 152.335C307.563 151.571 307.216 150.816 306.629 150.244C306.029 149.657 305.116 148.946 303.823 148.217C301.681 147.009 298.535 145.778 294.147 145.004L294.133 145.001C291.748 144.524 289.085 145.412 286.653 146.823C284.237 148.223 282.16 150.076 280.996 151.344C280.594 151.784 279.861 151.499 279.861 150.902V137.836C279.861 134.843 277.558 132.352 274.573 132.117L256.235 130.67C255.867 130.641 255.574 130.342 255.555 129.971C255.535 129.6 255.796 129.273 256.161 129.206L266.395 127.321C273.588 125.997 278.144 118.844 275.721 111.947C272.822 103.699 268.849 94.1913 263.853 84.9554C255.728 69.9382 244.95 55.7389 231.77 48.7649C231.449 48.5955 231.327 48.1983 231.499 47.8787L247.197 18.4605C247.932 16.7724 248.674 14.1409 248.555 11.7118C248.438 9.298 247.491 7.20173 244.943 6.27583C244.876 6.25148 244.814 6.21635 244.759 6.17186C243.733 5.35189 241.972 4.36782 240.062 4.08367C238.509 3.85242 236.872 4.0822 235.414 5.24491C234.803 5.73132 234.356 6.38579 233.942 7.0467L213.646 39.3747C213.499 39.6098 213.22 39.7261 212.948 39.665C206.209 38.1441 188.543 35.1014 174.472 35.1014C160.4 35.1014 142.735 38.1441 135.996 39.665C135.725 39.7261 135.445 39.6098 135.297 39.3747L115.002 7.0467C114.587 6.38577 114.141 5.73134 113.531 5.24493C112.072 4.0822 110.434 3.85242 108.881 4.08367Z
""".trimIndent()

@Composable
private fun AndroidOutlineAnimation(modifier: Modifier = Modifier) {
    val pathParser = remember {
        PathParser().parsePathString(BOT_OUTLINE)
    }
    val nodes = remember {
        pathParser.toNodes()
    }
    val pathBounds = remember {
        pathParser.toPath().getBounds()
    }
    val duration = 20000
    val transition = rememberInfiniteTransition()
    val trimOffsetStart by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.01f,
        animationSpec = infiniteRepeatable(
            tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val strokeWidth = 2.dp
    val strokeWidthPx = with(LocalDensity.current) {
        strokeWidth.toPx()
    }
    val color = SolidColor(MaterialTheme.colorScheme.primary)
    val vectorPainter = rememberVectorPainter(
        defaultWidth = pathBounds.width.roundToInt().dp,
        defaultHeight = pathBounds.height.roundToInt().dp,
        viewportHeight = pathBounds.height + 20f,
        viewportWidth = pathBounds.width + 20f,
        autoMirror = false,
    ) { _, _ ->
        Group(name = "android") {
            Path(
                pathData = nodes,
                stroke = color,
                strokeLineWidth = strokeWidthPx,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.3f,
            )
        }
        Group(name = "animated-droid") {
            Path(
                pathData = nodes,
                trimPathStart = trimOffsetStart,
                stroke = color,
                strokeLineWidth = strokeWidthPx,
                strokeLineCap = StrokeCap.Round,
            )
        }
    }
    Box(
        modifier = modifier
            .padding(64.dp)
            .width(197.dp)
            .height(218.dp)
            .paint(vectorPainter),
    )
}
