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
import android.graphics.BitmapFactory
import android.net.Uri
import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.model.ValidatedDescription
import com.android.developers.androidify.model.ValidatedImage
import com.android.developers.androidify.ondevice.LocalSegmentationDataSource
import com.android.developers.androidify.util.LocalFileProvider
import com.android.developers.androidify.vertexai.FirebaseAiDataSource
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ImageGenerationRepository {
    suspend fun generateFromDescription(description: String, skinTone: String): Bitmap
    suspend fun getDescriptionFromImage(file: File): ValidatedDescription
    suspend fun generateFromImage(file: File, skinTone: String): Bitmap
    suspend fun saveImage(imageBitmap: Bitmap): Uri
    suspend fun saveImageToExternalStorage(imageBitmap: Bitmap): Uri
    suspend fun saveImageToExternalStorage(imageUri: Uri): Uri

    suspend fun addBackgroundToBot(image: Bitmap, backgroundPrompt: String): Bitmap
    suspend fun removeBackground(image: Bitmap): Bitmap
}

@Singleton
internal class ImageGenerationRepositoryImpl @Inject constructor(
    private val localFileProvider: LocalFileProvider,
    private val internetConnectivityManager: InternetConnectivityManager,
    private val geminiNanoDataSource: GeminiNanoGenerationDataSource,
    private val firebaseAiDataSource: FirebaseAiDataSource,
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val localSegmentationDataSource: LocalSegmentationDataSource,
) : ImageGenerationRepository {

    private suspend fun validatePromptHasEnoughInformation(inputPrompt: String): ValidatedDescription =
        firebaseAiDataSource.validatePromptHasEnoughInformation(inputPrompt)

    private suspend fun validateImageIsFullPerson(file: File): ValidatedImage {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val validateImageResult = if (remoteConfigDataSource.useGeminiNano()) {
            geminiNanoDataSource.validateImageHasEnoughInformation(bitmap)
        } else {
            null
        }

        // If validating image with Nano is not successful, fallback to using Firebase AI
        return validateImageResult
            ?: firebaseAiDataSource.validateImageHasEnoughInformation(bitmap)
    }

    @Throws(InsufficientInformationException::class)
    override suspend fun generateFromDescription(
        description: String,
        skinTone: String,
    ): Bitmap {
        checkInternetConnection()
        if (description.isBlank()) throw InsufficientInformationException()
        val validatedPrompt = validatePromptHasEnoughInformation(description)
        if (!validatedPrompt.success || validatedPrompt.userDescription == null) {
            throw InsufficientInformationException()
        }
        return firebaseAiDataSource.generateImageFromPromptAndSkinTone(
            validatedPrompt.userDescription.toString(),
            skinTone,
        )
    }

    override suspend fun getDescriptionFromImage(file: File): ValidatedDescription {
        checkInternetConnection()
        val validatedImage = validateImageIsFullPerson(file)
        if (!validatedImage.success) {
            throw ImageValidationException(validatedImage.errorMessage?.toImageValidationError())
        }

        var imageDescription = if (remoteConfigDataSource.useGeminiNano()) {
            geminiNanoDataSource.generateDescriptivePromptFromImage(
                BitmapFactory.decodeFile(file.absolutePath),
            )
        } else {
            null
        }

        Timber.d("nano generated image desc ${imageDescription?.userDescription}")

        // If we're not getting a valid result from Nano, try with Firebase AI Logic
        if (imageDescription?.success != true) {
            Timber.d("generating image description with Firebase AI Logic")
            imageDescription = firebaseAiDataSource.generateDescriptivePromptFromImage(
                BitmapFactory.decodeFile(file.absolutePath),
            )
        }

        return imageDescription
    }

    override suspend fun generateFromImage(
        file: File,
        skinTone: String,
    ): Bitmap {
        val imageDescription = getDescriptionFromImage(file)

        if (!imageDescription.success || imageDescription.userDescription == null) {
            throw ImageDescriptionFailedGenerationException()
        }
        return firebaseAiDataSource.generateImageFromPromptAndSkinTone(
            imageDescription.userDescription.toString(),
            skinTone,
        )
    }

    override suspend fun saveImage(imageBitmap: Bitmap): Uri {
        val cacheFile = localFileProvider.createCacheFile("shared_image_${UUID.randomUUID()}.png")
        localFileProvider.saveBitmapToFile(imageBitmap, cacheFile)
        return localFileProvider.sharingUriForFile(cacheFile)
    }

    override suspend fun saveImageToExternalStorage(imageBitmap: Bitmap): Uri {
        val cacheFile =
            localFileProvider.createCacheFile("androidify_image_result_${UUID.randomUUID()}.png")
        localFileProvider.saveBitmapToFile(imageBitmap, cacheFile)
        return localFileProvider.saveToSharedStorage(cacheFile, cacheFile.name, "image/png")
    }

    override suspend fun saveImageToExternalStorage(imageUri: Uri): Uri {
        return localFileProvider.saveUriToSharedStorage(
            imageUri,
            "androidify_image_original_${UUID.randomUUID()}.png",
            "image/png",
        )
    }

    private fun checkInternetConnection() {
        if (!internetConnectivityManager.isInternetAvailable()) {
            throw NoInternetException()
        }
    }

    override suspend fun addBackgroundToBot(image: Bitmap, backgroundPrompt: String): Bitmap {
        val backgroundBotInstructions = remoteConfigDataSource.getBotBackgroundInstructionPrompt() +
            "\"" + backgroundPrompt + "\""
        return firebaseAiDataSource.generateImageWithEdit(image, backgroundBotInstructions)
    }

    override suspend fun removeBackground(image: Bitmap): Bitmap {
        return localSegmentationDataSource.removeBackground(image)
    }
}
