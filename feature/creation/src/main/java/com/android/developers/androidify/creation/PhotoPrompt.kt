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
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LocalSharedTransitionScope
import com.android.developers.androidify.theme.R
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.SharedElementKey
import com.android.developers.androidify.theme.components.ScaleIndicationNodeFactory
import com.android.developers.androidify.theme.components.SecondaryOutlinedButton
import com.android.developers.androidify.theme.sharedBoundsRevealWithShapeMorph
import com.android.developers.androidify.theme.sharedBoundsWithDefaults
import com.android.developers.androidify.util.dashedRoundedRectBorder
import com.android.developers.androidify.util.isHorizontalWindow
import com.android.developers.androidify.creation.R as CreationR

@Composable
fun PhotoPrompt(
    uiState: CreationState,
    dropBehaviourFactory: DropBehaviourFactory,
    onCameraPressed: () -> Unit,
    onChooseImageClicked: () -> Unit,
    onDropCallback: (Uri) -> Unit,
    onUndoPressed: () -> Unit,
) {
    val defaultDropAreaBackgroundColor = MaterialTheme.colorScheme.surface
    val alternateDropAreaBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    var background by remember { mutableStateOf(defaultDropAreaBackgroundColor) }

    val activity = LocalActivity.current as? ComponentActivity
    val externalAppCallback = remember {
        dropBehaviourFactory.createTargetCallback(
            activity = activity ?: return@remember null,
            onImageDropped = { uri -> onDropCallback(uri) },
            onDropStarted = { background = alternateDropAreaBackgroundColor },
            onDropEnded = { background = defaultDropAreaBackgroundColor },
        )
    }

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
                .run {
                    if (externalAppCallback == null) this
                    else dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            dropBehaviourFactory.shouldStartDragAndDrop(
                                event,
                            )
                        },
                        target = externalAppCallback,
                    )
                }
                .fillMaxSize()
                .padding(2.dp),
            onCameraPressed = onCameraPressed,
            onChooseImagePress = onChooseImageClicked,
        )
    } else {
        ImagePreviewUri(
            uri = imageUri,
            onUndoPressed = onUndoPressed,
            onChooseImagePressed = onChooseImageClicked,
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = 200.dp),
        )
    }
}

@Composable
private fun UploadEmptyState(
    onCameraPressed: () -> Unit,
    onChooseImagePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isHorizontalWindow()) {
        HorizontallyAlignedUploadEmptyState(
            onCameraPressed = onCameraPressed,
            onChooseImagePress = onChooseImagePress,
            modifier = modifier,
        )
    } else {
        VerticallyAlignedUploadEmptyState(
            onCameraPressed = onCameraPressed,
            onChooseImagePress = onChooseImagePress,
            modifier = modifier,
        )
    }
}

@Composable
private fun VerticallyAlignedUploadEmptyState(
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
private fun HorizontallyAlignedUploadEmptyState(
    onCameraPressed: () -> Unit,
    onChooseImagePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TakePhotoButton(onCameraPressed)
        Text(
            stringResource(CreationR.string.photo_picker_title),
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            minLines = 2,
            maxLines = 2,
        )
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

@Composable
private fun ImagePreviewUri(
    modifier: Modifier = Modifier,
    uri: Uri,
    onUndoPressed: () -> Unit,
    onChooseImagePressed: () -> Unit,
) {
    val sharedElementScope = LocalSharedTransitionScope.current
    with(sharedElementScope) {
        ImagePreview(modifier, onUndoPressed, onChooseImagePressed) {
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
        }
    }
}

@Composable
private fun ImagePreview(
    modifier: Modifier = Modifier,
    onUndoPressed: () -> Unit,
    onChooseImagePressed: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier) {
        content(this@Box)

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

@Preview
@Composable
fun UploadEmptyPreview() {
    AndroidifyTheme {
        SharedElementContextPreview {
            UploadEmptyState(
                onCameraPressed = {},
                onChooseImagePress = {},
            )
        }
    }
}

@Preview
@Composable
fun ImagePreviewPreview() {
    AndroidifyTheme {
        SharedElementContextPreview {
            ImagePreview(
                onUndoPressed = {},
                onChooseImagePressed = {},
            ) {
                val bitmap =
                    ImageBitmap.imageResource(com.android.developers.androidify.results.R.drawable.placeholderbot)
                Image(bitmap = bitmap, contentDescription = null)
            }
        }
    }
}
