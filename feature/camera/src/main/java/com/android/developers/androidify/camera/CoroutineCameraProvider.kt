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

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Runs a camera for the duration of a coroutine.
 *
 * The camera selected by [cameraSelector] will run with the provided [useCases] for the
 * duration that [block] is active. This means that [block] should suspend until the camera
 * should be closed.
 */
suspend fun <R> ProcessCameraProvider.runWith(
    cameraSelector: CameraSelector,
    useCases: UseCaseGroup,
    block: suspend CoroutineScope.(Camera) -> R,
): R = coroutineScope {
    val scopedLifecycle = CoroutineLifecycleOwner(coroutineContext)
    block(this@runWith.bindToLifecycle(scopedLifecycle, cameraSelector, useCases))
}

/**
 * A [LifecycleOwner] that follows the lifecycle of a coroutine.
 *
 * If the coroutine is active, the owned lifecycle will jump to a
 * [Lifecycle.State.RESUMED] state. When the coroutine completes, the owned lifecycle will
 * transition to a [Lifecycle.State.DESTROYED] state.
 */
internal class CoroutineLifecycleOwner(coroutineContext: CoroutineContext) :
    LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry =
        LifecycleRegistry(this).apply {
            currentState = Lifecycle.State.INITIALIZED
        }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    init {
        if (coroutineContext[Job]?.isActive == true) {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            coroutineContext[Job]?.invokeOnCompletion {
                lifecycleRegistry.apply {
                    currentState = Lifecycle.State.STARTED
                    currentState = Lifecycle.State.CREATED
                    currentState = Lifecycle.State.DESTROYED
                }
            }
        } else {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }
}
