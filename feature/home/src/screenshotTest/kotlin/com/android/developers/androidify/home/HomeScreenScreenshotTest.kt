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

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.util.AdaptivePreview

class HomeScreenScreenshotTest {

    @AdaptivePreview
    @Preview(showBackground = true)
    @Composable
    fun HomeScreenScreenshot() {
        AndroidifyTheme {
            SharedElementContextPreview {
                HomeScreenContents(
                    layoutType = calculateLayoutType(enableXr = false),
                    onClickLetsGo = { },
                    onAboutClicked = {},
                    videoLink = "",
                    dancingBotLink = "",
                )
            }
        }
    }
}
