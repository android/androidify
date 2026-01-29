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
package com.android.developers.testing.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.android.developers.androidify.data.ImageGenerationRepository
import com.android.developers.androidify.model.ValidatedDescription
import java.io.File

class FakeImageGenerationRepository : ImageGenerationRepository {
    var exceptionToThrow: Exception? = null

    override suspend fun generateFromDescription(
        description: String,
        skinTone: String,
    ): Bitmap {
        if (exceptionToThrow != null) throw exceptionToThrow!!
        return createBitmap(1, 1)
    }

    override suspend fun getDescriptionFromImage(file: File): ValidatedDescription {
        return ValidatedDescription(true, "")
    }

    override suspend fun generateFromImage(
        file: File,
        skinTone: String,
    ): Bitmap {
        if (exceptionToThrow != null) throw exceptionToThrow!!
        return createBitmap(1, 1)
    }

    override suspend fun saveImage(imageBitmap: Bitmap): Uri {
        if (exceptionToThrow != null) throw exceptionToThrow!!
        return "content://com.example.app/images/saveImageInternal.jpg".toUri()
    }

    override suspend fun saveImageToExternalStorage(imageBitmap: Bitmap): Uri {
        if (exceptionToThrow != null) throw exceptionToThrow!!
        return "content://com.example.app/images/original.jpg".toUri()
    }

    override suspend fun saveImageToExternalStorage(imageUri: Uri): Uri {
        if (exceptionToThrow != null) throw exceptionToThrow!!
        return imageUri
    }

    override suspend fun addBackgroundToBot(
        image: Bitmap,
        backgroundPrompt: String,
    ): Bitmap {
        return createBitmap(1, 1)
    }

    override suspend fun removeBackground(image: Bitmap): Bitmap {
        return createBitmap(1, 1)
    }
}
