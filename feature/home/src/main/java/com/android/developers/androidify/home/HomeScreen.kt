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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.components.SquiggleBackground
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.isAtLeastMedium

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            if (isMediumWindowSize) {
                HomeScreenMediumContents(Modifier.weight(1f), videoLink, dancingBotLink, onClickLetsGo)
            } else {
                HomeScreenCompactPager(
                    videoLink,
                    dancingBotLink,
                    onClickLetsGo,
                    onAboutClicked,
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
