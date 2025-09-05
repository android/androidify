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
package com.android.developers.androidify.wear.common

enum class WatchFaceActivationStrategy {
    NO_ACTION_NEEDED,
    CALL_SET_ACTIVE_NO_USER_ACTION,
    FOLLOW_PROMPT_ON_WATCH,
    LONG_PRESS_TO_SET,
    GO_TO_WATCH_SETTINGS,
    ;

    companion object {
        fun fromWatchFaceState(
            hasActiveWatchFace: Boolean = false,
            hasGrantedSetActivePermission: Boolean = false,
            canRequestSetActivePermission: Boolean = true,
            hasUsedSetActiveApi: Boolean = false,
        ): WatchFaceActivationStrategy {
            return when {
                hasActiveWatchFace -> NO_ACTION_NEEDED
                hasGrantedSetActivePermission && !hasUsedSetActiveApi -> CALL_SET_ACTIVE_NO_USER_ACTION
                canRequestSetActivePermission && !hasUsedSetActiveApi -> FOLLOW_PROMPT_ON_WATCH
                !canRequestSetActivePermission && !hasGrantedSetActivePermission && !hasUsedSetActiveApi -> GO_TO_WATCH_SETTINGS
                else -> return LONG_PRESS_TO_SET
            }
        }
    }
}
