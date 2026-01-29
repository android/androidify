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
@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)

package com.android.developers.androidify.customize

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.android.developers.androidify.customize.watchface.WatchFaceModalSheet
import com.android.developers.androidify.customize.xr.CustomizeExportLayoutSpatial
import com.android.developers.androidify.results.PermissionRationaleDialog
import com.android.developers.androidify.results.R
import com.android.developers.androidify.results.shareImage
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LocalAnimateBoundsScope
import com.android.developers.androidify.theme.components.AboutButton
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.PrimaryButton
import com.android.developers.androidify.theme.components.SecondaryOutlinedButton
import com.android.developers.androidify.theme.transitions.loadingShimmerOverlay
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.allowsFullContent
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.wear.common.ConnectedWatch
import com.android.developers.androidify.xr.RequestFullSpaceIconButton
import com.android.developers.androidify.xr.RequestHomeSpaceIconButton
import com.android.developers.androidify.xr.couldRequestFullSpace
import com.android.developers.androidify.xr.couldRequestHomeSpace
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.android.developers.androidify.theme.R as ThemeR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeAndExportScreen(
    onBackPress: () -> Unit,
    onInfoPress: () -> Unit,
    viewModel: CustomizeExportViewModel,
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(state.value.savedUri) {
        val savedImageUri = state.value.savedUri
        if (savedImageUri != null) {
            shareImage(context, savedImageUri)
            viewModel.onSavedUriConsumed()
        }
    }

    val layoutType = calculateLayoutType(enableXr = state.value.xrEnabled)
    CustomizeExportContents(
        state.value,
        onBackPress,
        onInfoPress,
        onToolSelected = { tool ->
            viewModel.changeSelectedTool(tool)
        },
        onShareClicked = viewModel::shareClicked,
        onDownloadClicked = viewModel::downloadClicked,
        onSelectedToolStateChanged = viewModel::selectedToolStateChanged,
        onInstallWatchFaceClicked = {
            viewModel.installWatchFace()
        },
        onInstallAndroidifyClicked = {
            viewModel.launchPlayInstallOnWatch()
        },
        onResetWatchFaceSend = {
            viewModel.resetWatchFaceSend()
        },
        layoutType = layoutType,
        snackbarHostState = viewModel.snackbarHostState.collectAsStateWithLifecycle().value,
        loadWatchFaces = viewModel::loadWatchFaces,
        onWatchFaceSelect = viewModel::onWatchFaceSelected,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CustomizeExportContents(
    state: CustomizeExportState,
    onBackPress: () -> Unit,
    onInfoPress: () -> Unit,
    onShareClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    onToolSelected: (CustomizeTool) -> Unit,
    onSelectedToolStateChanged: (ToolState) -> Unit,
    onInstallWatchFaceClicked: () -> Unit,
    onInstallAndroidifyClicked: suspend () -> Boolean,
    onResetWatchFaceSend: () -> Unit,
    layoutType: CustomizeExportLayoutType,
    snackbarHostState: SnackbarHostState,
    loadWatchFaces: () -> Unit,
    onWatchFaceSelect: (WatchFaceAsset) -> Unit,
) {
    var showWatchFaceBottomSheet by remember { mutableStateOf(false) }
    val watchFaceSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val imageResult = remember(state.showImageEditProgress) {
        movableContentWithReceiverOf<ExportImageCanvas> {
            val chromeModifier = if (this.showSticker) {
                Modifier
                    .clip(RoundedCornerShape(6))
            } else {
                Modifier
                    .dropShadow(
                        RoundedCornerShape(6),
                        shadow = Shadow(
                            radius = 26.dp,
                            spread = 10.dp,
                            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.2f),
                        ),
                    )
                    .clip(RoundedCornerShape(6))
            }
            Box(
                Modifier
                    .padding(16.dp),
            ) {
                ImageResult(
                    this@movableContentWithReceiverOf,
                    modifier = Modifier,
                    outerChromeModifier = Modifier
                        .then(chromeModifier)
                        .loadingShimmerOverlay(
                            visible = state.showImageEditProgress,
                            clipShape = RoundedCornerShape(percent = 6),
                        ),
                )
            }
        }
    }
    val topBar = @Composable {
        AndroidifyTopAppBar(
            backEnabled = true,
            titleText = stringResource(R.string.customize_and_export),
            isMediumWindowSize = layoutType != CustomizeExportLayoutType.Compact,
            onBackPressed = onBackPress,
            actions = {
                AboutButton { onInfoPress() }
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
    val toolSelector: ToolSelectorComposable = @Composable { modifier, horizontal ->
        ToolSelector(
            tools = state.tools,
            selectedOption = state.selectedTool,
            modifier = modifier,
            horizontal = horizontal,
            onToolSelected = { tool ->
                onToolSelected(tool)
            },
        )
    }
    val toolDetail: ToolDetailComposable = @Composable { modifier, singleLine ->
        SelectedToolDetail(
            state,
            onSelectedToolStateChanged = { toolState ->
                onSelectedToolStateChanged(toolState)
            },
            singleLine = singleLine,
            modifier = modifier,
        )
    }
    val actionButtons = @Composable { modifier: Modifier ->
        BotActionsButtonRow(
            onShareClicked = {
                onShareClicked()
            },
            onDownloadClicked = {
                onDownloadClicked()
            },
            onWearDeviceClick = {
                showWatchFaceBottomSheet = true
            },
            hasWearDevice = state.connectedWatch != null,
            modifier = modifier,
        )
    }
    state.connectedWatch?.let { device ->
        if (showWatchFaceBottomSheet) {
            WatchFaceModalSheet(
                sheetState = watchFaceSheetState,
                onDismiss = {
                    onResetWatchFaceSend()
                    showWatchFaceBottomSheet = false
                },
                connectedWatch = device,
                installationStatus = state.watchFaceInstallationStatus,
                onWatchFaceInstallClick = {
                    onInstallWatchFaceClicked()
                },
                onAndroidifyInstallClick = {
                    onInstallAndroidifyClicked()
                },
                onLoad = loadWatchFaces,
                watchFaceSelectionState = state.watchFaceSelectionState,
                onWatchFaceSelect = onWatchFaceSelect,
            )
        }
    }

    when (layoutType) {
        CustomizeExportLayoutType.Medium -> CustomizeExportScreenScaffold(
            snackbarHostState,
            topBar = topBar,
            containerColor = MaterialTheme.colorScheme.surface,
        ) { paddingValues ->
            CustomizeExportLayoutMedium(
                paddingValues = paddingValues,
                imageResult = imageResult,
                state = state,
                toolDetail = toolDetail,
                toolSelector = toolSelector,
                actionButtons = actionButtons,
            )
        }

        CustomizeExportLayoutType.Compact -> CustomizeExportScreenScaffold(
            snackbarHostState,
            topBar = topBar,
            containerColor = MaterialTheme.colorScheme.surface,
        ) { paddingValues ->
            CustomizeExportLayoutCompact(
                paddingValues = paddingValues,
                imageResult = imageResult,
                state = state,
                toolSelector = toolSelector,
                toolDetail = toolDetail,
                actionButtons = actionButtons,
            )
        }

        CustomizeExportLayoutType.Spatial -> CustomizeExportLayoutSpatial(
            imageResult = imageResult,
            state = state,
            toolSelector = toolSelector,
            toolDetail = toolDetail,
            actionButtons = actionButtons,
            snackbarHostState = snackbarHostState,
            topBar = topBar,
        )
    }
}

@Composable
private fun CustomizeExportLayoutCompact(
    paddingValues: PaddingValues,
    imageResult: @Composable (ExportImageCanvas.() -> Unit),
    state: CustomizeExportState,
    toolDetail: ToolDetailComposable,
    toolSelector: ToolSelectorComposable,
    actionButtons: @Composable (Modifier) -> Unit,
) {
    LookaheadScope {
        CompositionLocalProvider(LocalAnimateBoundsScope provides this) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center,
                ) {
                    imageResult(
                        state.exportImageCanvas,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                toolSelector(Modifier, true)
                Spacer(modifier = Modifier.height(16.dp))
                toolDetail(Modifier, true)
                Spacer(modifier = Modifier.height(16.dp))
                actionButtons(Modifier)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CustomizeExportLayoutMedium(
    paddingValues: PaddingValues,
    imageResult: @Composable (ExportImageCanvas.() -> Unit),
    state: CustomizeExportState,
    toolDetail: ToolDetailComposable,
    toolSelector: ToolSelectorComposable,
    actionButtons: @Composable (Modifier) -> Unit,
) {
    LookaheadScope {
        CompositionLocalProvider(LocalAnimateBoundsScope provides this) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier.weight(0.6f),
                    contentAlignment = Alignment.Center,
                ) {
                    imageResult(
                        state.exportImageCanvas,
                    )
                }
                Column(
                    Modifier
                        .weight(0.4f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            toolDetail(Modifier.align(Alignment.CenterEnd), false)
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        toolSelector(Modifier.requiredSizeIn(minWidth = 56.dp), false)
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    actionButtons(
                        Modifier
                            .align(Alignment.End)
                            .padding(end = 16.dp),
                    )
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun SelectedToolDetail(
    state: CustomizeExportState,
    singleLine: Boolean,
    onSelectedToolStateChanged: (ToolState) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        state.selectedTool,
        modifier = modifier
            .wrapContentSize()
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.medium,
            ),
    ) { targetState ->
        val toolState = state.toolState[targetState]
        when (targetState) {
            CustomizeTool.Size -> {
                val aspectRatioToolState = toolState as AspectRatioToolState
                AspectRatioTool(
                    aspectRatioToolState.options,
                    aspectRatioToolState.selectedToolOption,
                    singleLine = singleLine,
                    onSizeOptionSelected = {
                        onSelectedToolStateChanged(aspectRatioToolState.copy(selectedToolOption = it))
                    },
                )
            }

            CustomizeTool.Background -> {
                val backgroundToolState = toolState as BackgroundToolState
                BackgroundTool(
                    backgroundToolState.options,
                    backgroundToolState.selectedToolOption,
                    singleLine = singleLine,
                    onBackgroundOptionSelected = {
                        onSelectedToolStateChanged(backgroundToolState.copy(selectedToolOption = it))
                    },
                )
            }
        }
    }
}

@Composable
private fun BotActionsButtonRow(
    onWearDeviceClick: () -> Unit,
    onShareClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    hasWearDevice: Boolean,
    modifier: Modifier = Modifier,
    verboseLayout: Boolean = allowsFullContent(),
) {
    Row(modifier.height(IntrinsicSize.Min)) {
        PrimaryButton(
            onClick = {
                onShareClicked()
            },
            leadingIcon = {
                Row {
                    Icon(
                        ImageVector
                            .vectorResource(ThemeR.drawable.sharp_share_24),
                        contentDescription = null, // decorative element
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            },
            buttonText = if (verboseLayout) stringResource(R.string.share_your_bot) else null,
        )
        Spacer(Modifier.width(8.dp))
        val externalStoragePermission = rememberPermissionState(
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        val mustGrantPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            false
        } else {
            !externalStoragePermission.status.isGranted
        }
        var showRationaleDialog by remember {
            mutableStateOf(false)
        }
        SecondaryOutlinedButton(
            onClick = {
                if (mustGrantPermission) {
                    if (externalStoragePermission.status.shouldShowRationale) {
                        showRationaleDialog = true
                    } else {
                        externalStoragePermission.launchPermissionRequest()
                    }
                    externalStoragePermission.launchPermissionRequest()
                } else {
                    onDownloadClicked()
                }
            },
            leadingIcon = {
                Icon(
                    ImageVector
                        .vectorResource(R.drawable.rounded_download_24),
                    contentDescription = stringResource(R.string.download_bot),
                )
            },
            modifier = Modifier.fillMaxHeight(),
        )
        if (hasWearDevice) {
            Spacer(Modifier.width(8.dp))
            SecondaryOutlinedButton(
                onClick = onWearDeviceClick,
                leadingIcon = {
                    Icon(
                        ImageVector
                            .vectorResource(R.drawable.watch_24),
                        contentDescription = stringResource(R.string.send_to_watch),
                    )
                },
                modifier = Modifier.fillMaxHeight(),
            )
        }
        PermissionRationaleDialog(
            showRationaleDialog,
            onDismiss = {
                showRationaleDialog = false
            },
            externalStoragePermission,
        )
    }
}

typealias ToolSelectorComposable = @Composable (modifier: Modifier, horizontal: Boolean) -> Unit
typealias ToolDetailComposable = @Composable (modifier: Modifier, singleLine: Boolean) -> Unit

@Composable
fun CustomizeExportScreenScaffold(
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
        modifier = modifier,
        content = content,
    )
}

@Preview(showBackground = true)
@PhonePreview
@Composable
fun CustomizeExportPreview() {
    AndroidifyTheme {
        AnimatedContent(true) { targetState ->
            targetState
            CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                val connectedWatch = ConnectedWatch(
                    nodeId = "1234",
                    displayName = "Pixel Watch 3",
                    hasAndroidify = true,
                )
                val bitmap = getPlaceholderBotBitmap()
                val state = CustomizeExportState(
                    exportImageCanvas = ExportImageCanvas(imageBitmap = bitmap),
                    connectedWatch = connectedWatch,
                )
                CustomizeExportContents(
                    state = state,
                    onDownloadClicked = {},
                    onShareClicked = {},
                    onBackPress = {},
                    onInfoPress = {},
                    onToolSelected = {},
                    snackbarHostState = SnackbarHostState(),
                    layoutType = CustomizeExportLayoutType.Compact,
                    onSelectedToolStateChanged = {},
                    onInstallWatchFaceClicked = {},
                    onInstallAndroidifyClicked = { true },
                    onResetWatchFaceSend = {},
                    loadWatchFaces = {},
                    onWatchFaceSelect = {},
                )
            }
        }
    }
}

@LargeScreensPreview
@Composable
fun CustomizeExportPreviewLarge() {
    AndroidifyTheme {
        AnimatedContent(true) { targetState ->
            targetState
            CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                val bitmap = getPlaceholderBotBitmap()
                val connectedWatch = ConnectedWatch(
                    nodeId = "1234",
                    displayName = "Pixel Watch 3",
                    hasAndroidify = true,
                )
                val state = CustomizeExportState(
                    exportImageCanvas = ExportImageCanvas(
                        imageBitmap = bitmap,
                        aspectRatioOption = SizeOption.Square,
                    ),
                    selectedTool = CustomizeTool.Background,
                    connectedWatch = connectedWatch,
                )
                CustomizeExportContents(
                    state = state,
                    onDownloadClicked = {},
                    onShareClicked = {},
                    onBackPress = {},
                    onInfoPress = {},
                    onToolSelected = {},
                    snackbarHostState = SnackbarHostState(),
                    layoutType = CustomizeExportLayoutType.Medium,
                    onSelectedToolStateChanged = {},
                    onInstallWatchFaceClicked = {},
                    onInstallAndroidifyClicked = { true },
                    onResetWatchFaceSend = {},
                    loadWatchFaces = {},
                    onWatchFaceSelect = {},
                )
            }
        }
    }
}
