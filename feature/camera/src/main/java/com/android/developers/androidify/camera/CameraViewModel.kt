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

import android.app.Application
import android.media.Image
import android.media.MediaActionSound
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraControl.OperationCanceledException
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageInfo
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.concurrent.futures.await
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.application
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.android.developers.androidify.util.LocalFileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class CameraViewModel
@Inject constructor(
    application: Application,
    val localFileProvider: LocalFileProvider,
    val rearCameraUseCase: RearCameraUseCase,
) : AndroidViewModel(application) {
    private var _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState>
        get() = _uiState

    private var _foldingFeature = MutableStateFlow<FoldingFeature?>(null)
    val foldingFeature: StateFlow<FoldingFeature?>
        get() = _foldingFeature
    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _uiState.update { it.copy(surfaceRequest = newSurfaceRequest) }
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat(),
            )
        }
    }

    private val cameraCaptureUseCase = ImageCapture.Builder().build()

    private val cameraImageAnalysisUseCase = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val cameraUseCaseGroup = UseCaseGroup.Builder()
        .addUseCase(cameraPreviewUseCase)
        .addUseCase(cameraCaptureUseCase)
        .addUseCase(cameraImageAnalysisUseCase)
        .build()
    private val cameraTypeFlow = MutableStateFlow<CameraSelector?>(null)

    private var mediaActionSound: MediaActionSound? = null

    private var autofocusRequestId: Int = 0

    suspend fun bindToCamera() = suspendCancellableCoroutine { cont ->
        val job = viewModelScope.launch {
            // Launch a coroutine to run pose detection in parallel with camera code. This will
            // automatically be cancelled once the calling coroutine is cancelled.
            launch { runPoseDetection() }

            val processCameraProvider = ProcessCameraProvider.awaitInstance(application)
            val availableCameraLenses =
                listOf(
                    DEFAULT_BACK_CAMERA,
                    DEFAULT_FRONT_CAMERA,
                ).filter {
                    processCameraProvider.hasCamera(it)
                }
            _uiState.update { it.copy(canFlipCamera = availableCameraLenses.size == 2) }
            cameraTypeFlow.update { it ?: availableCameraLenses.firstOrNull() }
            mediaActionSound = MediaActionSound()
            cameraTypeFlow
                .onCompletion {
                    mediaActionSound?.release()
                    mediaActionSound = null
                    cameraControl = null
                    cameraInfo = null
                }
                .collectLatest { cameraType ->
                    // If the camera type has changed, reset the autofocus requests
                    autofocusRequestId++ // Ensure in-progress requests don't overwrite this
                    _uiState.update { it.copy(autofocusUiState = AutofocusUiState.Unspecified) }

                    if (cameraType != null) {
                        processCameraProvider.runWith(cameraType, cameraUseCaseGroup) { camera ->
                            cameraControl = camera.cameraControl
                            cameraInfo = camera.cameraInfo

                            // Suspend on zoom updates
                            camera.cameraInfo.zoomState.asFlow().collectLatest { zoomState ->
                                _uiState.update {
                                    it.copy(
                                        zoomLevel = zoomState.zoomRatio,
                                        zoomMinRatio = zoomState.minZoomRatio,
                                        zoomMaxRatio = zoomState.maxZoomRatio,
                                    )
                                }
                            }
                        }
                    }
                }
        }

        job.invokeOnCompletion { cause ->
            if (cause == null) {
                cont.resume(Unit)
            } else {
                cont.resumeWithException(cause)
            }
        }

        cont.invokeOnCancellation { job.cancel() }
    }

    fun captureImage() {
        viewModelScope.launch {
            mediaActionSound?.play(MediaActionSound.SHUTTER_CLICK)
            val file = localFileProvider.getFileFromCache("image${System.currentTimeMillis()}.jpg")
            val outputFileOptions = OutputFileOptions.Builder(file).apply {
                if (cameraTypeFlow.value == DEFAULT_FRONT_CAMERA) {
                    val metadata = ImageCapture.Metadata().apply {
                        // Ensure the captured front camera image is mirrored to match preview
                        isReversedHorizontal = true
                    }
                    setMetadata(metadata)
                }
            }.build()

            try {
                // Use the suspend version of takePicture() to get the result
                val outputFileResults = cameraCaptureUseCase.takePicture(outputFileOptions)
                Log.d("CameraViewModel", "Image captured: ${outputFileResults.savedUri}")
                _uiState.update { it.copy(imageUri = outputFileResults.savedUri) }
            } catch (exception: ImageCaptureException) {
                Log.e("CameraViewModel", "Error capturing image $exception")
                // TODO handle error on screen
            }
        }
    }

    fun tapToFocus(tapCoordinates: Offset) {
        viewModelScope.launch {
            val requestId = ++autofocusRequestId
            val point = surfaceMeteringPointFactory
                ?.createPoint(tapCoordinates.x, tapCoordinates.y)
            if (point != null) {
                _uiState.update {
                    it.copy(
                        autofocusUiState = AutofocusUiState.Specified(
                            surfaceCoordinates = tapCoordinates,
                            status = AutofocusUiState.Status.RUNNING,
                        ),
                    )
                }
                val meteringAction = FocusMeteringAction.Builder(point).build()
                val completionStatus: AutofocusUiState.Status =
                    try {
                        if (cameraControl?.startFocusAndMetering(meteringAction)
                                ?.await()?.isFocusSuccessful == true
                        ) {
                            mediaActionSound?.play(MediaActionSound.FOCUS_COMPLETE)
                            AutofocusUiState.Status.SUCCESS
                        } else {
                            AutofocusUiState.Status.FAILURE
                        }
                    } catch (e: OperationCanceledException) {
                        // New calls to startFocusAndMetering and switching the camera will cancel
                        // the previous focus and metering request.
                        AutofocusUiState.Status.CANCELLED
                    }

                if (requestId == autofocusRequestId) {
                    _uiState.update {
                        it.copy(
                            autofocusUiState = AutofocusUiState.Specified(
                                surfaceCoordinates = tapCoordinates,
                                status = completionStatus,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun setZoomLevel(zoomLevel: Float) {
        viewModelScope.launch {
            cameraControl?.apply {
                // If the zoom level is changing, cancel any in-progress autofocus events
                cancelFocusAndMetering()
                setZoomRatio(zoomLevel)
            }
        }
    }

    fun setCapturedImage(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun flipCameraDirection() {
        cameraTypeFlow.update {
            if (it == DEFAULT_BACK_CAMERA) DEFAULT_FRONT_CAMERA else DEFAULT_BACK_CAMERA
        }
    }

    fun initRearDisplayFeature(activity: ComponentActivity) {
        rearCameraUseCase.init(activity)
    }

    fun shouldShowRearDisplayFeature() = rearCameraUseCase.shouldDisplayRearCameraButton()

    fun toggleRearDisplayFeature(activity: ComponentActivity) {
        rearCameraUseCase.toggleRearCameraDisplay(activity)
        _uiState.update { it.copy(isRearCameraActive = rearCameraUseCase.isRearCameraActive()) }
    }

    fun calculateFoldingFeature(activity: ComponentActivity) {
        val job = viewModelScope.launch {
            WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
                .collect { layoutInfo ->
                    _foldingFeature.update {
                        layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
                            .firstOrNull()
                    }
                }
        }

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                job.cancel()
            }
        })
    }

    private suspend fun PoseDetector.detectPersonInFrame(
        image: Image,
        imageInfo: ImageInfo,
    ): Boolean {
        val results = process(InputImage.fromMediaImage(image, imageInfo.rotationDegrees)).await()
        val landmarkResults = results.allPoseLandmarks
        val detectedLandmarks = mutableListOf<Int>()
        for (landmark in landmarkResults) {
            if (landmark.inFrameLikelihood > 0.7) {
                detectedLandmarks.add(landmark.landmarkType)
            }
        }

        return detectedLandmarks.containsAll(
            listOf(PoseLandmark.NOSE, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private suspend fun runPoseDetection() {
        PoseDetection.getClient(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build(),
        ).use { poseDetector ->
            // Since image analysis is processed by ML Kit asynchronously in its own thread pool,
            // we can run this directly from the calling coroutine scope instead of pushing this
            // work to a background dispatcher.
            cameraImageAnalysisUseCase.analyze { imageProxy ->
                imageProxy.image?.let { image ->
                    val poseDetected = poseDetector.detectPersonInFrame(image, imageProxy.imageInfo)
                    _uiState.update { it.copy(detectedPose = poseDetected) }
                }
            }
        }
    }
}

/**
 * Represents the UI state of the camera screen.
 *
 * @property surfaceRequest The current [SurfaceRequest] from the camera preview, or null if not yet available.
 * @property imageUri The [Uri] of the captured image, or null if no image has been captured.
 * @property detectedPose Indicates whether a pose has been detected, it's true by default.
 */
data class CameraUiState(
    val surfaceRequest: SurfaceRequest? = null,
    val imageUri: Uri? = null,
    val detectedPose: Boolean = false,
    val zoomMaxRatio: Float = 1f,
    val zoomMinRatio: Float = 1f,
    val zoomLevel: Float = 1f,
    val canFlipCamera: Boolean = true,
    val isRearCameraActive: Boolean = false,
    val autofocusUiState: AutofocusUiState = AutofocusUiState.Unspecified,
) {
    val zoomOptions = when {
        zoomMinRatio <= 0.6f && zoomMaxRatio >= 1f -> listOf(0.6f, 1f)
        zoomMinRatio < 1f && zoomMaxRatio >= 1f -> listOf(zoomMinRatio, 1f)
        zoomMinRatio <= 1f && zoomMaxRatio >= 2f -> listOf(1f, 2f)
        zoomMinRatio == zoomMaxRatio -> listOf(zoomMinRatio)
        else -> listOf(zoomMinRatio, zoomMaxRatio)
    }
}

/**
 * Represents the UI state of an autofocus event
 */
sealed interface AutofocusUiState {

    data object Unspecified : AutofocusUiState

    data class Specified(
        val surfaceCoordinates: Offset,
        val status: Status,
    ) : AutofocusUiState

    enum class Status {
        RUNNING,
        SUCCESS,
        FAILURE,
        CANCELLED,
    }
}
