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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreationScreen(
    creationViewModel: CreationViewModel,
    onCameraPressed: () -> Unit = {},
    onBackPressed: () -> Unit,
    onAboutPressed: () -> Unit,
    onImageCreated: (resultImageUri: Uri, prompt: String?, originalImageUri: Uri?) -> Unit,
) {
    val uiState by creationViewModel.uiState.collectAsStateWithLifecycle()
    val layoutType = calculateLayoutType(uiState.xrEnabled)
    BackHandler(
        enabled = uiState.screenState != ScreenState.EDIT,
    ) {
        creationViewModel.onBackPress()
    }
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            creationViewModel.onImageSelected(uri)
        }
    }
    val snackbarHostState by creationViewModel.snackbarHostState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.resultBitmapUri) {
        uiState.resultBitmapUri?.let { resultBitmapUri ->
            onImageCreated(
                resultBitmapUri,
                uiState.descriptionText.text.toString(),
                if (uiState.selectedPromptOption == PromptType.PHOTO) {
                    uiState.imageUri
                } else {
                    null
                },
            )
            creationViewModel.onResultDisplayed()
        }
    }

    when (uiState.screenState) {
        ScreenState.EDIT -> {
            EditScreen(
                snackbarHostState = snackbarHostState,
                dropBehaviourFactory = creationViewModel.dropBehaviourFactory,
                layoutType = layoutType,
                onCameraPressed = onCameraPressed,
                onBackPressed = onBackPressed,
                onAboutPressed = onAboutPressed,
                uiState = uiState,
                onChooseImageClicked = { pickMedia.launch(PickVisualMediaRequest(it)) },
                onPromptOptionSelected = creationViewModel::onSelectedPromptOptionChanged,
                onUndoPressed = creationViewModel::onUndoPressed,
                onPromptGenerationPressed = creationViewModel::onPromptGenerationClicked,
                onBotColorSelected = creationViewModel::onBotColorChanged,
                onStartClicked = creationViewModel::onStartClicked,
                onDropCallback = creationViewModel::onImageSelected,
            )
        }

        ScreenState.LOADING -> {
            LoadingScreen(
                onCancelPress = {
                    creationViewModel.cancelInProgressTask()
                },
                layoutType = layoutType,
            )
        }
    }
}
