package com.android.developers.androidify.home.content

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.developers.androidify.theme.R

@Composable
internal fun MainHomeContent(
    dancingBotLink: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        DecorativeSquiggleLimeGreen()
        DancingBotHeadlineText(dancingBotLink = dancingBotLink, modifier = Modifier.weight(1f))
        DecorativeSquiggleLightGreen()
    }
}

@Composable
private fun ColumnScope.DecorativeSquiggleLimeGreen() {
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
            ImageVector.vectorResource(R.drawable.decorative_squiggle),
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

@Composable
private fun DancingBotHeadlineText(
    modifier: Modifier = Modifier,
    dancingBotLink: String?,
) {
    Box(modifier = modifier) {
        val animatedBot = "animatedBot"
        val text = buildAnnotatedString {
            append(stringResource(com.android.developers.androidify.home.R.string.customize_your_own))
            // Attach "animatedBot" annotation on the placeholder
            appendInlineContent(animatedBot)
            append(stringResource(com.android.developers.androidify.home.R.string.into_an_android_bot))
        }
        var placeHolderSize by remember { mutableStateOf(220.sp) }
        val inlineContent = mapOf(
            Pair(
                first = animatedBot,
                second = InlineTextContent(
                    Placeholder(
                        width = placeHolderSize,
                        height = placeHolderSize,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                    ),
                ) {
                    AsyncImage(
                        model = dancingBotLink,
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .fillMaxSize(),
                        contentDescription = null,
                    )
                },
            ),
        )

        BasicText(
            text = text,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
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

@Composable
private fun ColumnScope.DecorativeSquiggleLightGreen() {
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
            ImageVector.vectorResource(R.drawable.decorative_squiggle_2),
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