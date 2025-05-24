package app.getnuri.feature.feedback.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Navigation comment:
// This screen is for entering feedback for a specific meal.
// It's typically launched from a notification or a meal history list.
// After submitting feedback, it should call onFeedbackSubmitted to navigate away.

val predefinedFeelings = listOf("Great", "Good", "Okay", "Bloated", "Tired")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackEntryScreen(
    mealId: Long,
    onFeedbackSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedbackEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize mealId in ViewModel if it's not already set or different
    LaunchedEffect(mealId) {
        viewModel.setMealId(mealId)
    }
    
    // Handle navigation when feedback is saved
    LaunchedEffect(uiState.submissionState) {
        if (uiState.submissionState == SubmissionState.Saved) {
            onFeedbackSubmitted()
            viewModel.resetSubmissionState() // Reset state after navigation
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Meal Feedback") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Feedback for Meal ID: ${uiState.mealId}", // Display mealId from ViewModel's state
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("How are you feeling?", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                predefinedFeelings.forEach { feeling ->
                    SelectableChip(
                        text = feeling,
                        isSelected = uiState.selectedFeeling == feeling,
                        onClick = { viewModel.selectFeeling(feeling) },
                        enabled = uiState.submissionState != SubmissionState.Saving
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Additional Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = uiState.submissionState != SubmissionState.Saving
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.submissionState == SubmissionState.Saving) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.saveFeedback() },
                    enabled = uiState.selectedFeeling.isNotBlank() && uiState.submissionState != SubmissionState.Saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Feedback")
                }
            }

            if (uiState.submissionState is SubmissionState.Error) {
                Text(
                    text = (uiState.submissionState as SubmissionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp), // Consistent with other custom chip-like elements if any
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
