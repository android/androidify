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
package com.android.developers.androidify.model

import androidx.annotation.Keep

@Keep
data class ValidatedDescription(
    val success: Boolean,
    val userDescription: String?,
)

@Keep
data class ValidatedImage(
    val success: Boolean,
    val errorMessage: ImageValidationError?,
)

@Keep
data class GeneratedPrompt(
    val success: Boolean,
    val generatedPrompts: List<String>?,
)

@Keep
enum class ImageValidationError(val description: String) {
    NOT_PERSON("not_a_person"),
    NOT_ENOUGH_DETAIL("not_enough_detail"),
    POLICY_VIOLATION("policy_violation"),
    OTHER("other"),
}
