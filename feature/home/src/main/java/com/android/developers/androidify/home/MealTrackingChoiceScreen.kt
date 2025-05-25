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

package app.getnuri.home

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt

import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import app.getnuri.theme.AndroidifyTheme
import app.getnuri.theme.LocalSharedTransitionScope
import app.getnuri.theme.SharedElementKey
import app.getnuri.theme.components.AndroidifyTopAppBar
import app.getnuri.theme.components.PrimaryButton
import app.getnuri.theme.components.ScaleIndicationNodeFactory
import app.getnuri.theme.components.SecondaryOutlinedButton
import app.getnuri.theme.components.SquiggleBackground
import app.getnuri.theme.sharedBoundsRevealWithShapeMorph
import app.getnuri.theme.sharedBoundsWithDefaults
import app.getnuri.util.AnimatedTextField
import app.getnuri.util.dashedRoundedRectBorder
import app.getnuri.util.isAtLeastMedium
import kotlinx.coroutines.launch
import app.getnuri.theme.R as ThemeR

// Enum for meal input types
enum class MealInputType(val displayName: String) {
    PHOTO("Photo"),
    TEXT("Description")
}

// Mock state for demonstration - in real implementation this would come from ViewModel
data class MealTrackingState(
    val screenState: MealScreenState = MealScreenState.INPUT,
    val selectedInputType: MealInputType = MealInputType.PHOTO,
    val imageUri: Uri? = null,
    val descriptionText: TextFieldState = TextFieldState(),
)

enum class MealScreenState {
    INPUT,
    LOADING,
    RESULT
}

@Composable
fun MealTrackingChoiceScreen(
    fileName: String? = null,
    isMedium: Boolean = isAtLeastMedium(),
    onCameraPressed: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onMealLogged: (Uri?, String) -> Unit = { _, _ -> },
    onAboutClicked: () -> Unit = {},
) {
    // Mock state - in real implementation this would come from a ViewModel
    var uiState by remember { mutableStateOf(MealTrackingState()) }
    
    BackHandler(
        enabled = uiState.screenState != MealScreenState.INPUT,
    ) {
        uiState = uiState.copy(screenState = MealScreenState.INPUT)
    }
    
    LaunchedEffect(Unit) {
        if (fileName != null) {
            uiState = uiState.copy(imageUri = fileName.toUri())
        }
    }
    
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            uiState = uiState.copy(imageUri = uri)
        }
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    when (uiState.screenState) {
        MealScreenState.INPUT -> {
            MealInputScreen(
                snackbarHostState = snackbarHostState,
                isExpanded = isMedium,
                onCameraPressed = onCameraPressed,
                onBackPressed = onBackPressed,
                uiState = uiState,
                onChooseImageClicked = { pickMedia.launch(PickVisualMediaRequest(it)) },
                onInputTypeSelected = { uiState = uiState.copy(selectedInputType = it) },
                onUndoPressed = { uiState = uiState.copy(imageUri = null) },

                onTrackMealClicked = { 
                    val description = uiState.descriptionText.text.toString()
                    onMealLogged(uiState.imageUri, description)
                },
                onAboutClicked = onAboutClicked,
            )
        }
        
        MealScreenState.LOADING -> {
            LoadingScreen(
                onCancelPress = {
                    uiState = uiState.copy(screenState = MealScreenState.INPUT)
                },
            )
        }
        
        MealScreenState.RESULT -> {
            // Result screen would be implemented based on app's needs
        }
    }
}

