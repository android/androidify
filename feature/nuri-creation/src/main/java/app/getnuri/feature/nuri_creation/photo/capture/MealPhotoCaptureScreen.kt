package app.getnuri.feature.nuri_creation.photo.capture

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.getnuri.camera.CameraPreviewScreen // Assuming CameraPreviewScreen is in this package

// Navigation comment:
// This screen will be a destination in the Nuri creation navigation graph.
// Upon successful image capture, it should navigate to MealPhotoConfirmationScreen.

/**
 * A composable screen that hosts the CameraPreviewScreen for capturing a meal photo.
 *
 * @param navigateToConfirmation Lambda to be invoked when an image is captured,
 *                               passing the Uri of the captured image.
 */
@Composable
fun MealPhotoCaptureScreen(
    modifier: Modifier = Modifier,
    navigateToConfirmation: (Uri) -> Unit
) {
    CameraPreviewScreen(
        modifier = modifier,
        onImageCaptured = { uri, _ -> // Second param is isTextDetected, not needed here
            // When an image is captured by CameraPreviewScreen,
            // invoke the navigateToConfirmation lambda with the image URI.
            navigateToConfirmation(uri)
        },
        onError = { exception ->
            // Handle camera errors, e.g., log them or show a message.
            // For now, just print the error.
            println("Camera error: ${exception.message}")
        }
    )
}
