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

import android.app.Application
import android.util.Log
import com.google.ai.edge.aicore.DownloadCallback
import com.google.ai.edge.aicore.DownloadConfig
import com.google.ai.edge.aicore.GenerativeAIException
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiNanoDownloader @Inject constructor(private val application: Application) {
    var generativeModel: GenerativeModel? = null
        private set

    private var modelDownloaded = false

    fun isModelDownloaded() = modelDownloaded

    suspend fun downloadModel() {
        Log.d("GeminiNanoDownloader", "downloadModel")
        try {
            setup()
            generativeModel?.prepareInferenceEngine()
        } catch (e: Exception) {
            Log.e("GeminiNanoDownloader", "Error preparing inference engine", e)
        }
        Log.d("GeminiNanoDownloader", "prepare inference engine")
    }

    private fun setup() {
        val downloadCallback = object : DownloadCallback {
            override fun onDownloadStarted(bytesToDownload: Long) {
                super.onDownloadStarted(bytesToDownload)
                Log.i("GeminiNanoDownloader", "onDownloadStarted for Gemini Nano $bytesToDownload")
            }

            override fun onDownloadCompleted() {
                super.onDownloadCompleted()
                modelDownloaded = true
                Log.i("GeminiNanoDownloader", "onDownloadCompleted for Gemini Nano")
            }

            override fun onDownloadFailed(failureStatus: String, e: GenerativeAIException) {
                super.onDownloadFailed(failureStatus, e)
                // downloading the model has failed so make the model null as we can't use it
                generativeModel = null
                Log.i("GeminiNanoDownloader", "onDownloadFailed for Gemini Nano")
            }
        }

        val downloadConfig = DownloadConfig(downloadCallback)

        val generationConfig = generationConfig {
            context = application
            temperature = 0.2f
            topK = 16
            maxOutputTokens = 256
        }

        generativeModel = GenerativeModel(
            generationConfig = generationConfig,
            downloadConfig = downloadConfig,
        )
    }
}