@Composable
fun MealInputScreen(
    snackbarHostState: SnackbarHostState,
    isExpanded: Boolean,
    onCameraPressed: () -> Unit,
    onBackPressed: () -> Unit,
    uiState: MealTrackingState,
    onChooseImageClicked: (PickVisualMedia.VisualMediaType) -> Unit,
    onInputTypeSelected: (MealInputType) -> Unit,
    onUndoPressed: () -> Unit,

    onTrackMealClicked: () -> Unit,
    onAboutClicked: () -> Unit,
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
                backEnabled = false,
                aboutEnabled = true,
                isMediumWindowSize = isExpanded,
                useNuriStyling = true,
                onAboutClicked = onAboutClicked,
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
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                if (!isExpanded) {
                    MealInputTypeToolbar(
                        uiState.selectedInputType,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        onOptionSelected = onInputTypeSelected,
                    )
                }
                
                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                    ) {
                        MainMealInputPane(
                            uiState,
                            modifier = Modifier.weight(1f),
                            onCameraPressed = onCameraPressed,
                            onChooseImageClicked = {
                                onChooseImageClicked(PickVisualMedia.ImageOnly)
                            },
                            onUndoPressed = onUndoPressed,

                            onSelectedInputTypeChanged = onInputTypeSelected,
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        TrackMealButton(
                            modifier = Modifier.padding(bottom = 8.dp),
                            buttonText = stringResource(R.string.track_meal_button),
                            onClicked = onTrackMealClicked,
                        )
                    }
                } else {
                    MainMealInputPane(
                        uiState,
                        modifier = Modifier.weight(1f),
                        onCameraPressed = onCameraPressed,
                        onChooseImageClicked = {
                            onChooseImageClicked(PickVisualMedia.ImageOnly)
                        },
                        onUndoPressed = onUndoPressed,

                        onSelectedInputTypeChanged = onInputTypeSelected,
                    )
                    
                    TrackMealButton(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                        onClicked = onTrackMealClicked,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun MainMealInputPane(
    uiState: MealTrackingState,
    modifier: Modifier = Modifier,
    onCameraPressed: () -> Unit,
    onChooseImageClicked: () -> Unit = {},
    onUndoPressed: () -> Unit = {},
    onSelectedInputTypeChanged: (MealInputType) -> Unit,
) {
    Box(modifier = modifier) {
        val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
        val pagerState = rememberPagerState(0) { MealInputType.entries.size }
        val focusManager = LocalFocusManager.current
        
        LaunchedEffect(uiState.selectedInputType) {
            launch {
                pagerState.animateScrollToPage(
                    uiState.selectedInputType.ordinal,
                    animationSpec = spatialSpec,
                )
            }.invokeOnCompletion {
                if (uiState.selectedInputType != MealInputType.entries[pagerState.currentPage]) {
                    onSelectedInputTypeChanged(MealInputType.entries[pagerState.currentPage])
                }
            }
        }
        
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onSelectedInputTypeChanged(MealInputType.entries[page])
            }
        }
        
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.targetPage }.collect {
                if (pagerState.targetPage != MealInputType.TEXT.ordinal) {
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
                MealInputType.PHOTO.ordinal -> {
                    val imageUri = uiState.imageUri
                    if (imageUri == null) {
                        MealPhotoEmptyState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp),
                            onCameraPressed = onCameraPressed,
                            onChooseImagePress = onChooseImageClicked,
                        )
                    } else {
                        MealImagePreview(
                            imageUri,
                            onUndoPressed,
                            onChooseImagePressed = onChooseImageClicked,
                            modifier = Modifier
                                .fillMaxSize()
                                .heightIn(min = 200.dp),
                        )
                    }
                }
                
                MealInputType.TEXT.ordinal -> {
                    MealTextPrompt(
                        textFieldState = uiState.descriptionText,
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
private fun TrackMealButton(
    modifier: Modifier = Modifier,
    buttonText: String = "Track Meal",
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
                    Icons.Default.Fastfood,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
fun MealImagePreview(
    uri: Uri,
    onUndoPressed: () -> Unit,
    onChooseImagePressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedElementScope = LocalSharedTransitionScope.current
    with(sharedElementScope) {
        Box(modifier) {
            AsyncImage(
                ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(false)
                    .build(),
                placeholder = null,
                contentDescription = "Selected meal photo",
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
                    onClick = onUndoPressed,
                    leadingIcon = {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Retake photo",
                        )
                    },
                )
                Spacer(modifier = Modifier.width(8.dp))
                SecondaryOutlinedButton(
                    onClick = onChooseImagePressed,
                    buttonText = "Choose Photo",
                    leadingIcon = {
                        Icon(
                            painterResource(android.R.drawable.ic_menu_gallery),
                            contentDescription = "Choose photo",
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun MealTextPrompt(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            textStyle = TextStyle(fontSize = 24.sp),
            decorator = { innerTextField ->
                if (textFieldState.text.isEmpty()) {
                    Text(
                        "What did you eat? Be as detailed as you'd like...",
                        color = Color(0xFF4A148C),
                        fontSize = 24.sp,
                    )
                }
                innerTextField()
            },
        )
    }
}



@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun MealInputTypeToolbar(
    selectedOption: MealInputType,
    modifier: Modifier = Modifier,
    onOptionSelected: (MealInputType) -> Unit,
) {
    val options = MealInputType.entries
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

@Composable
private fun MealPhotoEmptyState(
    onCameraPressed: () -> Unit,
    onChooseImagePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
            )
            .dashedRoundedRectBorder(
                2.dp,
                MaterialTheme.colorScheme.outline,
                cornerRadius = 28.dp,
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Take a photo of your meal",
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
            onClick = onChooseImagePress,
            leadingIcon = {
                Icon(
                    painterResource(android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp),
                )
            },
            buttonText = "Choose from Gallery",
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
                    indication = androidx.compose.material3.ripple(color = Color.White),
                    onClick = onCameraPressed,
                    role = Role.Button,
                    enabled = true,
                    onClickLabel = "Take photo of meal",
                )
                .sharedBoundsRevealWithShapeMorph(
                    rememberSharedContentState(SharedElementKey.CameraButtonToFullScreenCamera),
                    restingShape = MaterialShapes.Cookie9Sided,
                    targetShape = RoundedPolygon.rectangle().normalized(),
                    targetValueByState = {
                        when (it) {
                            androidx.compose.animation.EnterExitState.PreEnter -> 0f
                            androidx.compose.animation.EnterExitState.Visible -> 1f
                            androidx.compose.animation.EnterExitState.PostExit -> 1f
                        }
                    },
                ),
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Take photo of meal",
                modifier = Modifier
                    .sizeIn(minHeight = 24.dp, maxHeight = 58.dp)
                    .padding(8.dp)
                    .aspectRatio(1f)
                    .align(Alignment.Center),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun LoadingScreen(
    onCancelPress: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Processing your meal...",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryOutlinedButton(
                onClick = onCancelPress,
                buttonText = "Cancel"
            )
        }
    }
}

@Preview
@Composable
private fun MealTrackingChoiceScreenPreview() {
    AndroidifyTheme {
        MealTrackingChoiceScreen()
    }
} 