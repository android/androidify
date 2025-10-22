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
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.NonUiContext
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface LocalFileProvider {
    suspend fun saveBitmapToFile(bitmap: Bitmap, file: File)
    suspend fun getFileFromCache(fileName: String): File
    suspend fun createCacheFile(fileName: String): File
    suspend fun saveToSharedStorage(file: File, fileName: String, mimeType: String): Uri
    fun sharingUriForFile(file: File): Uri
    suspend fun copyToInternalStorage(uri: Uri): File
    suspend fun saveUriToSharedStorage(inputUri: Uri, fileName: String, mimeType: String): Uri
    suspend fun loadBitmapFromUri(uri: Uri): Bitmap?
}

@Singleton
class LocalFileProviderImpl @Inject constructor(
    @param:NonUiContext private val context: Context,
    @param:Named("IO") private val ioDispatcher: CoroutineDispatcher,
) : LocalFileProvider {

    override suspend fun saveBitmapToFile(bitmap: Bitmap, file: File) = withContext(ioDispatcher) {
        file.outputStream().buffered().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        }
    }

    override suspend fun getFileFromCache(fileName: String): File = withContext(ioDispatcher) {
        File(context.cacheDir, fileName)
    }

    @Throws(IOException::class)
    override suspend fun createCacheFile(fileName: String): File = withContext(ioDispatcher) {
        val cacheDir = context.cacheDir
        val imageFile = File(cacheDir, fileName)
        if (!imageFile.createNewFile()) {
            throw IOException("Unable to create file: ${imageFile.absolutePath}")
        }
        return@withContext imageFile
    }

    override suspend fun saveToSharedStorage(
        file: File,
        fileName: String,
        mimeType: String,
    ): Uri = withContext(ioDispatcher) {
        val (uri, contentValues) = createSharedStorageEntry(fileName, mimeType)
        saveFileToUri(file, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.ImageColumns.IS_PENDING, 0)
        }
        context.contentResolver.update(uri, contentValues, null, null)
        return@withContext uri
    }

    override suspend fun saveUriToSharedStorage(
        inputUri: Uri,
        fileName: String,
        mimeType: String,
    ): Uri = withContext(ioDispatcher) {
        val (newUri, contentValues) = createSharedStorageEntry(fileName, mimeType)
        context.copyContent(newUri, inputUri) ?: throw IOException("Failed to open output stream.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.ImageColumns.IS_PENDING, 0)
        }
        context.contentResolver.update(newUri, contentValues, null, null)
        return@withContext newUri
    }

    override suspend fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return withContext(ioDispatcher) {
            try {
                context.contentResolver.openInputStream(uri)?.use {
                    return@withContext BitmapFactory.decodeStream(it)
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @Throws(IOException::class)
    @WorkerThread
    private fun saveFileToUri(file: File, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.buffered()?.use { outputStream ->
            file.inputStream().buffered().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Failed to open output stream for uri: $uri")
    }

    @Throws(IOException::class)
    @WorkerThread
    private fun createSharedStorageEntry(
        fileName: String,
        mimeType: String,
    ): Pair<Uri, ContentValues> {
        val resolver = context.contentResolver
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
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    @Throws(IOException::class)
    override suspend fun copyToInternalStorage(uri: Uri): File = withContext(ioDispatcher) {
        getFileFromCache("temp_file_${UUID.randomUUID()}").also { file ->
            context.contentResolver.openInputStream(uri)?.buffered()?.use { inputStream ->
                file.outputStream().buffered().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}

private fun Context.copyContent(
    to: Uri,
    from: Uri,
): Long? {
    return contentResolver.openOutputStream(to)?.use { outputStream ->
        contentResolver.openInputStream(from)?.use { inputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}
