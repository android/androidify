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
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.android.developers.androidify.customize.CustomizeAndExportScreen
import com.android.developers.androidify.customize.CustomizeExportViewModel
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.results.ResultsScreen
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LimeGreen
import com.android.developers.androidify.theme.LocalSharedTransitionScope
import com.android.developers.androidify.theme.Primary90
import com.android.developers.androidify.theme.R
import com.android.developers.androidify.theme.Secondary
import com.android.developers.androidify.theme.SharedElementKey
import com.android.developers.androidify.theme.components.AboutButton
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.GradientAssistElevatedChip
import com.android.developers.androidify.theme.components.PrimaryButton
import com.android.developers.androidify.theme.components.ScaleIndicationNodeFactory
import com.android.developers.androidify.theme.components.SecondaryOutlinedButton
import com.android.developers.androidify.theme.components.SquiggleBackground
import com.android.developers.androidify.theme.components.gradientChipColorDefaults
import com.android.developers.androidify.theme.components.infinitelyAnimatingLinearGradient
import com.android.developers.androidify.theme.sharedBoundsRevealWithShapeMorph
import com.android.developers.androidify.theme.sharedBoundsWithDefaults
import com.android.developers.androidify.util.AnimatedTextField
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.dashedRoundedRectBorder
import com.android.developers.androidify.util.isAtLeastMedium
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.android.developers.androidify.creation.R as CreationR

@Composable
fun CreationScreen(
    fileName: String? = null,
    creationViewModel: CreationViewModel = hiltViewModel(),
    isMedium: Boolean = isAtLeastMedium(),
    onCameraPressed: () -> Unit = {},
    onBackPressed: () -> Unit,
    onAboutPressed: () -> Unit,
) {
    val uiState by creationViewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(
        enabled = uiState.screenState != ScreenState.EDIT,
    ) {
        creationViewModel.onBackPress()
    }
    LaunchedEffect(Unit) {
        if (fileName != null) {
            creationViewModel.onImageSelected(fileName.toUri())
        } else {
            creationViewModel.onImageSelected(null)
        }
    }
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            creationViewModel.onImageSelected(uri)
        }
    }
    val snackbarHostState by creationViewModel.snackbarHostState.collectAsStateWithLifecycle()
    when (uiState.screenState) {
        ScreenState.EDIT -> {
            EditScreen(
                snackbarHostState = snackbarHostState,
                dropBehaviourFactory = creationViewModel.dropBehaviourFactory,
                isExpanded = isMedium,
                onCameraPressed = onCameraPressed,
                onBackPressed = onBackPressed,
                onAboutPressed = onAboutPressed,
                uiState = uiState,
                onChooseImageClicked = { pickMedia.launch(PickVisualMediaRequest(it)) },
                onPromptOptionSelected = creationViewModel::onSelectedPromptOptionChanged,
                onUndoPressed = creationViewModel::onUndoPressed,
                onPromptGenerationPressed = creationViewModel::onPromptGenerationClicked,
                onBotColorSelected = creationViewModel::onBotColorChanged,
                onStartClicked = creationViewModel::startClicked,
                onDropCallback = creationViewModel::onImageSelected,
            )
        }

        ScreenState.LOADING -> {
            LoadingScreen(
                onCancelPress = {
                    creationViewModel.cancelInProgressTask()
                },
            )
        }

        ScreenState.RESULT -> {
            val prompt = uiState.descriptionText.text.toString()
            val key = if (uiState.descriptionText.text.isBlank()) {
                uiState.imageUri.toString()
            } else {
                prompt
            }
            ResultsScreen(
                uiState.resultBitmap!!,
                if (uiState.selectedPromptOption == PromptType.PHOTO) {
                    uiState.imageUri
                } else {
                    null
                },
                promptText = prompt,
                viewModel = hiltViewModel(key = key),
                onAboutPress = onAboutPressed,
                onBackPress = onBackPressed,
                onNextPress = creationViewModel::customizeExportClicked,
            )
        }

        ScreenState.CUSTOMIZE -> {
            val prompt = uiState.descriptionText.text.toString()
            val key = if (uiState.descriptionText.text.isBlank()) {
                uiState.imageUri.toString()
            } else {
                prompt
            }
            uiState.resultBitmap?.let { bitmap ->
                CustomizeAndExportScreen(
                    resultImage = bitmap,
                    originalImageUri = uiState.imageUri,
                    onBackPress = onBackPressed,
                    onInfoPress = onAboutPressed,
                    viewModel = hiltViewModel<CustomizeExportViewModel>(key = key),
                )
            }
        }
    }
}

