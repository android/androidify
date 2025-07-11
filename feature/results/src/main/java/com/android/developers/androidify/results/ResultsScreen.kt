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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalPermissionsApi::class)

package com.android.developers.androidify.results

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.PrimaryButton
import com.android.developers.androidify.theme.components.ResultsBackground
import com.android.developers.androidify.util.AdaptivePreview
import com.android.developers.androidify.util.SmallPhonePreview
import com.android.developers.androidify.util.allowsFullContent
import com.android.developers.androidify.util.isAtLeastMedium
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@Composable
fun ResultsScreen(
    resultImage: Bitmap,
    originalImageUri: Uri?,
    promptText: String?,
    modifier: Modifier = Modifier,
    verboseLayout: Boolean = allowsFullContent(),
    onBackPress: () -> Unit,
    onAboutPress: () -> Unit,
    onNextPress: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel<ResultsViewModel>(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(resultImage, originalImageUri, promptText) {
        viewModel.setArguments(resultImage, originalImageUri, promptText)
    }
    val snackbarHostState by viewModel.snackbarHostState.collectAsStateWithLifecycle()
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(snackbarData, shape = SnackbarDefaults.shape)
                },
            )
        },
        topBar = {
            AndroidifyTopAppBar(
                backEnabled = true,
                isMediumWindowSize = isAtLeastMedium(),
                onBackPressed = {
                    onBackPress()
                },
                onAboutClicked = onAboutPress,
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
    ) { contentPadding ->

        ResultsScreenContents(
            contentPadding,
            state,
            verboseLayout = verboseLayout,
            onCustomizeShareClicked = onNextPress,
        )
    }
}

@AdaptivePreview
@SmallPhonePreview
@Preview
@Composable
private fun ResultsScreenPreview() {
    AndroidifyTheme {
        val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
        val state = remember {
            mutableStateOf(
                ResultState(
                    resultImageBitmap = bitmap.asAndroidBitmap(),
                    promptText = "wearing a hat with straw hair",
                ),
            )
        }

        ResultsScreenContents(
            contentPadding = PaddingValues(0.dp),
            state = state,
            onCustomizeShareClicked = {},
        )
    }
}

@SmallPhonePreview
@Composable
private fun ResultsScreenPreviewSmall() {
    AndroidifyTheme {
        val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
        val state = remember {
            mutableStateOf(
                ResultState(
                    resultImageBitmap = bitmap.asAndroidBitmap(),
                    promptText = "wearing a hat with straw hair",
                ),
            )
        }

        ResultsScreenContents(
            contentPadding = PaddingValues(0.dp),
            state = state,
            verboseLayout = false,
            onCustomizeShareClicked = {},
        )
    }
}

@Composable
fun ResultsScreenContents(
    contentPadding: PaddingValues,
    state: State<ResultState>,
    verboseLayout: Boolean = allowsFullContent(),
    onCustomizeShareClicked: () -> Unit,
    defaultSelectedResult: ResultOption = ResultOption.ResultImage,
) {
    ResultsBackground()
    val showResult = state.value.resultImageBitmap != null
    var selectedResultOption by remember {
        mutableStateOf(defaultSelectedResult)
    }
    val wasPromptUsed = state.value.originalImageUrl == null
    val promptToolbar = @Composable { modifier: Modifier ->
        ResultToolbarOption(
            modifier = modifier,
            selectedResultOption,
            wasPromptUsed,
            onResultOptionSelected = { option ->
                selectedResultOption = option
            },
        )
    }
    val botResultCard = @Composable { modifier: Modifier ->
        AnimatedVisibility(
            showResult,
            enter = fadeIn(tween(300, delayMillis = 1000)) + slideInVertically(
                tween(1000, easing = EaseOutBack, delayMillis = 1000),
                initialOffsetY = { fullHeight -> fullHeight },
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                BotResultCard(
                    state.value.resultImageBitmap!!,
                    state.value.originalImageUrl,
                    state.value.promptText,
                    modifier = Modifier.align(Alignment.Center),
                    flippableState = selectedResultOption.toFlippableState(),
                    onFlipStateChanged = { flipOption ->
                        selectedResultOption = when (flipOption) {
                            FlippableState.Front -> ResultOption.ResultImage
                            FlippableState.Back -> ResultOption.OriginalInput
                        }
                    },
                )
            }
        }
    }
    val buttonRow = @Composable { modifier: Modifier ->
        BotActionsButtonRow(
            onCustomizeShareClicked = {
                onCustomizeShareClicked()
            },
            modifier = modifier,
            verboseLayout = verboseLayout,
        )
    }
    val backgroundQuotes = @Composable { modifier: Modifier ->
        AnimatedVisibility(
            showResult,
            enter = slideInHorizontally(animationSpec = tween(1000)) { fullWidth -> fullWidth },
            modifier = Modifier.fillMaxSize(),
        ) {
            BackgroundRandomQuotes(verboseLayout)
        }
    }

    // Draw the actual content
    if (verboseLayout) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            promptToolbar(Modifier.align(Alignment.CenterHorizontally))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            ) {
                backgroundQuotes(Modifier)
                botResultCard(Modifier)
            }
            buttonRow(
                Modifier
                    .padding(bottom = 16.dp, top = 16.dp)
                    .align(Alignment.CenterHorizontally),
            )
        }
    } else {
        Box {
            backgroundQuotes(Modifier.fillMaxSize())
            botResultCard(Modifier)
            promptToolbar(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp),
            )
            buttonRow(
                Modifier
                    .padding(bottom = 16.dp, end = 16.dp)
                    .align(Alignment.BottomEnd),
            )
        }
    }
}

@Composable
private fun BackgroundRandomQuotes(verboseLayout: Boolean = true) {
    val locaInspectionMode = LocalInspectionMode.current
    Box(modifier = Modifier.fillMaxSize()) {
        val listResultCompliments = stringArrayResource(R.array.list_compliments)
        val randomQuote = remember {
            if (locaInspectionMode) {
                listResultCompliments.first()
            } else {
                listResultCompliments.random()
            }
        }
        // Disable animation in tests
        val iterations = if (LocalInspectionMode.current) 0 else 100
        Text(
            randomQuote,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            fontSize = 120.sp,
            modifier = Modifier
                .align(if (verboseLayout) Alignment.TopCenter else Alignment.Center)
                .basicMarquee(
                    iterations = iterations,
                    repeatDelayMillis = 0,
                    velocity = 80.dp,
                    initialDelayMillis = 500,
                ),
        )
        if (verboseLayout) {
            val listMinusOther = listResultCompliments.asList().minus(randomQuote)
            val randomQuote2 = remember {
                if (locaInspectionMode) {
                    listMinusOther.first()
                } else {
                    listMinusOther.random()
                }
            }
            Text(
                randomQuote2,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                fontSize = 110.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .basicMarquee(
                        iterations = iterations,
                        repeatDelayMillis = 0,
                        velocity = 60.dp,
                        initialDelayMillis = 500,
                    ),
            )
        }
    }
}

@Composable
private fun BotActionsButtonRow(
    onCustomizeShareClicked: () -> Unit,
    modifier: Modifier = Modifier,
    verboseLayout: Boolean = false,
) {
    Row(modifier) {
        PrimaryButton(
            onClick = {
                onCustomizeShareClicked()
            },
            trailingIcon = {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        ImageVector
                            .vectorResource(com.android.developers.androidify.theme.R.drawable.rounded_arrow_forward_24),
                        contentDescription = null, // decorative element
                    )
                }
            },
            buttonText = if (verboseLayout) stringResource(R.string.customize_and_share) else null,
        )
    }
}
