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
package com.android.developers.androidify.results

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import com.android.developers.androidify.data.ConfigProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = ResultsViewModel.Factory::class)
class ResultsViewModel @AssistedInject constructor(
    @Assisted("resultImageUrl") val resultImageUrl: Uri?,
    @Assisted("originalImageUrl") val originalImageUrl: Uri?,
    @Assisted("promptText") val promptText: String?,
    configProvider: ConfigProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("resultImageUrl") resultImageUrl: Uri?,
            @Assisted("originalImageUrl") originalImageUrl: Uri?,
            @Assisted("promptText") promptText: String?,
        ): ResultsViewModel
    }
    private val _state = MutableStateFlow(ResultState())
    val state = _state.asStateFlow()

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    init {
        _state.update {
            ResultState(resultImageUrl, originalImageUrl, promptText = promptText, xrEnabled = configProvider.isXrEnabled())
        }
    }
}

data class ResultState(
    val resultImageUri: Uri? = null,
    val originalImageUrl: Uri? = null,
    val promptText: String? = null,
    val xrEnabled: Boolean = false,
)
