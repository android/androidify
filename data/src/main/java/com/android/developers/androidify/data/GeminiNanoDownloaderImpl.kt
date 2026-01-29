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

import com.android.developers.androidify.RemoteConfigDataSource
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface GeminiNanoDownloader {
    fun isModelDownloaded(): Boolean
}

@Singleton
class GeminiNanoDownloaderImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
) : GeminiNanoDownloader {
    var generativeModel = Generation.getClient()

    private var modelDownloaded = false

    override fun isModelDownloaded() = modelDownloaded

    suspend fun downloadModel() {
        Timber.d("downloadModel")
        try {
            setup()
            generativeModel.warmup()
        } catch (e: Exception) {
            Timber.e(e, "Error preparing inference engine")
        }
        Timber.d("prepare inference engine")
    }

    private suspend fun setup() {
        val nanoStatus = generativeModel.checkStatus()

        if (nanoStatus == FeatureStatus.UNAVAILABLE) {
            Timber.d("Nano not available on device")
            return
        }

        if (nanoStatus == FeatureStatus.DOWNLOADABLE &&
            remoteConfigDataSource.enabledGeminiNanoModelVersions()
                .contains(generativeModel.getBaseModelName())
        ) {
            generativeModel.download().collect { status ->
                when (status) {
                    is DownloadStatus.DownloadStarted ->
                        Timber.d("starting download for Gemini Nano")

                    is DownloadStatus.DownloadProgress ->
                        Timber.d("Nano ${status.totalBytesDownloaded} bytes downloaded")

                    DownloadStatus.DownloadCompleted -> {
                        Timber.d("Gemini Nano download complete")
                        modelDownloaded = true
                    }

                    is DownloadStatus.DownloadFailed -> {
                        Timber.e("Nano download failed ${status.e.message}")
                    }
                }
            }
        }

        if (nanoStatus == FeatureStatus.AVAILABLE) {
            modelDownloaded = true
        }
    }
}
