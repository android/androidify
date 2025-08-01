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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeExportViewModel @Inject constructor(
    val imageGenerationRepository: ImageGenerationRepository,
    val composableBitmapRenderer: ComposableBitmapRenderer,
    application: Application,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CustomizeExportState())
    val state = _state.asStateFlow()

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    override fun onCleared() {
        super.onCleared()
        composableBitmapRenderer.dispose()
    }
    fun setArguments(
        resultImageUrl: Bitmap,
        originalImageUrl: Uri?,
    ) {
        _state.update {
            CustomizeExportState(
                originalImageUrl,
                exportImageCanvas = it.exportImageCanvas.copy(imageBitmap = resultImageUrl),
            )
        }
    }

    fun shareClicked() {
        viewModelScope.launch {
            val exportImageCanvas = state.value.exportImageCanvas
            val resultBitmap = composableBitmapRenderer.renderComposableToBitmap(exportImageCanvas.canvasSize) {
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
                _state.update {
                    it.copy(
                        toolState = it.toolState + (it.selectedTool to toolState),
                        exportImageCanvas = it.exportImageCanvas.updateAspectRatioAndBackground(
                            it.exportImageCanvas.selectedBackgroundOption,
                            (toolState.selectedToolOption as SizeOption),
                        ),
                    )
                }
            }
            else -> throw IllegalArgumentException("Unknown tool option")
        }
    }

    private fun triggerAiBackgroundGeneration(backgroundOption: BackgroundOption) {
        viewModelScope.launch(Dispatchers.IO) {
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
                val bitmap = imageGenerationRepository.generateImageWithEdit(
                    image,
                    backgroundOption.prompt,
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
}
