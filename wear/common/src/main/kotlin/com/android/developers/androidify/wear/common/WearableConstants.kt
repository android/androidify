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

object WearableConstants {
    const val ANDROIDIFY_INITIATE_TRANSFER_PATH = "/initiate_transfer"
    const val ANDROIDIFY_FINALIZE_TRANSFER_TEMPLATE = "/finalize_transfer/%s"

    const val ANDROIDIFY_INSTALLED_WEAR = "androidify"
    const val ANDROIDIFY_INSTALLED_PHONE = "androidify_phone"
    const val ANDROIDIFY_TRANSFER_PATH_TEMPLATE = "/transfer_apk/%s"

    const val ANDROIDIFY_PLAY_URL = "market://details?id="
    const val ANDROIDIFY_LAUNCH_URL = "androidify://launch"

    const val SETUP_TIMEOUT_MS = 60_000L
    const val TRANSFER_TIMEOUT_MS = 60_000L
}
