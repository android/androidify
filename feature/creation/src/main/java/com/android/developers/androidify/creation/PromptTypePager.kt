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
@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
)

package com.android.developers.androidify.creation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.components.HorizontalToolbar
import com.android.developers.androidify.util.PhonePreview
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun MainCreationPane(
    uiState: CreationState,
    dropBehaviourFactory: DropBehaviourFactory,
    modifier: Modifier = Modifier,
    onCameraPressed: () -> Unit,
    onChooseImageClicked: () -> Unit = {},
    onUndoPressed: () -> Unit = {},
    onPromptGenerationPressed: () -> Unit,
    onSelectedPromptOptionChanged: (PromptType) -> Unit,
    onDropCallback: (Uri) -> Unit,
) {
    PromptTypePager(modifier, uiState, onSelectedPromptOptionChanged) { promptType, pagerState ->
        when (promptType) {
            PromptType.PHOTO -> {
                PhotoPrompt(
                    uiState = uiState,
                    dropBehaviourFactory = dropBehaviourFactory,
                    onCameraPressed = onCameraPressed,
                    onChooseImageClicked = onChooseImageClicked,
                    onDropCallback = onDropCallback,
                    onUndoPressed = onUndoPressed,
                )
            }

            PromptType.TEXT -> {
                // Workaround for https://issuetracker.google.com/432431393
                val showTextPrompt by remember {
                    derivedStateOf {
                        pagerState.currentPage == PromptType.TEXT.ordinal
                                && pagerState.targetPage == pagerState.currentPage
                    }
                }
                if (showTextPrompt) {
                    TextPrompt(
                        textFieldState = uiState.descriptionText,
                        promptGenerationInProgress = uiState.promptGenerationInProgress,
                        generatedPrompt = uiState.generatedPrompt,
                        onPromptGenerationPressed = onPromptGenerationPressed,
                        modifier = Modifier
                            .fillMaxSize()
                            .heightIn(min = 200.dp)
                            .padding(2.dp),
                    )
                }
            }
        }
    }
}


@Composable
private fun PromptTypePager(
    modifier: Modifier = Modifier,
    uiState: CreationState,
    onSelectedPromptOptionChanged: (PromptType) -> Unit,
    content: @Composable PagerScope.(PromptType, PagerState) -> Unit,
) {
    Box(
        modifier = modifier,
    ) {
        val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
        val pagerState =
            rememberPagerState(uiState.selectedPromptOption.ordinal) { PromptType.entries.size }
        val focusManager = LocalFocusManager.current
        LaunchedEffect(uiState.selectedPromptOption) {
            launch {
                pagerState.animateScrollToPage(
                    uiState.selectedPromptOption.ordinal,
                    animationSpec = spatialSpec,
                )
            }.invokeOnCompletion {
                if (uiState.selectedPromptOption != PromptType.entries[pagerState.currentPage]) {
                    onSelectedPromptOptionChanged(PromptType.entries[pagerState.currentPage])
                }
            }
        }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onSelectedPromptOptionChanged(PromptType.entries[page])
            }
        }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.targetPage }.collect {
                if (pagerState.targetPage != PromptType.TEXT.ordinal) {
                    focusManager.clearFocus()
                }
            }
        }
        HorizontalPager(
            pagerState,
            modifier.fillMaxSize(),
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(16.dp),
            pageContent = {
                content(this, PromptType.entries[it], pagerState)
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun PromptTypeToolbar(
    selectedOption: PromptType,
    modifier: Modifier = Modifier,
    onOptionSelected: (PromptType) -> Unit,
) {
    HorizontalToolbar<PromptType>(
        selectedOption = selectedOption,
        modifier = modifier,
        label = { item -> item.displayName },
        onOptionSelected = onOptionSelected,
    )
}

@Preview
@Composable
private fun PromptTypeToolbarPreview() {
    AndroidifyTheme {
        PromptTypeToolbar(
            selectedOption = PromptType.PHOTO,
            onOptionSelected = {},
        )
    }
}

@Preview
@Composable
private fun PromptTypeMainPreview() {
    AndroidifyTheme {
        SharedElementContextPreview {
            MainCreationPane(
                uiState = CreationState(),
                dropBehaviourFactory = fakeDropBehaviourFactory,
                onCameraPressed = { },
                onChooseImageClicked = {},
                onUndoPressed = {},
                onPromptGenerationPressed = {},
                onSelectedPromptOptionChanged = {},
                onDropCallback = {}
            )
        }
    }
}
