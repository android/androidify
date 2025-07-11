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

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.LocalSharedTransitionScope
import com.android.developers.androidify.theme.R
import com.android.developers.androidify.theme.SharedElementKey
import com.android.developers.androidify.theme.sharedBoundsReveal

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AndroidifyTopAppBar(
    modifier: Modifier = Modifier,
    titleText: String = stringResource(R.string.androidify_title),
    isMediumWindowSize: Boolean = false,
    backEnabled: Boolean = false,
    aboutEnabled: Boolean = true,
    expandedCenterButtons: @Composable () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onAboutClicked: () -> Unit = {},
) {
    if (isMediumWindowSize) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 16.dp),
        ) {
            Row(
                modifier = modifier
                    .height(64.dp)
                    .padding(start = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = MaterialTheme.shapes.large,
                    )
                    .padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (backEnabled) {
                    BackButton(onBackPressed)
                } else {
                    Spacer(modifier.size(16.dp))
                }
                AndroidifyTitle(titleText)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center),
            ) {
                expandedCenterButtons()
            }

            if (aboutEnabled) {
                AboutButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = CircleShape,
                        ),
                    onAboutClicked = onAboutClicked,
                )
            }
        }
    } else {
        CenterAlignedTopAppBar(
            title = {
                AndroidifyTitle(titleText)
            },
            modifier = modifier
                .statusBarsPadding()
                .padding(8.dp)
                .clip(
                    MaterialTheme.shapes.large,
                ),
            navigationIcon = {
                if (backEnabled) {
                    BackButton(onBackPressed)
                }
            },
            actions = {
                if (aboutEnabled) {
                    AboutButton(onAboutClicked = onAboutClicked)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        )
    }
}

@Composable
private fun BackButton(onBackPressed: () -> Unit) {
    IconButton(onClick = onBackPressed) {
        Icon(
            ImageVector.vectorResource(R.drawable.rounded_arrow_back_24),
            contentDescription = "Back",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidifyTranslucentTopAppBar(
    modifier: Modifier = Modifier,
    titleText: String = stringResource(R.string.androidify_title),
    isMediumSizeLayout: Boolean = false,
) {
    if (isMediumSizeLayout) {
        TopAppBar(
            title = {
                Spacer(Modifier.statusBarsPadding())
                AndroidifyTitle(titleText)
            },
            modifier = modifier.clip(
                MaterialTheme.shapes.large.copy(topStart = CornerSize(0f), topEnd = CornerSize(0f)),
            ),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
    } else {
        CenterAlignedTopAppBar(
            title = {
                Spacer(Modifier.statusBarsPadding())
                AndroidifyTitle(titleText)
            },
            modifier = modifier.clip(
                MaterialTheme.shapes.large.copy(topStart = CornerSize(0f), topEnd = CornerSize(0f)),
            ),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun AndroidifyTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AboutButton(modifier: Modifier = Modifier, onAboutClicked: () -> Unit = {}) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        IconButton(
            onClick = {
                onAboutClicked()
            },
            modifier = modifier.sharedBoundsReveal(
                rememberSharedContentState(SharedElementKey.AboutKey),
                renderInOverlayDuringTransition = false,
            ),
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.outline_info_24),
                contentDescription = "About",
            )
        }
    }
}
