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
package com.android.developers.androidify.updater

import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.wear.watchfacepush.WatchFacePushManagerFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val defaultWatchFaceName = "default_watchface.apk"
private const val manifestTokenKey = "com.google.android.wearable.marketplace.DEFAULT_WATCHFACE_VALIDATION_TOKEN"

private const val TAG = "UpdateWorker"

/**
 * WorkManager worker that tries to update the default watch face, if installed.
 *
 * Checks which watch faces the package already has installed, and if there is a default watch face
 * in the assets bundle. Compares the versions of these to determine whether an update is necessary
 * and if so, updates the default watch face, taking also the new watch face validation token from
 * the manifest file.
 */
class UpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val watchFacePushManager = WatchFacePushManagerFactory.createWatchFacePushManager(applicationContext)

        val watchFaces = watchFacePushManager.listWatchFaces().installedWatchFaceDetails
            .associateBy { it.packageName }

        val copiedFile = File.createTempFile("tmp", ".apk", applicationContext.cacheDir)
        try {
            applicationContext.assets.open(defaultWatchFaceName).use { inputStream ->
                FileOutputStream(copiedFile).use { outputStream -> inputStream.copyTo(outputStream) }
            }
            val packageInfo =
                applicationContext.packageManager.getPackageArchiveInfo(copiedFile.absolutePath, 0)

            packageInfo?.let { newPkg ->
                // Check if the default watch face is currently installed and should therefore be
                // updated if the one in the assets folder has a higher version code.
                watchFaces[newPkg.packageName]?.let { curPkg ->
                    if (newPkg.longVersionCode > curPkg.versionCode) {
                        ParcelFileDescriptor.open(
                            copiedFile,
                            ParcelFileDescriptor.MODE_READ_ONLY,
                        ).use { pfd ->
                            val token = getDefaultWatchFaceToken()
                            if (token != null) {
                                watchFacePushManager.updateWatchFace(curPkg.slotId, pfd, token)
                                Log.d(TAG, "Watch face updated from ${curPkg.versionCode} to ${newPkg.longVersionCode}")
                            } else {
                                Log.w(TAG, "Watch face not updated, no token found")
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Watch face not updated", e)
            return Result.failure()
        } finally {
            copiedFile.delete()
        }
        return Result.success()
    }

    private fun getDefaultWatchFaceToken(): String? {
        val appInfo = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA,
        )
        return appInfo.metaData?.getString(manifestTokenKey)
    }
}
