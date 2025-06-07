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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.developers.androidify.home.component.CompactPager
import com.android.developers.androidify.home.component.HomePageButton
import com.android.developers.androidify.home.component.VideoPlayerRotatedCard
import com.android.developers.androidify.home.content.InactiveAppHomeContent
import com.android.developers.androidify.home.content.MainHomeContent
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.components.AndroidifyTranslucentTopAppBar
import com.android.developers.androidify.theme.components.SquiggleBackground
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.isAtLeastMedium

@ExperimentalMaterial3ExpressiveApi
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeViewModel = hiltViewModel(),
    isMediumWindowSize: Boolean = isAtLeastMedium(),
    onClickLetsGo: (IntOffset) -> Unit,
    onAboutClicked: () -> Unit,
) {
    val state by homeScreenViewModel.state.collectAsStateWithLifecycle()

    if (state.isAppActive) {
        HomeScreenContents(
            videoLink = state.videoLink,
            dancingBotLink = state.dancingDroidLink,
            isMediumWindowSize = isMediumWindowSize,
            onClickLetsGo = onClickLetsGo,
            onAboutClicked = onAboutClicked,
        )
    } else {
        InactiveAppHomeContent()
    }
}

@Composable
internal fun HomeScreenContents(
    videoLink: String?,
    dancingBotLink: String?,
    isMediumWindowSize: Boolean,
    onClickLetsGo: (IntOffset) -> Unit,
    onAboutClicked: () -> Unit,
) {
    var positionButtonClick by remember { mutableStateOf(IntOffset.Zero) }

    Box {
        SquiggleBackground()

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
                            videoLink = videoLink,
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
                        MainHomeContent(dancingBotLink = dancingBotLink)
                        HomePageButton(
                            modifier = Modifier
                                .onLayoutRectChanged {
                                    positionButtonClick = it.boundsInWindow.center
                                }
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .height(64.dp)
                                .width(220.dp),
                            onClick = { onClickLetsGo(positionButtonClick) },
                        )
                    }
                }
            } else {
                CompactPager(
                    videoLink = videoLink,
                    dancingBotLink = dancingBotLink,
                    onClick = onClickLetsGo,
                    onAboutClicked = onAboutClicked,
                )
            }
        }
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
