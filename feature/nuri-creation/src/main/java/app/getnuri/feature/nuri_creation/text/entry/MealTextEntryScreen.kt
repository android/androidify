package app.getnuri.feature.nuri_creation.text.entry

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Navigation comment:
// This screen is for text-based meal input.
// "Analyze Meal" triggers AI processing.
// "Save Meal" (on success) saves the meal.
// "Enter Another Meal" (on saved) resets the screen.

/**
 * A composable screen for entering meal details via text, analyzing, and saving them.
 * It uses [MealTextEntryViewModel] to manage state.
 *
 * @param onMealSaved Callback invoked after a meal is successfully saved, typically for navigation.
 * @param modifier Modifier for this composable.
 * @param viewModel The ViewModel for this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTextEntryScreen(
    onMealSaved: () -> Unit, // Callback after meal is saved successfully
    modifier: Modifier = Modifier,
    viewModel: MealTextEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Describe, Analyze & Save Meal") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (val state = uiState.analysisState) {
                TextEntryAnalysisState.Idle, is TextEntryAnalysisState.Error -> {
                    OutlinedTextField(
                        value = uiState.mealDescription,
                        onValueChange = { viewModel.onMealDescriptionChange(it) },
                        label = { Text("e.g., Chicken salad with avocado") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        enabled = state is TextEntryAnalysisState.Idle || state is TextEntryAnalysisState.Error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.analyzeMealDescription() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.mealDescription.isNotBlank() && state is TextEntryAnalysisState.Idle
                    ) {
                        Text("Analyze Meal")
                    }
                    if (state is TextEntryAnalysisState.Error) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        Button(
                            onClick = { viewModel.onMealDescriptionChange(uiState.mealDescription) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
                TextEntryAnalysisState.Loading -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                is TextEntryAnalysisState.Success -> {
                    DisplayAnalysisResults(uiState.mealDescription, state.analysisData, Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveMeal() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Meal")
                    }
                    Button(
                        onClick = { viewModel.startNewEntry() },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Clear & Start Over")
                    }
                }
                TextEntryAnalysisState.Saving -> {
                    // Optionally show analysis data while saving
                    if (uiState.analysisState is TextEntryAnalysisState.Success) { // Should not happen due to state machine logic
                         DisplayAnalysisResults(uiState.mealDescription, (uiState.analysisState as TextEntryAnalysisState.Success).analysisData, Modifier.weight(1f))
                    } else {
                        // Fallback or keep previous view if analysis data isn't available in Saving state directly
                         Text("Description: ${uiState.mealDescription}", style = MaterialTheme.typography.bodyLarge)
                         Spacer(modifier = Modifier.height(16.dp))
                    }
                     Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Saving Meal...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                TextEntryAnalysisState.Saved -> {
                    Text("Meal Saved Successfully!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.startNewEntry()
                            onMealSaved() // Invoke callback
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Another Meal")
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayAnalysisResults(description: String, analysisData: app.getnuri.data.model.MealAnalysisData, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        item {
            Text("Original Description:", style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analysis Complete!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = analysisData.extractedIngredients.joinToString(", ").ifEmpty { "None identified" },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Potential Triggers:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = analysisData.potentialTriggers.joinToString(", ").ifEmpty { "None identified" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
