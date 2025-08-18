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
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.data.ImageGenerationRepository
import com.android.developers.androidify.util.LocalFileProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomizeExportViewModel.Factory::class)
class CustomizeExportViewModel @AssistedInject constructor(
    @Assisted("resultImageUrl") val resultImageUrl: Uri,
    @Assisted("originalImageUrl") val originalImageUrl: Uri?,
    val imageGenerationRepository: ImageGenerationRepository,
    val composableBitmapRenderer: ComposableBitmapRenderer,
    val localFileProvider: LocalFileProvider,
    application: Application,
) : AndroidViewModel(application) {

    @AssistedFactory
    interface Factory{
        fun create(
            @Assisted("resultImageUrl") resultImageUrl: Uri,
            @Assisted("originalImageUrl")originalImageUrl: Uri?
        ): CustomizeExportViewModel
    }

    private val _state = MutableStateFlow(CustomizeExportState())
    val state = _state.asStateFlow()

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    init {
        _state.update {
            it.copy(
                originalImageUrl = originalImageUrl,
                exportImageCanvas = it.exportImageCanvas.copy(imageUri = resultImageUrl),
            )
        }
        loadInitialBitmap(resultImageUrl)
    }


    override fun onCleared() {
        super.onCleared()
    }

    fun setArguments(
        resultImageUrl: Uri,
        originalImageUrl: Uri?,
    ) {
        _state.update {
            CustomizeExportState(
                originalImageUrl,
                exportImageCanvas = it.exportImageCanvas.copy(imageUri = resultImageUrl),
            )
        }
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
                Log.e("CustomizeExportViewModel", "Background removal failed", exception)
                snackbarHostState.value.showSnackbar("Background removal failed")
                _state.update {
                    val aspectRatioToolState = (it.toolState[CustomizeTool.Size] as AspectRatioToolState)
                        .copy(selectedToolOption =  previousSizeOption)
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
                    image, backgroundOption.prompt,
                )
                _state.update {
                    it.copy(
                        exportImageCanvas = it.exportImageCanvas.copy(imageWithEdit = bitmap),
                    )
                }
            } catch (e: Exception) {
                Log.e("CustomizeExportViewModel", "Image generation failed", e)
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
                val savedOriginalUri =
                    imageGenerationRepository.saveImageToExternalStorage(originalImage)
                _state.update {
                    it.copy(externalOriginalSavedUri = savedOriginalUri)
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

    private fun loadInitialBitmap(uri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = localFileProvider.loadBitmapFromUri(uri)
                _state.update {
                    it.copy(
                        exportImageCanvas = it.exportImageCanvas.copy(imageBitmap = bitmap)
                    )
                }
            } catch (e: Exception) {
                _snackbarHostState.value.showSnackbar("Could not load image.")
            }
        }
    }
}
