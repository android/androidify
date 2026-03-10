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
package com.android.developers.androidify.data

import com.android.developers.androidify.model.ImageValidationError as ModelImageValidationError

class InsufficientInformationException(errorMessage: String? = null) : Exception(errorMessage)

class ImageValidationException(val imageValidationError: ImageValidationError? = null) : Exception(imageValidationError.toString())

class ImageDescriptionFailedGenerationException() : Exception()
class NoInternetException : Exception("No internet connection")

enum class ImageValidationError {
    NOT_PERSON,
    NOT_ENOUGH_DETAIL,
    POLICY_VIOLATION,
    OTHER,
}

fun ModelImageValidationError.toImageValidationError(): com.android.developers.androidify.data.ImageValidationError {
    return when (this) {
        ModelImageValidationError.NOT_PERSON -> com.android.developers.androidify.data.ImageValidationError.NOT_PERSON
        ModelImageValidationError.NOT_ENOUGH_DETAIL -> com.android.developers.androidify.data.ImageValidationError.NOT_ENOUGH_DETAIL
        ModelImageValidationError.POLICY_VIOLATION -> com.android.developers.androidify.data.ImageValidationError.POLICY_VIOLATION
        ModelImageValidationError.OTHER -> com.android.developers.androidify.data.ImageValidationError.OTHER
    }
}
