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
@file:OptIn(ExperimentalPermissionsApi::class)

package com.android.developers.androidify

/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.developers.androidify.watchfacepush.WatchFaceOnboardingRepository
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchFaceOnboardingViewModel(
    val watchFaceOnboardingRepository: WatchFaceOnboardingRepository,
) : ViewModel() {
    val state = watchFaceOnboardingRepository.watchFaceTransferState
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, WatchFaceInstallationStatus.Unknown)

    /**
     * As a result of permissions changes, for example, the user completing the permissions flow, or
     * the user going into settings and changing the permission, the activation strategy may now
     * have changed. For example, it may now be possible for the app to directly set the watch face
     * as active.
     *
     * This method re-evaluates the strategy and acts on it if necessary, communicating the new
     * strategy back to the phone.
     */
    fun maybeSendUpdateOnPermissionsChange(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ) {
        viewModelScope.launch {
            val currentState = state.value
            if (granted) {
                watchFaceOnboardingRepository.updatePermissionStatus(true)
            } else if (!shouldShowRationale) {
                watchFaceOnboardingRepository.updatePermissionStatus(false)
            }

            var newStrategy = watchFaceOnboardingRepository.getWatchFaceActivationStrategy()
            // In the special case where the watch face can now be directly activated by the app.
            if (newStrategy == WatchFaceActivationStrategy.CALL_SET_ACTIVE_NO_USER_ACTION) {
                watchFaceOnboardingRepository.setActiveWatchFace()
                newStrategy = WatchFaceActivationStrategy.NO_ACTION_NEEDED
            }

            if (currentState is WatchFaceInstallationStatus.Complete &&
                currentState.success
            ) {
                val newStatus = WatchFaceInstallationStatus.Complete(
                    success = true,
                    activationStrategy = newStrategy,
                    installError = currentState.installError,
                    otherNodeId = currentState.otherNodeId,
                    transferId = currentState.transferId,
                    validationToken = currentState.validationToken,
                )
                watchFaceOnboardingRepository.setWatchFaceTransferState(newStatus)
                watchFaceOnboardingRepository.sendInstallUpdate(newStatus)
            }
        }
    }

    fun resetWatchFaceTransferState() {
        viewModelScope.launch {
            watchFaceOnboardingRepository.resetWatchFaceTransferState()
        }
    }

    companion object {
        private const val TAG = "WatchFaceOnboardingViewModel"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as MainApplication)
                WatchFaceOnboardingViewModel(
                    watchFaceOnboardingRepository = app.watchFaceOnboardingRepository,
                )
            }
        }
    }
}
