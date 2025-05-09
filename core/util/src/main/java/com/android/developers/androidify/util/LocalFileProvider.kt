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
package com.android.developers.androidify.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/*
* Copyright 2025 The Android Open Source Project
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
interface LocalFileProvider {
    fun saveBitmapToFile(bitmap: Bitmap, file: File): File
    fun getFileFromCache(fileName: String): File
    fun createCacheFile(fileName: String): File
    fun saveToSharedStorage(file: File, fileName: String, mimeType: String): Uri
    fun sharingUriForFile(file: File): Uri
    fun copyToInternalStorage(uri: Uri): File
    fun saveUriToSharedStorage(
        inputUri: Uri,
        fileName: String,
        mimeType: String,
    ): Uri
}

@Singleton
open class LocalFileProviderImpl @Inject constructor(val application: Context) : LocalFileProvider {

    override fun saveBitmapToFile(bitmap: Bitmap, file: File): File {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            return file
        } catch (e: IOException) {
            throw e
        } finally {
            outputStream?.close()
        }
    }
    override fun getFileFromCache(fileName: String): File {
        return File(application.cacheDir, fileName)
    }

    @Throws(IOException::class)
    override fun createCacheFile(fileName: String): File {
        val cacheDir = application.cacheDir
        val imageFile = File(cacheDir, fileName)
        if (!imageFile.createNewFile()) {
            throw IOException("Unable to create file: ${imageFile.absolutePath}")
        }
        return imageFile
    }

    override fun saveToSharedStorage(
        file: File,
        fileName: String,
        mimeType: String,
    ): Uri {
        val (uri, contentValues) = createSharedStorageEntry(fileName, mimeType)
        saveFileToUri(file, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.ImageColumns.IS_PENDING, 0)
        }
        application.contentResolver.update(uri, contentValues, null, null)
        return uri
    }

    override fun saveUriToSharedStorage(
        inputUri: Uri,
        fileName: String,
        mimeType: String,
    ): Uri {
        val (newUri, contentValues) = createSharedStorageEntry(fileName, mimeType)
        application.contentResolver.openOutputStream(newUri)?.use { outputStream ->
            application.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                val buffer = ByteArray(4 * 1024) // 4 KB buffer size - adjust as needed
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        } ?: throw IOException("Failed to open output stream.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.ImageColumns.IS_PENDING, 0)
        }
        application.contentResolver.update(newUri, contentValues, null, null)
        return newUri
    }

    @Throws(IOException::class)
    private fun saveFileToUri(file: File, uri: Uri) {
        application.contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(4 * 1024) // 4 KB buffer size - adjust as needed
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        } ?: throw IOException("Failed to open output stream for uri: $uri")
    }

    @Throws(IOException::class)
    private fun createSharedStorageEntry(fileName: String, mimeType: String): Pair<Uri, ContentValues> {
        val resolver = application.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            // We set the date taken to now to ensure that the images appear on the date we create them here.
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, contentValues)
            ?: throw IOException("Failed to create new MediaStore entry.")
        return Pair(uri, contentValues)
    }

    override fun sharingUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            application,
            "${application.packageName}.fileprovider",
            file,
        )
    }

    @Throws(IOException::class)
    override fun copyToInternalStorage(uri: Uri): File {
        val uuid = UUID.randomUUID()
        val file = File(application.cacheDir, "temp_file_$uuid")
        application.contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }
}
