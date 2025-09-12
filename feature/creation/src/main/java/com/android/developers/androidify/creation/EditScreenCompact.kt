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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.components.SecondaryOutlinedButton
import com.android.developers.androidify.util.PhonePreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.android.developers.androidify.creation.R as CreationR

@Composable
fun EditScreenContentsCompact(
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
    var showColorPickerBottomSheet by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        PromptTypeToolbar(
            uiState.selectedPromptOption,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterHorizontally),
            onOptionSelected = onPromptOptionSelected,
        )

        MainCreationPane(
            uiState,
            dropBehaviourFactory = dropBehaviourFactory,
            modifier = Modifier.weight(1f),
            onCameraPressed = onCameraPressed,
            onChooseImageClicked = {
                onChooseImageClicked(PickVisualMedia.ImageOnly)
            },
            onUndoPressed = onUndoPressed,
            onPromptGenerationPressed = onPromptGenerationPressed,
            onSelectedPromptOptionChanged = onPromptOptionSelected,
            onDropCallback = onDropCallback,
        )

        BottomButtons(
            onButtonColorClicked = {
                showColorPickerBottomSheet = !showColorPickerBottomSheet
            },
            uiState = uiState,
            onStartClicked = onStartClicked,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
    BotColorPickerBottomSheet(
        showColorPickerBottomSheet,
        dismissBottomSheet = {
            showColorPickerBottomSheet = false
        },
        onColorChanged = onBotColorSelected,
        listBotColors = uiState.listBotColors,
        selectedBotColor = uiState.botColor,
    )
}

@Composable
private fun BotColorPickerBottomSheet(
    showColorPickerBottomSheet: Boolean,
    dismissBottomSheet: () -> Unit,
    onColorChanged: (BotColor) -> Unit,
    listBotColors: List<BotColor>,
    selectedBotColor: BotColor,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showColorPickerBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier,
            sheetState = sheetState,
            onDismissRequest = {
                dismissBottomSheet()
            },
        ) {
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier.padding(
                    start = 36.dp,
                    end = 36.dp,
                    top = 16.dp,
                    bottom = 8.dp,
                ),
            ) {
                AndroidBotColorPicker(
                    selectedBotColor,
                    onBotColorSelected = {
                        onColorChanged(it)
                        scope.launch {
                            delay(400)
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                dismissBottomSheet()
                            }
                        }
                    },
                    listBotColor = listBotColors,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.BottomButtons(
    onButtonColorClicked: () -> Unit,
    uiState: CreationState,
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .wrapContentSize()
            .padding(8.dp)
            .align(Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SecondaryOutlinedButton(
            onClick = {
                onButtonColorClicked()
            },
            buttonText = stringResource(CreationR.string.bot_color_button),
            modifier = Modifier.fillMaxRowHeight(),
            leadingIcon = {
                Row {
                    DisplayBotColor(
                        uiState.botColor,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                CircleShape,
                            )
                            .size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            },
        )
        TransformButton(
            modifier = Modifier.fillMaxRowHeight(),
            onClicked = onStartClicked,
        )
    }
}

@Composable
@PhonePreview
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
                layoutType = EditScreenLayoutType.Compact,
            )
        }
    }
}
