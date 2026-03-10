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

import android.graphics.Bitmap
import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.model.ImageValidationError
import com.android.developers.androidify.model.ValidatedDescription
import com.android.developers.androidify.model.ValidatedImage
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface GeminiNanoGenerationDataSource {
    suspend fun generatePrompt(prompt: String): String?
    suspend fun validateImageHasEnoughInformation(image: Bitmap): ValidatedImage?
    suspend fun generateDescriptivePromptFromImage(image: Bitmap): ValidatedDescription?
}

@Singleton
internal class GeminiNanoGenerationDataSourceImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val downloader: GeminiNanoDownloaderImpl,
) :
    GeminiNanoGenerationDataSource {

    /**
     * Generate a prompt to create an Android bot using Gemini Nano.
     * If Gemini Nano is not available, return null.
     */
    override suspend fun generatePrompt(prompt: String): String? {
        if (!downloader.isModelDownloaded()) return null
        val response = downloader.generativeModel.generateContent(
            generateContentRequest(TextPart(prompt)) {
                temperature = 0.2f
                topK = 16
                candidateCount = 1
                maxOutputTokens = 256
            },
        )
        Timber.d("generatePrompt: ${response.candidates[0].text}")
        return response.candidates[0].text
    }

    override suspend fun validateImageHasEnoughInformation(image: Bitmap): ValidatedImage? {
        if (!downloader.isModelDownloaded()) return null

        val response = downloader.generativeModel.generateContent(
            generateContentRequest(
                ImagePart(image),
                TextPart(remoteConfigDataSource.promptImageValidationNano()),
            ) {
                temperature = 0.0f
                maxOutputTokens = 20
            },
        ).candidates[0].text

        // If the model returns null as the validation error, there was no error found when
        // validating the image.
        val successValue = response == "null"
        return ValidatedImage(
            successValue,
            ImageValidationError.entries.find { it.description == response },
        )
    }

    override suspend fun generateDescriptivePromptFromImage(image: Bitmap): ValidatedDescription? {
        if (!downloader.isModelDownloaded()) return null

        val generatedImageDescription = downloader.generativeModel.generateContent(
            generateContentRequest(
                ImagePart(image),
                TextPart(remoteConfigDataSource.promptImageDescriptionNano()),
            ) {
                temperature = 0.2f
            },
        )

        return ValidatedDescription(
            true,
            generatedImageDescription.candidates[0].text,
        )
    }
}