@Composable
fun EditScreen(
    snackbarHostState: SnackbarHostState,
    dropBehaviourFactory: DropBehaviourFactory,
    isExpanded: Boolean,
    onCameraPressed: () -> Unit,
    onBackPressed: () -> Unit,
    onAboutPressed: () -> Unit,
    uiState: CreationState,
    onChooseImageClicked: (PickVisualMedia.VisualMediaType) -> Unit,
    onPromptOptionSelected: (PromptType) -> Unit,
    onUndoPressed: () -> Unit,
    onPromptGenerationPressed: () -> Unit,
    onBotColorSelected: (BotColor) -> Unit,
    onStartClicked: () -> Unit,
    onDropCallback: (Uri) -> Unit = {},
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData,
                        shape = SnackbarDefaults.shape,
                        modifier = Modifier.padding(4.dp),
                    )
                },
                modifier = Modifier.safeContentPadding(),
            )
        },
        topBar = {
            AndroidifyTopAppBar(
                backEnabled = true,
                isMediumWindowSize = isExpanded,
                actions = {
                    AboutButton { onAboutPressed() }
                },
                onBackPressed = onBackPressed,
                expandedCenterButtons = {
                    PromptTypeToolbar(
                        uiState.selectedPromptOption,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        onOptionSelected = onPromptOptionSelected,
                    )
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { contentPadding ->
        SquiggleBackground(offsetHeightFraction = 0.5f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding(),
        ) {
            var showColorPickerBottomSheet by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                if (!isExpanded) {
                    PromptTypeToolbar(
                        uiState.selectedPromptOption,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        onOptionSelected = onPromptOptionSelected,
                    )
                }
                if (isExpanded) {
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
                } else {
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
                }

                if (isExpanded) {
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
                } else {
                    BottomButtons(
                        onButtonColorClicked = {
                            showColorPickerBottomSheet = !showColorPickerBottomSheet
                        },
                        uiState = uiState,
                        onStartClicked = onStartClicked,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
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
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun MainCreationPane(
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
    val defaultDropAreaBackgroundColor = MaterialTheme.colorScheme.surface
    val alternateDropAreaBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    var background by remember { mutableStateOf(defaultDropAreaBackgroundColor) }

    val activity = LocalActivity.current as ComponentActivity
    val externalAppCallback = remember {
        dropBehaviourFactory.createTargetCallback(
            activity = activity,
            onImageDropped = { uri -> onDropCallback(uri) },
            onDropStarted = { background = alternateDropAreaBackgroundColor },
            onDropEnded = { background = defaultDropAreaBackgroundColor },
        )
    }

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
        ) {
            when (it) {
                PromptType.PHOTO.ordinal -> {
                    val imageUri = uiState.imageUri
                    if (imageUri == null) {
                        UploadEmptyState(
                            modifier = Modifier
                                .background(
                                    color = background,
                                    shape = MaterialTheme.shapes.large,
                                )
                                .dashedRoundedRectBorder(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    cornerRadius = 28.dp,
                                )
                                .dragAndDropTarget(
                                    shouldStartDragAndDrop = { event ->
                                        dropBehaviourFactory.shouldStartDragAndDrop(
                                            event,
                                        )
                                    },
                                    target = externalAppCallback,
                                )
                                .fillMaxSize()
                                .padding(2.dp),
                            onCameraPressed = onCameraPressed,
                            onChooseImagePress = onChooseImageClicked,
                        )
                    } else {
                        ImagePreview(
                            imageUri,
                            onUndoPressed,
                            onChooseImagePressed = onChooseImageClicked,
                            modifier = Modifier
                                .fillMaxSize()
                                .heightIn(min = 200.dp),
                        )
                    }
                }

                PromptType.TEXT.ordinal -> {
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
private fun TransformButton(
    modifier: Modifier = Modifier,
    buttonText: String = stringResource(CreationR.string.transform_button),
    onClicked: () -> Unit = {},
) {
    PrimaryButton(
        modifier = modifier,
        onClick = onClicked,
        buttonText = buttonText,
        trailingIcon = {
            Row {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    ImageVector.vectorResource(R.drawable.rounded_arrow_forward_24),
                    contentDescription = null,
                )
            }
        },
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
fun ImagePreview(
    uri: Uri,
    onUndoPressed: () -> Unit,
    onChooseImagePressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedElementScope = LocalSharedTransitionScope.current
    with(sharedElementScope) {
        Box(modifier) {
            AsyncImage(
                ImageRequest.Builder(LocalContext.current).data(uri).crossfade(false).build(),
                placeholder = null,
                contentDescription = stringResource(CreationR.string.cd_selected_image),
                modifier = Modifier
                    .align(Alignment.Center)
                    .sharedBoundsWithDefaults(rememberSharedContentState(SharedElementKey.CaptureImageToDetails))
                    .clip(MaterialTheme.shapes.large)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            ) {
                SecondaryOutlinedButton(
                    onClick = {
                        onUndoPressed()
                    },
                    leadingIcon = {
                        Icon(
                            ImageVector.vectorResource(CreationR.drawable.rounded_redo_24),
                            contentDescription = stringResource(CreationR.string.cd_retake_photo),
                        )
                    },
                )
                Spacer(modifier = Modifier.width(8.dp))
                SecondaryOutlinedButton(
                    onClick = {
                        onChooseImagePressed()
                    },
                    buttonText = stringResource(CreationR.string.photo_picker_choose_photo_label), // Reusing existing
                    leadingIcon = {
                        Icon(
                            ImageVector.vectorResource(CreationR.drawable.rounded_photo_24),
                            contentDescription = stringResource(CreationR.string.cd_choose_photo),
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextPromptGenerationPreview() {
    AndroidifyTheme {
        TextPrompt(
            TextFieldState(),
            false,
            generatedPrompt = "wearing a red sweater",
            onPromptGenerationPressed = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextPromptGenerationInProgressPreview() {
    AndroidifyTheme {
        TextPrompt(
            TextFieldState(),
            true,
            generatedPrompt = "wearing a red sweater",
            onPromptGenerationPressed = {},
        )
    }
}

@Composable
fun TextPrompt(
    textFieldState: TextFieldState,
    promptGenerationInProgress: Boolean,
    modifier: Modifier = Modifier,
    generatedPrompt: String? = null,
    onPromptGenerationPressed: () -> Unit,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                rememberVectorPainter(ImageVector.vectorResource(CreationR.drawable.rounded_draw_24)),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                stringResource(CreationR.string.headline_my_bot_is),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 24.sp,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .dashedRoundedRectBorder(
                    2.dp,
                    MaterialTheme.colorScheme.outline,
                    cornerRadius = 28.dp,
                )
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxSize(),
        ) {
            AnimatedTextField(
                textFieldState,
                targetEndState = generatedPrompt,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                textStyle = TextStyle(fontSize = 24.sp),
                decorator = { innerTextField ->
                    if (textFieldState.text.isEmpty()) {
                        Text(
                            stringResource(CreationR.string.prompt_text_hint).trimIndent(),
                            color = Color.Gray,
                            fontSize = 24.sp,
                        )
                    }
                    innerTextField()
                },
            )
            AnimatedVisibility(
                !WindowInsets.isImeVisible,
                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
            ) {
                HelpMeWriteButton(promptGenerationInProgress, onPromptGenerationPressed)
            }
        }
    }
}

@Composable
private fun HelpMeWriteButton(
    promptGenerationInProgress: Boolean,
    onPromptGenerationPressed: () -> Unit,
) {
    val color = if (promptGenerationInProgress) {
        Brush.infinitelyAnimatingLinearGradient(
            listOf(
                LimeGreen,
                Primary90,
                Secondary,
            ),
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.surfaceContainerLow)
    }
    GradientAssistElevatedChip(
        onClick = {
            onPromptGenerationPressed()
        },
        label = {
            if (promptGenerationInProgress) {
                Text(stringResource(CreationR.string.writing))
            } else {
                Text(stringResource(CreationR.string.write_me_a_prompt))
            }
        },
        leadingIcon = {
            Icon(
                rememberVectorPainter(ImageVector.vectorResource(CreationR.drawable.pen_spark_24)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        colors = gradientChipColorDefaults().copy(
            containerColor = color,
            disabledContainerColor = color,
        ),
        enabled = !promptGenerationInProgress,
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun PromptTypeToolbar(
    selectedOption: PromptType,
    modifier: Modifier = Modifier,
    onOptionSelected: (PromptType) -> Unit,
) {
    val options = PromptType.entries
    HorizontalFloatingToolbar(
        modifier = modifier.border(
            2.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = MaterialTheme.shapes.large,
        ),
        colors = FloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.surface,
            toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            fabContainerColor = MaterialTheme.colorScheme.tertiary,
            fabContentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        expanded = true,
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                modifier = Modifier,
                checked = selectedOption == label,
                onCheckedChange = { onOptionSelected(label) },
                shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Text(label.displayName, maxLines = 1)
            }
            if (index != options.size - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@LargeScreensPreview
@Preview
@Composable
private fun UploadEmptyPreview() {
    AndroidifyTheme {
        UploadEmptyState(
            {},
            {},
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
        )
        UploadEmptyState(
            {},
            {},
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun UploadEmptyState(
    onCameraPressed: () -> Unit,
    onChooseImagePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(CreationR.string.photo_picker_title),
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            minLines = 2,
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TakePhotoButton(onCameraPressed)
        Spacer(modifier = Modifier.height(32.dp))
        SecondaryOutlinedButton(
            onClick = {
                onChooseImagePress()
            },
            leadingIcon = {
                Image(
                    painterResource(CreationR.drawable.choose_picture_image),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp),
                )
            },
            buttonText = stringResource(CreationR.string.photo_picker_choose_photo_label),
        )
    }
}

@Composable
private fun TakePhotoButton(onCameraPressed: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val sharedElementScope = LocalSharedTransitionScope.current
    with(sharedElementScope) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
                .sizeIn(
                    minHeight = 48.dp,
                    maxHeight = ButtonDefaults.ExtraLargeContainerHeight,
                    minWidth = 48.dp,
                    maxWidth = ButtonDefaults.ExtraLargeContainerHeight,
                )
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .indication(interactionSource, ScaleIndicationNodeFactory(animationSpec))
                .background(
                    MaterialTheme.colorScheme.onSurface,
                    MaterialShapes.Cookie9Sided.toShape(),
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = Color.White),
                    onClick = {
                        onCameraPressed()
                    },
                    role = Role.Button,
                    enabled = true,
                    onClickLabel = stringResource(CreationR.string.take_picture_content_description),
                )
                .sharedBoundsRevealWithShapeMorph(
                    rememberSharedContentState(SharedElementKey.CameraButtonToFullScreenCamera),
                    restingShape = MaterialShapes.Cookie9Sided,
                    targetShape = RoundedPolygon.rectangle().normalized(),
                    targetValueByState = {
                        when (it) {
                            EnterExitState.PreEnter -> 0f
                            EnterExitState.Visible -> 1f
                            EnterExitState.PostExit -> 1f
                        }
                    },
                ),
        ) {
            Image(
                painterResource(R.drawable.photo_camera),
                contentDescription = stringResource(CreationR.string.take_picture_content_description),
                modifier = Modifier
                    .sizeIn(minHeight = 24.dp, maxHeight = 58.dp)
                    .padding(8.dp)
                    .aspectRatio(1f)
                    .align(Alignment.Center),
            )
        }
    }
}
