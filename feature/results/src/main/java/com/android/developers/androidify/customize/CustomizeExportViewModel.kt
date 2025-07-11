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
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.data.ImageGenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeExportViewModel @Inject constructor(
    val imageGenerationRepository: ImageGenerationRepository,
    application: Application,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CustomizeExportState())
    val state = _state.asStateFlow()

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

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
            val resultUrl = renderToBitmap(application, state.value.exportImageCanvas)
            if (resultUrl != null) {
                val imageFileUri = imageGenerationRepository.saveImage(resultUrl)

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
        _state.update {
            it.copy(
                toolState = it.toolState + (it.selectedTool to toolState),
                exportImageCanvas =
                when (toolState.selectedToolOption) {
                    is BackgroundOption -> {
                        val backgroundOption = toolState.selectedToolOption as BackgroundOption
                        it.exportImageCanvas.copy(selectedBackgroundOption = backgroundOption)
                    }
                    is SizeOption -> {
                        it.exportImageCanvas.copy(
                            aspectRatioOption = (toolState.selectedToolOption as SizeOption),
                        )
                    }
                    else -> throw IllegalArgumentException("Unknown tool option")
                },
            )
        }
    }
    fun downloadClicked() {
        viewModelScope.launch {
            val resultBitmap = renderToBitmap(application, state.value.exportImageCanvas)
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
