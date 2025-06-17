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
package com.android.developers.androidify.creation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.data.ImageDescriptionFailedGenerationException
import com.android.developers.androidify.data.ImageGenerationRepository
import com.android.developers.androidify.data.ImageValidationError
import com.android.developers.androidify.data.ImageValidationException
import com.android.developers.androidify.data.InsufficientInformationException
import com.android.developers.androidify.data.InternetConnectivityManager
import com.android.developers.androidify.data.NoInternetException
import com.android.developers.androidify.data.TextGenerationRepository
import com.android.developers.androidify.util.LocalFileProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreationViewModel @Inject constructor(
    val internetConnectivityManager: InternetConnectivityManager,
    val imageGenerationRepository: ImageGenerationRepository,
    val textGenerationRepository: TextGenerationRepository,
    val fileProvider: LocalFileProvider,
    @ApplicationContext
    val context: Context,
) : ViewModel() {

    init {
        viewModelScope.launch {
            imageGenerationRepository.initialize()
            textGenerationRepository.initialize()
        }
    }

    private var _uiState = MutableStateFlow(CreationState())

    val uiState: StateFlow<CreationState>
        get() = _uiState

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    private var promptGenerationJob: Job? = null
    private var imageGenerationJob: Job? = null

    fun onImageSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                imageUri = uri,
                selectedPromptOption = PromptType.PHOTO,
            )
        }
    }

    fun onBotColorChanged(botColor: BotColor) {
        _uiState.update {
            it.copy(botColor = botColor)
        }
    }

    fun onSelectedPromptOptionChanged(promptType: PromptType) {
        _uiState.update {
            it.copy(selectedPromptOption = promptType)
        }
    }

    fun onPromptGenerationClicked() {
        promptGenerationJob?.cancel()
        promptGenerationJob = viewModelScope.launch {
            Log.d("CreationViewModel", "Generating prompt...")
            _uiState.update {
                it.copy(promptGenerationInProgress = true)
            }
            try {
                val prompt = textGenerationRepository.getNextGeneratedBotPrompt()
                Log.d("CreationViewModel", "Prompt: $prompt")
                if (prompt != null) {
                    _uiState.update {
                        it.copy(
                            generatedPrompt = prompt,
                            promptGenerationInProgress = false,
                        )
                    }
                }
            } catch (exception: Exception) {
                Log.e("CreationViewModel", "Error generating prompt", exception)
                _uiState.update {
                    it.copy(promptGenerationInProgress = false)
                }
            }
        }
    }

    fun startClicked() {
        imageGenerationJob?.cancel()
        imageGenerationJob = viewModelScope.launch {
            if (internetConnectivityManager.isInternetAvailable()) {
                try {
                    _uiState.update {
                        it.copy(screenState = ScreenState.LOADING)
                    }
                    val bitmap = when (uiState.value.selectedPromptOption) {
                        PromptType.PHOTO -> {
                            val selectedImage = _uiState.value.imageUri
                            if (selectedImage == null) {
                                _uiState.update {
                                    it.copy(screenState = ScreenState.EDIT)
                                }
                                _snackbarHostState.value.showSnackbar(context.getString(R.string.error_choose_image_prompt))
                                return@launch
                            } else {
                                imageGenerationRepository.generateFromImage(
                                    fileProvider.copyToInternalStorage(selectedImage),
                                    _uiState.value.botColor.getVerboseDescription(),
                                )
                            }
                        }

                        PromptType.TEXT -> imageGenerationRepository.generateFromDescription(
                            _uiState.value.descriptionText.text.toString(),
                            _uiState.value.botColor.getVerboseDescription(),
                        )
                    }
                    _uiState.update {
                        it.copy(resultBitmap = bitmap, screenState = ScreenState.RESULT)
                    }
                } catch (e: Exception) {
                    handleImageGenerationError(e)
                }
            } else {
                displayNoInternet()
            }
        }
    }

    private suspend fun handleImageGenerationError(exception: Exception) {
        Log.d("CreationViewModel", "Exception in generating image", exception)
        _uiState.update {
            it.copy(screenState = ScreenState.EDIT)
        }
        val message = when (exception) {
            is ImageValidationException -> {
                when (exception.imageValidationError) {
                    ImageValidationError.NOT_PERSON -> context.getString(R.string.error_image_generation_full_body)
                    ImageValidationError.NOT_ENOUGH_DETAIL -> context.getString(R.string.error_image_generation_detailed_description)
                    ImageValidationError.POLICY_VIOLATION -> context.getString(R.string.error_image_generation_policy_violation)
                    ImageValidationError.OTHER -> context.getString(R.string.error_image_generation_other)
                    else -> context.getString(R.string.error_image_generation_other)
                }
            }
            is InsufficientInformationException -> context.getString(R.string.error_provide_more_descriptive_bot)
            is NoInternetException -> context.getString(R.string.error_connectivity)
            is ImageDescriptionFailedGenerationException -> context.getString(R.string.error_image_validation)
            else -> {
                Log.e("CreationViewModel", "Unknown error:", exception)
                context.getString(R.string.error_upload_generic)
            }
        }
        _snackbarHostState.value.showSnackbar(message)
    }

    private suspend fun displayNoInternet() {
        _uiState.update {
            it.copy(screenState = ScreenState.EDIT)
        }
        _snackbarHostState.value.showSnackbar(context.getString(R.string.error_connectivity))
    }

    fun cancelInProgressTask() {
        promptGenerationJob?.cancel()
        imageGenerationJob?.cancel()
        _uiState.update {
            it.copy(screenState = ScreenState.EDIT)
        }
    }

    fun onUndoPressed() {
        _uiState.update {
            it.copy(imageUri = null)
        }
    }

    fun onBackPress() {
        when (uiState.value.screenState) {
            ScreenState.LOADING -> {
                cancelInProgressTask()
            }
            ScreenState.RESULT -> {
                _uiState.update {
                    it.copy(screenState = ScreenState.EDIT, resultBitmap = null)
                }
            }
            ScreenState.EDIT -> {
                // do nothing, back press handled outside
            }
        }
    }
}

data class CreationState(
    val selectedPromptOption: PromptType = PromptType.PHOTO,
    val listBotColors: List<BotColor> = DEFAULT_BOT_COLORS,
    val botColor: BotColor = listBotColors.first(),
    val imageUri: Uri? = null,
    val descriptionText: TextFieldState = TextFieldState(),
    val generatedPrompt: String? = null,
    val promptGenerationInProgress: Boolean = false,
    val screenState: ScreenState = ScreenState.EDIT,
    val resultBitmap: Bitmap? = null,
)

enum class ScreenState {
    EDIT,
    LOADING,
    RESULT,
}



enum class PromptType(val displayName: String) {
    PHOTO("Photo"),
    TEXT("Prompt"),
}
