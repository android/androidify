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
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.creation.R as CreationR

@Composable
fun EditScreenContentsMedium(
    dropBehaviourFactory: DropBehaviourFactory,
    onCameraPressed: () -> Unit,
    uiState: CreationState,
    onChooseImageClicked: (PickVisualMedia.VisualMediaType) -> Unit,
    onPromptOptionSelected: (PromptType) -> Unit,
    onUndoPressed: () -> Unit,
    onPromptGenerationPressed: () -> Unit,
    onBotColorSelected: (BotColor) -> Unit,
    onStartClicked: () -> Unit,
    onDropCallback: (Uri) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
        ) {
            MainCreationPane(
                uiState,
                dropBehaviourFactory = dropBehaviourFactory,
                modifier = Modifier.weight(.6f),
                onCameraPressed = onCameraPressed,
                onChooseImageClicked = {
                    onChooseImageClicked(PickVisualMedia.ImageOnly)
                },
                onUndoPressed = onUndoPressed,
                onPromptGenerationPressed = onPromptGenerationPressed,
                onSelectedPromptOptionChanged = onPromptOptionSelected,
                onDropCallback = onDropCallback,
            )
            Box(
                modifier = Modifier
                    .weight(.4f)
                    .padding(top = 16.dp, bottom = 16.dp)
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = MaterialTheme.shapes.large,
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.large,
                    ),
            ) {
                AndroidBotColorPicker(
                    selectedBotColor = uiState.botColor,
                    modifier = Modifier.padding(16.dp),
                    onBotColorSelected = onBotColorSelected,
                    listBotColor = uiState.listBotColors,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TransformButton(
                modifier = Modifier.padding(bottom = 8.dp),
                buttonText = stringResource(CreationR.string.start_transformation_button),
                onClicked = onStartClicked,
            )
        }
    }
}

@Composable
@LargeScreensPreview
private fun EditScreenPreview() {
    AndroidifyTheme {
        SharedElementContextPreview {
            EditScreen(
                snackbarHostState = SnackbarHostState(),
                dropBehaviourFactory = fakeDropBehaviourFactory,
                onCameraPressed = { },
                uiState = CreationState(),
                onChooseImageClicked = {},
                onPromptOptionSelected = {},
                onUndoPressed = {},
                onPromptGenerationPressed = {},
                onBotColorSelected = {},
                onStartClicked = {},
                onDropCallback = {},
                onBackPressed = {},
                onAboutPressed = {},
                layoutType = EditScreenLayoutType.Medium,
            )
        }
    }
}
