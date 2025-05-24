package app.getnuri.feature.nuri_creation.text.entry

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.getnuri.data.MealDao
import app.getnuri.data.NuriMealAnalyzer
import app.getnuri.data.Meal
import app.getnuri.data.model.MealAnalysisData
import androidx.work.Data as WorkManagerData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.getnuri.background.FeedbackReminderWorker
import app.getnuri.background.MEAL_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed interface TextEntryAnalysisState {
    data object Idle : TextEntryAnalysisState
    data object Loading : TextEntryAnalysisState
    data class Success(val analysisData: MealAnalysisData) : TextEntryAnalysisState
    data class Error(val message: String) : TextEntryAnalysisState
    data object Saving : TextEntryAnalysisState
    data object Saved : TextEntryAnalysisState
}

data class MealTextEntryUiState(
    val mealDescription: String = "",
    val analysisState: TextEntryAnalysisState = TextEntryAnalysisState.Idle
)

@HiltViewModel
class MealTextEntryViewModel @Inject constructor(
    private val nuriMealAnalyzer: NuriMealAnalyzer,
    private val mealDao: MealDao,
    private val workManager: WorkManager // Inject WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealTextEntryUiState())
    val uiState: StateFlow<MealTextEntryUiState> = _uiState.asStateFlow()

    fun onMealDescriptionChange(newDescription: String) {
        // Allow description changes only if not actively loading or saving
        if (_uiState.value.analysisState !is TextEntryAnalysisState.Loading &&
            _uiState.value.analysisState !is TextEntryAnalysisState.Saving) {
            _uiState.update { it.copy(mealDescription = newDescription, analysisState = TextEntryAnalysisState.Idle) }
        }
    }

    fun analyzeMealDescription() {
        val currentDescription = _uiState.value.mealDescription
        if (currentDescription.isBlank()) {
            _uiState.update { it.copy(analysisState = TextEntryAnalysisState.Error("Description cannot be empty.")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(analysisState = TextEntryAnalysisState.Loading) }
            try {
                val result = nuriMealAnalyzer.analyzeMealFromText(currentDescription)
                result.fold(
                    onSuccess = { data ->
                        _uiState.update { currentState ->
                            currentState.copy(analysisState = TextEntryAnalysisState.Success(data))
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update { currentState ->
                            currentState.copy(analysisState = TextEntryAnalysisState.Error(throwable.message ?: "Unknown error during analysis"))
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(analysisState = TextEntryAnalysisState.Error(e.message ?: "Exception during analysis call"))
                }
            }
        }
    }

    fun saveMeal() {
        val currentUiState = _uiState.value
        if (currentUiState.analysisState is TextEntryAnalysisState.Success) {
            val analysisData = (currentUiState.analysisState as TextEntryAnalysisState.Success).analysisData
            _uiState.update { it.copy(analysisState = TextEntryAnalysisState.Saving) }
            viewModelScope.launch {
                try {
                    val meal = Meal(
                        id = 0,
                        timestamp = System.currentTimeMillis(),
                        inputType = "TEXT",
                        description = currentUiState.mealDescription,
                        photoUri = null,
                        rawExtractedIngredients = analysisData.extractedIngredients,
                        rawExtractedTriggers = analysisData.potentialTriggers,
                        userConfirmedIngredients = analysisData.extractedIngredients, // Same as raw for now
                        userConfirmedTriggers = analysisData.potentialTriggers,
                        notes = null
                    )
                    val mealId = mealDao.insertMeal(meal) // Get the returned ID
                    _uiState.update { it.copy(analysisState = TextEntryAnalysisState.Saved) }
                    Log.d("MealTextEntryVM", "Meal saved successfully with ID: $mealId")

                    // Schedule reminder worker
                    scheduleFeedbackReminder(mealId)

                } catch (e: Exception) {
                    Log.e("MealTextEntryVM", "Failed to save meal or schedule worker", e)
                    _uiState.update {
                        it.copy(analysisState = TextEntryAnalysisState.Error("Failed to save meal: ${e.message}"))
                    }
                }
            }
        } else {
            Log.w("MealTextEntryVM", "SaveMeal called in an invalid state: ${currentUiState.analysisState}")
            _uiState.update { it.copy(analysisState = TextEntryAnalysisState.Error("Cannot save meal, previous analysis not successful.")) }
        }
    }

    private fun scheduleFeedbackReminder(mealId: Long) {
        val workData = WorkManagerData.Builder()
            .putLong(MEAL_ID_KEY, mealId)
            .build()

        val feedbackWorkRequest = OneTimeWorkRequestBuilder<FeedbackReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(30, TimeUnit.MINUTES) // 30 minutes
            .addTag("feedback-reminder-text-$mealId") // Unique tag
            .build()

        workManager.enqueueUniqueWork(
            "feedback-reminder-for-meal-$mealId", // Unique work name (ensure this is unique per meal)
            ExistingWorkPolicy.REPLACE, // Or KEEP if you don't want to reschedule if one exists
            feedbackWorkRequest
        )
        Log.d("MealTextEntryVM", "Scheduled feedback reminder for meal ID: $mealId")
    }

    fun startNewEntry() {
        _uiState.update { MealTextEntryUiState(mealDescription = "", analysisState = TextEntryAnalysisState.Idle) }
    }
}
