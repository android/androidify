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
package com.android.developers.testing.data

import android.graphics.Bitmap
import android.net.Uri
import com.android.developers.androidify.util.LocalFileProvider
import java.io.File
/**
 * A test implementation of [LocalFileProvider].
 *
 * This class provides a stub implementation for testing purposes.
 * It does not perform any actual file operations.
 */
class TestFileProvider : LocalFileProvider {
    override fun saveBitmapToFile(
        bitmap: Bitmap,
        file: File,
    ): File {
        TODO("Not yet implemented")
    }

    override fun getFileFromCache(fileName: String): File {
        TODO("Not yet implemented")
    }

    override fun createCacheFile(fileName: String): File {
        TODO("Not yet implemented")
    }

    override fun saveToSharedStorage(
        file: File,
        fileName: String,
        mimeType: String,
    ): Uri {
        TODO("Not yet implemented")
    }

    override fun sharingUriForFile(file: File): Uri {
        TODO("Not yet implemented")
    }

    override fun copyToInternalStorage(uri: Uri): File {
        return File("")
    }

    override fun saveUriToSharedStorage(
        inputUri: Uri,
        fileName: String,
        mimeType: String,
    ): Uri {
        TODO("Not yet implemented")
    }
}
