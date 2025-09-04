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
package com.android.developers.androidify.home

import androidx.lifecycle.ViewModel
import com.android.developers.androidify.data.ConfigProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(configProvider: ConfigProvider) : ViewModel() {
    private val _state = MutableStateFlow(
        HomeState(
            isAppActive = !configProvider.isAppInactive(),
            dancingDroidLink = configProvider.getDancingDroidLink(),
            videoLink = configProvider.getPromoVideoLink(),
            isXrEnabled = configProvider.isXrEnabled(),
        ),
    )
    val state = _state.asStateFlow()
}

data class HomeState(
    val isAppActive: Boolean = true,
    val isXrEnabled: Boolean = false,
    val videoLink: String? = null,
    val dancingDroidLink: String? = null,
)
