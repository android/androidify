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
package com.android.developers.androidify.home.xr

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import com.android.developers.androidify.home.HomeScreenContents
import com.android.developers.androidify.home.HomeScreenLayoutType
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.xr.SupportsFullSpaceModeRequestProvider
import com.android.developers.androidify.xr.XrHomeSpaceCompactPreview
import com.android.developers.androidify.xr.XrHomeSpaceMediumPreview

@XrHomeSpaceMediumPreview
@ExperimentalMaterial3ExpressiveApi
@Composable
private fun HomeScreenMediumXrHomeSpaceModePreview() {
    SharedElementContextPreview {
        SupportsFullSpaceModeRequestProvider {
            HomeScreenContents(
                layoutType = HomeScreenLayoutType.Medium,
                onClickLetsGo = { },
                videoLink = "",
                dancingBotLink = "https://services.google.com/fh/files/misc/android_dancing.gif",
                onAboutClicked = {},
            )
        }
    }
}

@ExperimentalMaterial3ExpressiveApi
@XrHomeSpaceCompactPreview
@Composable
private fun HomeScreenCompactXrHomeSpaceModePreview() {
    SupportsFullSpaceModeRequestProvider {
        SharedElementContextPreview {
            HomeScreenContents(
                layoutType = HomeScreenLayoutType.Compact,
                onClickLetsGo = {},
                videoLink = "",
                dancingBotLink = "https://services.google.com/fh/files/misc/android_dancing.gif",
                onAboutClicked = {},
            )
        }
    }
}
