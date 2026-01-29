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
package com.android.developers.androidify.customize

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.customize.watchface.WatchFaceSelectionState
import com.android.developers.androidify.data.ImageGenerationRepository
import com.android.developers.androidify.util.LocalFileProvider
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.watchface.transfer.WatchFaceInstallationRepository
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.collections.isNotEmpty

@HiltViewModel(assistedFactory = CustomizeExportViewModel.Factory::class)
class CustomizeExportViewModel @AssistedInject constructor(
    @Assisted("resultImageUrl") val resultImageUrl: Uri,
    @Assisted("originalImageUrl") val originalImageUrl: Uri?,
    val imageGenerationRepository: ImageGenerationRepository,
    val composableBitmapRenderer: ComposableBitmapRenderer,
    val watchfaceInstallationRepository: WatchFaceInstallationRepository,
    val localFileProvider: LocalFileProvider,
    val remoteConfigDataSource: RemoteConfigDataSource,
    application: Application,
) : AndroidViewModel(application) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("resultImageUrl") resultImageUrl: Uri,
            @Assisted("originalImageUrl")originalImageUrl: Uri?,
        ): CustomizeExportViewModel
    }

    private val _state = MutableStateFlow(CustomizeExportState(xrEnabled = remoteConfigDataSource.isXrEnabled()))
    val state: StateFlow<CustomizeExportState> = combine(
        _state,
        watchfaceInstallationRepository.connectedWatch,
        watchfaceInstallationRepository.watchFaceInstallationUpdates,
    ) {
            currentState, watch, installationStatus ->
        currentState.copy(
            connectedWatch = watch,
            watchFaceInstallationStatus = installationStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = _state.value,
    )

    private var transferJob: Job? = null

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    init {
        val enableBackgroundVibes = remoteConfigDataSource.isBackgroundVibesFeatureEnabled()
        var backgrounds = mutableListOf(
            BackgroundOption.None,
            BackgroundOption.Plain,
            BackgroundOption.Lightspeed,
            BackgroundOption.IO,
        )
        if (enableBackgroundVibes) {
            val backgroundVibes = listOf(
                BackgroundOption.MusicLover,
                BackgroundOption.PoolMaven,
                BackgroundOption.SoccerFanatic,
                BackgroundOption.StarGazer,
                BackgroundOption.FitnessBuff,
                BackgroundOption.Fandroid,
                BackgroundOption.GreenThumb,
                BackgroundOption.Gamer,
                BackgroundOption.Jetsetter,
                BackgroundOption.Chef,
            )
            backgrounds.addAll(backgroundVibes)
        }

        _state.update {
            it.copy(
                originalImageUrl = originalImageUrl,
                toolState = mapOf(
                    CustomizeTool.Size to AspectRatioToolState(),
                    CustomizeTool.Background to BackgroundToolState(
                        options = backgrounds,
                    ),
                ),
            )
        }
        loadInitialBitmap(resultImageUrl)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun shareClicked() {
        viewModelScope.launch {
            val exportImageCanvas = state.value.exportImageCanvas
            val resultBitmap =
                composableBitmapRenderer.renderComposableToBitmap(exportImageCanvas.canvasSize) {
                    ImageResult(
                        exportImageCanvas = exportImageCanvas,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            if (resultBitmap != null) {
                val imageFileUri = imageGenerationRepository.saveImage(resultBitmap)

                _state.update {
                    it.copy(savedUri = imageFileUri)
                }
            }
        }
    }

    fun onSavedUriConsumed() {
        _state.update {
            it.copy(savedUri = null)
        }
    }

    private fun triggerStickerBackgroundRemoval(bitmap: Bitmap, previousSizeOption: SizeOption) {
        viewModelScope.launch {
            try {
                val stickerBitmap = imageGenerationRepository.removeBackground(
                    bitmap,
                )
                _state.update {
                    it.copy(
                        showImageEditProgress = false,
                        exportImageCanvas = it.exportImageCanvas.copy(imageBitmapRemovedBackground = stickerBitmap)
                            .updateAspectRatioAndBackground(
                                it.exportImageCanvas.selectedBackgroundOption,
                                SizeOption.Sticker,
                            ),
                    )
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Background removal failed")
                snackbarHostState.value.showSnackbar("Background removal failed")
                _state.update {
                    val aspectRatioToolState = (it.toolState[CustomizeTool.Size] as AspectRatioToolState)
                        .copy(selectedToolOption = previousSizeOption)
                    it.copy(
                        toolState = it.toolState + (CustomizeTool.Size to aspectRatioToolState),
                        showImageEditProgress = false,
                        exportImageCanvas = it.exportImageCanvas.copy(imageBitmapRemovedBackground = null)
                            .updateAspectRatioAndBackground(
                                it.exportImageCanvas.selectedBackgroundOption,
                                previousSizeOption,
                            ),
                    )
                }
            }
        }
    }

    fun selectedToolStateChanged(toolState: ToolState) {
        when (toolState.selectedToolOption) {
            is BackgroundOption -> {
                val backgroundOption = toolState.selectedToolOption as BackgroundOption
                _state.update {
                    it.copy(
                        toolState = it.toolState + (it.selectedTool to toolState),
                        exportImageCanvas = it.exportImageCanvas.updateAspectRatioAndBackground(
                            backgroundOption,
                            it.exportImageCanvas.aspectRatioOption,
                        ),
                    )
                }
                if (backgroundOption.aiBackground) {
                    triggerAiBackgroundGeneration(backgroundOption)
                } else {
                    _state.update {
                        it.copy(
                            exportImageCanvas = it.exportImageCanvas.copy(imageWithEdit = null),
                        )
                    }
                }
            }
            is SizeOption -> {
                val selectedSizeOption = toolState.selectedToolOption as SizeOption
                val needsBackgroundRemoval = selectedSizeOption == SizeOption.Sticker &&
                    state.value.exportImageCanvas.imageBitmapRemovedBackground == null

                val imageBitmap = state.value.exportImageCanvas.imageBitmap
                if (needsBackgroundRemoval && imageBitmap != null) {
                    val previousSizeOption = state.value.exportImageCanvas.aspectRatioOption
                    _state.update {
                        it.copy(
                            toolState = it.toolState + (it.selectedTool to toolState),
                            showImageEditProgress = true,
                            exportImageCanvas = it.exportImageCanvas.updateAspectRatioAndBackground(
                                it.exportImageCanvas.selectedBackgroundOption,
                                SizeOption.Sticker,
                            ),
                        )
                    }
                    triggerStickerBackgroundRemoval(imageBitmap, previousSizeOption)
                } else {
                    _state.update {
                        it.copy(
                            toolState = it.toolState + (it.selectedTool to toolState),
                            showImageEditProgress = false,
                            exportImageCanvas = it.exportImageCanvas.updateAspectRatioAndBackground(
                                it.exportImageCanvas.selectedBackgroundOption,
                                selectedSizeOption,
                            ),
                        )
                    }
                }
            }
            else -> throw IllegalArgumentException("Unknown tool option")
        }
    }

    private fun triggerAiBackgroundGeneration(backgroundOption: BackgroundOption) {
        viewModelScope.launch {
            if (backgroundOption.prompt == null) {
                _state.update {
                    it.copy(
                        showImageEditProgress = false,
                        exportImageCanvas = it.exportImageCanvas.copy(imageWithEdit = null),
                    )
                }
                return@launch
            }
            val image = state.value.exportImageCanvas.imageBitmap
            if (image == null) {
                return@launch
            }

            _state.update { it.copy(showImageEditProgress = true) }
            try {
                val bitmap = imageGenerationRepository.addBackgroundToBot(
                    image,
                    backgroundOption.prompt,
                )
                _state.update {
                    it.copy(
                        exportImageCanvas = it.exportImageCanvas.copy(imageWithEdit = bitmap),
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Image generation failed")
                snackbarHostState.value.showSnackbar("Background vibe generation failed")
            } finally {
                _state.update { it.copy(showImageEditProgress = false) }
            }
        }
    }

    fun downloadClicked() {
        viewModelScope.launch {
            val exportImageCanvas = state.value.exportImageCanvas
            val resultBitmap = composableBitmapRenderer.renderComposableToBitmap(exportImageCanvas.canvasSize) {
                ImageResult(
                    exportImageCanvas = exportImageCanvas,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            val originalImage = state.value.originalImageUrl
            if (originalImage != null) {
                try {
                    val savedOriginalUri =
                        imageGenerationRepository.saveImageToExternalStorage(originalImage)
                    _state.update {
                        it.copy(externalOriginalSavedUri = savedOriginalUri)
                    }
                } catch (exception: Exception) {
                    Timber.d(exception, "Original image save failed: ")
                }
            }
            if (resultBitmap != null) {
                val imageUri = imageGenerationRepository.saveImageToExternalStorage(resultBitmap)
                _state.update {
                    it.copy(externalSavedUri = imageUri)
                }
                snackbarHostState.value.showSnackbar("Download complete")
            }
        }
    }

    fun changeSelectedTool(tool: CustomizeTool) {
        _state.update {
            it.copy(selectedTool = tool)
        }
    }

    fun loadWatchFaces() {
        if (_state.value.watchFaceSelectionState.watchFaces.isNotEmpty()) return

        _state.update { it.copy(watchFaceSelectionState = it.watchFaceSelectionState.copy(isLoadingWatchFaces = true)) }

        viewModelScope.launch {
            watchfaceInstallationRepository.getAvailableWatchFaces()
                .onSuccess { faces ->
                    _state.update {
                        it.copy(
                            watchFaceSelectionState = WatchFaceSelectionState(
                                watchFaces = faces,
                                isLoadingWatchFaces = false,
                                selectedWatchFace = faces.firstOrNull(),
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            watchFaceSelectionState = it.watchFaceSelectionState.copy(
                                isLoadingWatchFaces = false,
                            ),
                        )
                    }
                }
        }
    }

    fun onWatchFaceSelected(watchFace: WatchFaceAsset) {
        _state.update {
            it.copy(
                watchFaceSelectionState = it.watchFaceSelectionState.copy(
                    selectedWatchFace = watchFace,
                ),
            )
        }
    }

    suspend fun launchPlayInstallOnWatch(): Boolean {
        try {
            val watch = state.value.connectedWatch
            watch?.let {
                watchfaceInstallationRepository.installAndroidify(it.nodeId)
            }
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to open Play Store on watch")
        }
        return false
    }

    fun installWatchFace() {
        val watchFaceToInstall = _state.value.watchFaceSelectionState.selectedWatchFace ?: return
        val bitmap = state.value.exportImageCanvas.imageBitmap
        val watch = state.value.connectedWatch
        if (watch != null && bitmap != null) {
            transferJob = viewModelScope.launch(Dispatchers.Default) {
                watchfaceInstallationRepository.prepareForTransfer()
                val wfBitmap = imageGenerationRepository.removeBackground(bitmap)
                val response = watchfaceInstallationRepository
                    .createAndTransferWatchFace(watch, watchFaceToInstall, wfBitmap)

                if (response != WatchFaceInstallError.NO_ERROR) {
                    _state.update {
                        it.copy(
                            watchFaceInstallationStatus = WatchFaceInstallationStatus.Complete(
                                success = false,
                                installError = response,
                                otherNodeId = watch.nodeId,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun resetWatchFaceSend() {
        transferJob?.cancel()
        transferJob = null
        viewModelScope.launch {
            watchfaceInstallationRepository.resetInstallationStatus()
        }
    }

    private fun loadInitialBitmap(uri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = localFileProvider.loadBitmapFromUri(uri)
                _state.update {
                    it.copy(
                        exportImageCanvas = it.exportImageCanvas.copy(imageBitmap = bitmap),
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Could not load Bitmap from the URI due to ${e.message}")
                _snackbarHostState.value.showSnackbar("Could not load image.")
            }
        }
    }
}
