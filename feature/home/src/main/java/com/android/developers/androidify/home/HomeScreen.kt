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

import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import coil3.compose.AsyncImage
import com.android.developers.androidify.theme.Blue
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.AndroidifyTranslucentTopAppBar
import com.android.developers.androidify.theme.components.SquiggleBackground
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.isAtLeastMedium
import com.android.developers.androidify.theme.R as ThemeR

@ExperimentalMaterial3ExpressiveApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeViewModel = hiltViewModel(),
    isMediumWindowSize: Boolean = isAtLeastMedium(),
    onClickLetsGo: (IntOffset) -> Unit = {},
    onAboutClicked: () -> Unit = {},
) {
    val state = homeScreenViewModel.state.collectAsStateWithLifecycle()
    if (!state.value.isAppActive) {
        AppInactiveScreen()
    } else {
        HomeScreenContents(
            state.value.videoLink,
            state.value.dancingDroidLink,
            isMediumWindowSize,
            onClickLetsGo,
            onAboutClicked,
        )
    }
}

@Composable
fun HomeScreenContents(
    videoLink: String?,
    dancingBotLink: String?,
    isMediumWindowSize: Boolean,
    onClickLetsGo: (IntOffset) -> Unit,
    onAboutClicked: () -> Unit,
) {
    Box {
        SquiggleBackground()
        var positionButtonClick by remember {
            mutableStateOf(IntOffset.Zero)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            if (isMediumWindowSize) {
                AndroidifyTranslucentTopAppBar(isMediumSizeLayout = true)

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.weight(0.8f),
                    ) {
                        VideoPlayerRotatedCard(
                            videoLink,
                            modifier = Modifier
                                .padding(32.dp)
                                .align(Alignment.Center),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .align(Alignment.CenterVertically),
                    ) {
                        MainHomeContent(dancingBotLink)
                        HomePageButton(
                            modifier = Modifier
                                .onLayoutRectChanged {
                                    positionButtonClick = it.boundsInWindow.center
                                }
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .height(64.dp)
                                .width(220.dp),
                            onClick = {
                                onClickLetsGo(positionButtonClick)
                            },
                        )
                    }
                }
            } else {
                CompactPager(
                    videoLink,
                    dancingBotLink,
                    onClickLetsGo,
                    onAboutClicked,
                )
            }
        }
    }
}

