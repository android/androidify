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

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.developers.androidify.customize.getPlaceholderBotUri
import com.android.developers.androidify.results.xr.FlippablePanel
import com.android.developers.androidify.results.xr.ResultsScreenSpatial
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.AboutButton
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.ResultsBackground
import com.android.developers.androidify.util.AdaptivePreview
import com.android.developers.androidify.util.SmallPhonePreview
import com.android.developers.androidify.util.isWidthAtLeastMedium
import com.android.developers.androidify.xr.RequestFullSpaceIconButton
import com.android.developers.androidify.xr.RequestHomeSpaceIconButton
import com.android.developers.androidify.xr.couldRequestFullSpace
import com.android.developers.androidify.xr.couldRequestHomeSpace
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@Composable
fun ResultsScreen(
    onBackPress: () -> Unit,
    onAboutPress: () -> Unit,
    onNextPress: (resultImageUri: Uri, originalImageUri: Uri?) -> Unit,
    viewModel: ResultsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState by viewModel.snackbarHostState.collectAsStateWithLifecycle()
    val layoutType = getLayoutType(enableXr = state.xrEnabled)

    var selectedResultOption by remember {
        mutableStateOf(ResultOption.ResultImage)
    }
    val wasPromptUsed = state.originalImageUrl == null
    val onCustomizeShareClicked = state.resultImageUri?.let { resultUri ->
        {
            onNextPress(
                resultUri,
                state.originalImageUrl,
            )
        }
    }

    ResultsScreenContents(
        selectedResultOption = selectedResultOption,
        onResultOptionSelected = { selectedResultOption = it },
        wasPromptUsed = wasPromptUsed,
        onBackPress = onBackPress,
        layoutType = layoutType,
        onAboutPress = onAboutPress,
        state = state,
        onCustomizeShareClicked = onCustomizeShareClicked,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun ResultsScreenContents(
    selectedResultOption: ResultOption,
    onResultOptionSelected: (ResultOption) -> Unit,
    wasPromptUsed: Boolean,
    onBackPress: () -> Unit,
    layoutType: ResultsLayoutType,
    onAboutPress: () -> Unit,
    state: ResultState,
    onCustomizeShareClicked: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
) {
    var showResult by remember { mutableStateOf(false) }
    LaunchedEffect(state.resultImageUri) {
        showResult = state.resultImageUri != null
    }

    val promptToolbar = @Composable { modifier: Modifier ->
        ResultToolbarOption(
            modifier = modifier,
            selectedOption = selectedResultOption,
            wasPromptUsed = wasPromptUsed,
            onResultOptionSelected = onResultOptionSelected,
        )
    }
    val topBar = @Composable {
        AndroidifyTopAppBar(
            backEnabled = true,
            isMediumWindowSize = isWidthAtLeastMedium(),
            onBackPressed = {
                onBackPress()
            },
            expandedCenterButtons = {
                if (layoutType == ResultsLayoutType.Spatial) promptToolbar(Modifier)
            },
            actions = {
                AboutButton { onAboutPress() }
                if (state.xrEnabled) {
                    if (couldRequestFullSpace()) {
                        RequestFullSpaceIconButton()
                    }
                    if (couldRequestHomeSpace()) {
                        RequestHomeSpaceIconButton()
                    }
                }
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
            Box(Modifier.fillMaxSize()) {
                val front = @Composable {
                    FrontCardImage(state.resultImageUri!!)
                }
                val back = @Composable {
                    val originalImageUrl = state.originalImageUrl
                    if (originalImageUrl != null) {
                        BackCard(originalImageUrl)
                    } else {
                        BackCardPrompt(state.promptText!!)
                    }
                }
                val onFlipStateChanged = { flipOption: FlippableState ->
                    val option = when (flipOption) {
                        FlippableState.Front -> ResultOption.ResultImage
                        FlippableState.Back -> ResultOption.OriginalInput
                    }
                    onResultOptionSelected(option)
                }
                when (layoutType) {
                    ResultsLayoutType.Spatial ->
                        FlippablePanel(
                            front = front,
                            back = back,
                            flippableState = selectedResultOption.toFlippableState(),
                            onFlipStateChanged = onFlipStateChanged,
                        )

                    else ->
                        BotResultCard(
                            modifier = Modifier.align(Alignment.Center),
                            front = front,
                            back = back,
                            flippableState = selectedResultOption.toFlippableState(),
                            onFlipStateChanged = onFlipStateChanged,
                        )
                }
            }
        }
    }
    val buttonRow = @Composable { modifier: Modifier ->
        BotActionsButtonRow(
            onCustomizeShareClicked = {
                onCustomizeShareClicked?.invoke()
            },
            modifier = modifier,
            layoutType = layoutType,
        )
    }
    val backgroundQuotes = @Composable { modifier: Modifier ->
        AnimatedVisibility(
            showResult,
            enter = slideInHorizontally(animationSpec = tween(1000)) { fullWidth -> fullWidth },
            modifier = modifier.fillMaxSize(),
        ) {
            BackgroundRandomQuotes(layoutType != ResultsLayoutType.Constrained)
        }
    }

    when (layoutType) {
        ResultsLayoutType.Verbose ->
            ResultsScreenScaffold(snackbarHostState, topBar) { contentPadding ->
                ResultsBackground()
                ResultsScreenVerbose(
                    backgroundQuotes = backgroundQuotes,
                    botResultCard = botResultCard,
                    buttonRow = buttonRow,
                    promptToolbar = promptToolbar,
                    contentPadding = contentPadding,
                )
            }

        ResultsLayoutType.Constrained ->
            ResultsScreenScaffold(snackbarHostState, topBar) { contentPadding ->
                ResultsBackground()
                ResultsScreenConstrained(
                    backgroundQuotes = backgroundQuotes,
                    botResultCard = botResultCard,
                    buttonRow = buttonRow,
                    promptToolbar = promptToolbar,
                    contentPadding = contentPadding,
                )
            }

        ResultsLayoutType.Spatial ->
            ResultsScreenSpatial(
                backgroundQuotes = backgroundQuotes,
                botResultCard = botResultCard,
                buttonRow = buttonRow,
                topBar = { _ -> topBar() },
                snackbarHostState = snackbarHostState,
            )
    }
}

@Composable
fun ResultsScreenScaffold(
    snackbarHostState: SnackbarHostState,
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(snackbarData, shape = SnackbarDefaults.shape)
                },
            )
        },
        topBar = topBar,
        containerColor = containerColor,
        modifier = modifier.fillMaxSize(),
        content = content,
    )
}

