package app.getnuri.feature.nuri_creation.photo.confirm

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

// Navigation comment:
// This screen is reached after a photo is captured in MealPhotoCaptureScreen.
// - "Use this Photo" button: triggers AI processing via ViewModel.
// - "Retake" button: should navigate back to MealPhotoCaptureScreen and reset ViewModel state.
// - "Save Meal" button (after success): Triggers saving via ViewModel.
// - "Start Over" button (after saved): Resets ViewModel for a new photo.

/**
 * A composable screen that displays the captured meal photo for confirmation and analysis.
 *
 * @param imageUri The URI of the captured image to display.
 * @param onRetakePhoto Lambda to be invoked when the user decides to retake the photo.
 *                      This should typically navigate back.
 * @param onSaveMeal Lambda to be invoked when user wants to save the meal after analysis.
 * @param modifier Modifier for this composable.
 * @param viewModel The ViewModel for this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPhotoConfirmationScreen(
    imageUri: Uri,
    onRetakePhoto: () -> Unit, // Navigates back to capture screen
    onStartOver: () -> Unit, // Navigates back to capture screen or a common start
    modifier: Modifier = Modifier,
    viewModel: MealPhotoConfirmationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Ensure the ViewModel has the initial image URI
    LaunchedEffect(imageUri) {
        viewModel.setImageUri(imageUri)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Confirm Photo & Analyze") }) // Replace with actual string resource
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Determine current URI from state for image display, fallback to input if needed
            val currentImageUriForDisplay = when (val state = uiState) {
                is MealPhotoConfirmUiState.Idle -> state.imageUri ?: imageUri
                is MealPhotoConfirmUiState.Loading -> state.imageUri
                is MealPhotoConfirmUiState.Success -> state.imageUri
                is MealPhotoConfirmUiState.Error -> state.imageUri ?: imageUri
                is MealPhotoConfirmUiState.Saving -> state.imageUri
                MealPhotoConfirmUiState.Saved -> null // Don't show image after save, or show a placeholder
            }

            if (currentImageUriForDisplay != null && uiState !is MealPhotoConfirmUiState.Saved) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(currentImageUriForDisplay)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Captured meal photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (val state = uiState) {
                is MealPhotoConfirmUiState.Idle -> {
                    state.imageUri?.let { uri -> // Ensure URI is set before showing buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    viewModel.resetState(uri) // Pass current URI if needed for retake logic
                                    onRetakePhoto()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retake")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = { viewModel.analyzeMealPhoto(uri) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use this Photo")
                            }
                        }
                    } ?: run {
                         // Show a loading or placeholder if URI isn't set yet in Idle.
                         // This case should ideally be brief due to LaunchedEffect.
                        CircularProgressIndicator()
                    }
                }
                is MealPhotoConfirmUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                is MealPhotoConfirmUiState.Success -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item {
                            Text("Analysis Complete!", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = state.analysisData.extractedIngredients.joinToString(", ").ifEmpty { "None identified" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Potential Triggers:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = state.analysisData.potentialTriggers.joinToString(", ").ifEmpty { "None identified" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveMeal() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Meal")
                    }
                }
                is MealPhotoConfirmUiState.Saving -> {
                     LazyColumn(modifier = Modifier.weight(1f)) { // Keep showing data while saving
                        item {
                            Text("Analysis Complete!", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = state.analysisData.extractedIngredients.joinToString(", ").ifEmpty { "None identified" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Potential Triggers:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = state.analysisData.potentialTriggers.joinToString(", ").ifEmpty { "None identified" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Button disabled or shows saving indicator */ },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).height(20.dp))
                        Text("Saving Meal...")
                    }
                }
                MealPhotoConfirmUiState.Saved -> {
                    Text("Meal Saved Successfully!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.resetState() // Reset for next operation
                            onStartOver()      // Navigate to start a new meal entry
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Another Meal")
                    }
                }
                is MealPhotoConfirmUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            state.imageUri?.let { viewModel.analyzeMealPhoto(it) } ?: onRetakePhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try Analysis Again")
                    }
                    Button(
                        onClick = {
                            viewModel.resetState(state.imageUri) // Keep URI for potential retake
                            onRetakePhoto()
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Retake Photo")
                    }
                }
            }
        }
    }
}
