package app.getnuri.feature.nuri_creation.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.getnuri.data.model.MealAnalysisData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientExtractionViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(
        IngredientExtractionUiState(
            mealInfo = MealInfo("", null),
            ingredients = emptyList()
        )
    )
    val uiState: StateFlow<IngredientExtractionUiState> = _uiState.asStateFlow()
    
    fun initializeWithData(
        mealTitle: String,
        mealImageUri: String?,
        analysisData: MealAnalysisData?,
        scheduledTime: String? = null
    ) {
        val ingredients = analysisData?.toExtractedIngredients() ?: emptyList()
        _uiState.value = IngredientExtractionUiState(
            mealInfo = MealInfo(
                title = mealTitle,
                imageUri = mealImageUri,
                scheduledTime = scheduledTime
            ),
            ingredients = ingredients
        )
    }
    
    fun updateIngredient(index: Int, newIngredient: ExtractedIngredient) {
        viewModelScope.launch {
            val currentIngredients = _uiState.value.ingredients.toMutableList()
            if (index in currentIngredients.indices) {
                currentIngredients[index] = newIngredient
                _uiState.value = _uiState.value.copy(ingredients = currentIngredients)
            }
        }
    }
    
    fun removeIngredient(index: Int) {
        viewModelScope.launch {
            val currentIngredients = _uiState.value.ingredients.toMutableList()
            if (index in currentIngredients.indices) {
                currentIngredients.removeAt(index)
                _uiState.value = _uiState.value.copy(ingredients = currentIngredients)
            }
        }
    }
    
    fun addIngredient(ingredient: ExtractedIngredient) {
        viewModelScope.launch {
            val currentIngredients = _uiState.value.ingredients.toMutableList()
            currentIngredients.add(ingredient)
            _uiState.value = _uiState.value.copy(ingredients = currentIngredients)
        }
    }
    
    fun getIngredientsForSaving(): List<String> {
        return _uiState.value.ingredients.map { ingredient ->
            if (ingredient.quantity.isNotBlank() && ingredient.unit.isNotBlank()) {
                "${ingredient.name} | ${ingredient.quantity}${ingredient.unit}"
            } else if (ingredient.quantity.isNotBlank()) {
                "${ingredient.name} | ${ingredient.quantity}"
            } else {
                ingredient.name
            }
        }
    }
} 