@Composable
private fun ResultsScreenVerbose(
    backgroundQuotes: @Composable (Modifier) -> Unit,
    botResultCard: @Composable (Modifier) -> Unit,
    buttonRow: @Composable (Modifier) -> Unit,
    promptToolbar: @Composable (Modifier) -> Unit,
    contentPadding: PaddingValues,
) {
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
}

@Composable
fun ResultsScreenConstrained(
    backgroundQuotes: @Composable (Modifier) -> Unit,
    botResultCard: @Composable (Modifier) -> Unit,
    buttonRow: @Composable (Modifier) -> Unit,
    promptToolbar: @Composable (Modifier) -> Unit,
    contentPadding: PaddingValues,
) {
    Box(Modifier.padding(contentPadding)) {
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

@AdaptivePreview
@SmallPhonePreview
@Preview
@Composable
private fun ResultsScreenPreview() {
    AndroidifyTheme {
        val imageUri = getPlaceholderBotUri()
        val state = ResultState(
            resultImageUri = imageUri,
            promptText = "wearing a hat with straw hair",
        )

        ResultsScreenContents(
            selectedResultOption = ResultOption.OriginalInput,
            onResultOptionSelected = { },
            wasPromptUsed = true,
            onBackPress = { },
            layoutType = ResultsLayoutType.Verbose,
            onAboutPress = { },
            state = state,
            onCustomizeShareClicked = { },
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@SmallPhonePreview
@Composable
private fun ResultsScreenPreviewSmall() {
    AndroidifyTheme {
        val imageUri = getPlaceholderBotUri()
        val state = ResultState(
            resultImageUri = imageUri,
            promptText = "wearing a hat with straw hair",
        )

        ResultsScreenContents(
            selectedResultOption = ResultOption.OriginalInput,
            onResultOptionSelected = { },
            wasPromptUsed = true,
            onBackPress = { },
            layoutType = ResultsLayoutType.Constrained,
            onAboutPress = { },
            state = state,
            onCustomizeShareClicked = { },
            snackbarHostState = SnackbarHostState(),
        )
    }
}
