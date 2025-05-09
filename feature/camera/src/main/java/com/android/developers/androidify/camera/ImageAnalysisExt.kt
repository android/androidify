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
package com.android.developers.androidify.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Sets the [ImageAnalysis.Analyzer] on image analysis until the calling coroutine is cancelled.
 *
 * Each time a new [ImageProxy] is available, it will be sent to [block]. The block will be called
 * within the [kotlin.coroutines.CoroutineContext] of the calling coroutine. The provided image
 * proxy will be automatically closed when [block] completes.
 */
@OptIn(DelicateCoroutinesApi::class)
suspend fun ImageAnalysis.analyze(block: suspend (ImageProxy) -> Unit) {
    coroutineScope {
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                setAnalyzer(Runnable::run) { imageProxy ->
                    // Launch ATOMIC to ensure we close the ImageProxy
                    launch(start = CoroutineStart.ATOMIC) {
                        imageProxy.use { if (cont.isActive) block(it) }
                    }
                }
            }
        } finally {
            clearAnalyzer()
        }
    }
}
