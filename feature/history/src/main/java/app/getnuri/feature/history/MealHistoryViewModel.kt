package app.getnuri.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.getnuri.data.MealDao
import app.getnuri.data.UserFeedbackDao
import app.getnuri.feature.history.model.MealWithFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MealHistoryViewModel @Inject constructor(
    private val mealDao: MealDao,
    private val userFeedbackDao: UserFeedbackDao
) : ViewModel() {

    val mealHistory: StateFlow<List<MealWithFeedback>> =
        mealDao.getAllMeals() // Assuming this returns Flow<List<Meal>> sorted by timestamp DESC
            .flatMapLatest { meals ->
                if (meals.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    // Ensure meals are sorted if not already by DAO (important for consistent display)
                    // val sortedMeals = meals.sortedByDescending { it.timestamp }
                    // Using 'meals' directly assuming DAO sorts it.

                    val flows: List<Flow<MealWithFeedback>> = meals.map { meal ->
                        userFeedbackDao.getFeedbackForMeal(meal.id)
                            .map { feedbackList ->
                                MealWithFeedback(meal, feedbackList)
                            }
                    }
                    // The combine operator will emit a new list whenever any of the inner flows emit.
                    combine(flows) { arrayOfMealWithFeedback ->
                        // The result of combine is an Array, convert it to List
                        arrayOfMealWithFeedback.toList()
                            // Since getAllMeals is sorted by timestamp desc, and we map in that order,
                            // the combined list should also maintain this order.
                    }
                }
            }
            .distinctUntilChanged() // Only emit when the list content actually changes
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
}
