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
package com.android.developers.testing.network

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.android.developers.androidify.model.GeneratedPrompt
import com.android.developers.androidify.model.ValidatedDescription
import com.android.developers.androidify.model.ValidatedImage
import com.android.developers.androidify.vertexai.FirebaseAiDataSource

class TestFirebaseAiDataSource(val promptOutput: List<String>) : FirebaseAiDataSource {
    override suspend fun validatePromptHasEnoughInformation(inputPrompt: String): ValidatedDescription {
        return ValidatedDescription(true, "Firebase description")
    }

    override suspend fun validateImageHasEnoughInformation(image: Bitmap): ValidatedImage {
        return ValidatedImage(true, null)
    }

    override suspend fun generateDescriptivePromptFromImage(image: Bitmap): ValidatedDescription {
        return ValidatedDescription(true, "Firebase description")
    }

    override suspend fun generateImageFromPromptAndSkinTone(
        prompt: String,
        skinTone: String,
    ): Bitmap {
        return createBitmap(1, 1)
    }

    override suspend fun generatePrompt(prompt: String): GeneratedPrompt {
        return GeneratedPrompt(true, promptOutput)
    }

    override suspend fun generateImageWithEdit(
        image: Bitmap,
        backgroundPrompt: String,
    ): Bitmap {
        return createBitmap(1, 1)
    }
}