@Composable
private fun CompactPager(
    videoLink: String?,
    dancingBotLink: String?,
    onClick: (IntOffset) -> Unit,
    onAboutClicked: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidifyTopAppBar(
            aboutEnabled = true,
            onAboutClicked = onAboutClicked,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(.8f),
            beyondViewportPageCount = 1,
        ) { page ->
            if (page == 0) {
                MainHomeContent(
                    dancingBotLink = dancingBotLink,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally),
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoPlayerRotatedCard(
                        videoLink = videoLink,
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .align(Alignment.Center),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(.1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val isCurrent by remember { derivedStateOf<Boolean> { pagerState.currentPage == iteration } }
                val animatedColor by animateColorAsState(
                    if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onTertiary,
                    label = "animatedFirstColor",
                )

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(size = 16.dp))
                        .animateContentSize()
                        .background(
                            color = animatedColor,
                            shape = RoundedCornerShape(size = 16.dp),
                        )
                        .height(16.dp)
                        .width(if (isCurrent) 40.dp else 16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        var buttonPosition by remember {
            mutableStateOf(IntOffset.Zero)
        }
        HomePageButton(
            modifier = Modifier
                .onLayoutRectChanged {
                    buttonPosition = it.boundsInWindow.center
                }
                .padding(bottom = 16.dp)
                .height(64.dp)
                .width(220.dp),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = Blue),
            onClick = {
                onClick(buttonPosition)
            },
        )
    }
}

@Composable
private fun VideoPlayerRotatedCard(
    videoLink: String?,
    modifier: Modifier = Modifier,
) {
    val aspectRatio = 280f / 380f
    val videoInstructionText = stringResource(R.string.instruction_video_transcript)
    Box(
        modifier = modifier
            .focusable()
            .semantics { contentDescription = videoInstructionText }
            .aspectRatio(aspectRatio)
            .rotate(-3f)
            .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
            .background(
                color = Color.White,
                shape = MaterialTheme.shapes.large,
            ),
    ) {
        VideoPlayer(
            videoLink,
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .align(Alignment.Center)
                .clip(MaterialTheme.shapes.large)
                .clipToBounds(),
        )
    }
}

@ExperimentalMaterial3ExpressiveApi
@PhonePreview
@Composable
private fun HomeScreenPhonePreview() {
    SharedElementContextPreview {
        HomeScreenContents(
            isMediumWindowSize = false,
            onClickLetsGo = {},
            videoLink = "",
            dancingBotLink = "https://services.google.com/fh/files/misc/android_dancing.gif",
            onAboutClicked = {},
        )
    }
}

@ExperimentalMaterial3ExpressiveApi
@LargeScreensPreview
@Composable
private fun HomeScreenLargeScreensPreview() {
    SharedElementContextPreview {
        HomeScreenContents(
            isMediumWindowSize = true,
            onClickLetsGo = { },
            videoLink = "",
            dancingBotLink = "https://services.google.com/fh/files/misc/android_dancing.gif",
            onAboutClicked = {},
        )
    }
}

@Composable
private fun MainHomeContent(
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
private fun HomePageButton(
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
    AsyncImage(
        model = dancingBotLink,
        modifier = modifier,
        contentDescription = null,
    )
}

@Composable
private fun DancingBotHeadlineText(
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

@OptIn(UnstableApi::class) // New Media3 Compose artifact is currently experimental
@Composable
private fun VideoPlayer(
    videoLink: String?,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) return // Layoutlib does not support ExoPlayer

    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }
    LifecycleStartEffect(videoLink) {
        if (videoLink != null) {
            player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoLink))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
        }
        onStopOrDispose {
            player?.release()
            player = null
        }
    }

    var videoFullyOnScreen by remember { mutableStateOf(false) }
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .onVisibilityChanged(
                containerWidth = LocalView.current.width,
                containerHeight = LocalView.current.height,
            ) { fullyVisible -> videoFullyOnScreen = fullyVisible }
            .then(modifier),
    ) {
        player?.let { currentPlayer ->
            LaunchedEffect(videoFullyOnScreen) {
                if (videoFullyOnScreen) currentPlayer.play() else currentPlayer.pause()
            }

            // Render the video
            PlayerSurface(currentPlayer, surfaceType = SURFACE_TYPE_TEXTURE_VIEW)

            // Show a play / pause button
            val playPauseButtonState = rememberPlayPauseButtonState(currentPlayer)
            OutlinedIconButton(
                onClick = playPauseButtonState::onClick,
                enabled = playPauseButtonState.isEnabled,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            ) {
                val icon =
                    if (playPauseButtonState.showPlay) R.drawable.rounded_play_arrow_24 else R.drawable.rounded_pause_24
                val contentDescription =
                    if (playPauseButtonState.showPlay) R.string.play else R.string.pause
                Icon(
                    painterResource(icon),
                    stringResource(contentDescription),
                )
            }
        }
    }
}

fun Modifier.onVisibilityChanged(
    containerWidth: Int,
    containerHeight: Int,
    onChanged: (visible: Boolean) -> Unit,
) = this then Modifier.onLayoutRectChanged(100, 0) { layoutBounds ->
    onChanged(
        layoutBounds.boundsInRoot.top > 0 &&
            layoutBounds.boundsInRoot.bottom < containerHeight &&
            layoutBounds.boundsInRoot.left > 0 &&
            layoutBounds.boundsInRoot.right < containerWidth,
    )
}
