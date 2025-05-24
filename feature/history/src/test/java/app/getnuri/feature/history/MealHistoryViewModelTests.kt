package app.getnuri.feature.history

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import app.getnuri.data.MealDao
import app.getnuri.data.UserFeedbackDao
import app.getnuri.data.Meal
import app.getnuri.data.UserFeedback
import app.getnuri.feature.history.model.MealWithFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class MealHistoryViewModelTests {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MealHistoryViewModel
    private lateinit var mealDao: MealDao
    private lateinit var userFeedbackDao: UserFeedbackDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mealDao = mock()
        userFeedbackDao = mock()
        viewModel = MealHistoryViewModel(mealDao, userFeedbackDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test_mealHistory_emitsCorrectly()`() = runTest {
        val meal1 = Meal(id = 1L, timestamp = 1000L, inputType = "PHOTO", photoUri = "uri1", rawExtractedIngredients = listOf("a"), rawExtractedTriggers = listOf("b"), userConfirmedIngredients = listOf("a"), userConfirmedTriggers = listOf("b"))
        val meal2 = Meal(id = 2L, timestamp = 2000L, inputType = "TEXT", description = "desc2", rawExtractedIngredients = listOf("c"), rawExtractedTriggers = listOf("d"), userConfirmedIngredients = listOf("c"), userConfirmedTriggers = listOf("d"))
        val meals = listOf(meal2, meal1) // Assuming DAO returns sorted by timestamp DESC

        val feedback1 = UserFeedback(id = 10L, mealId = 1L, feedbackTimestamp = 1100L, feelingDescription = "Good")
        val feedback2a = UserFeedback(id = 20L, mealId = 2L, feedbackTimestamp = 2100L, feelingDescription = "Great")
        val feedback2b = UserFeedback(id = 21L, mealId = 2L, feedbackTimestamp = 2200L, feelingDescription = "Okay")

        whenever(mealDao.getAllMeals()).thenReturn(flowOf(meals))
        whenever(userFeedbackDao.getFeedbackForMeal(1L)).thenReturn(flowOf(listOf(feedback1)))
        whenever(userFeedbackDao.getFeedbackForMeal(2L)).thenReturn(flowOf(listOf(feedback2a, feedback2b)))

        viewModel.mealHistory.test {
            val emittedHistory = awaitItem() // Initial value is emptyList, then emits combined
            
            // Depending on how stateIn is configured and how quickly flows combine,
            // we might get an empty list first. Turbine should give us the latest combined state.
            // If it's still empty, we might need to advance time or use a different approach for initial emission.
            // For robust testing, we might need to skip the initial emptyList from stateIn.
            val actualHistory = if (emittedHistory.isEmpty() && meals.isNotEmpty()) awaitItem() else emittedHistory


            assertEquals(2, actualHistory.size)

            // Check meal2 (should be first due to timestamp)
            assertEquals(meal2, actualHistory[0].meal)
            assertEquals(2, actualHistory[0].feedback.size)
            assertTrue(actualHistory[0].feedback.containsAll(listOf(feedback2a, feedback2b)))

            // Check meal1
            assertEquals(meal1, actualHistory[1].meal)
            assertEquals(1, actualHistory[1].feedback.size)
            assertTrue(actualHistory[1].feedback.contains(feedback1))
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_mealHistory_emptyMeals()`() = runTest {
        whenever(mealDao.getAllMeals()).thenReturn(flowOf(emptyList()))

        viewModel.mealHistory.test {
            val history = awaitItem()
            assertTrue(history.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_mealHistory_mealsWithNoFeedback()`() = runTest {
        val meal1 = Meal(id = 1L, timestamp = 1000L, inputType = "TEXT", description = "desc1", rawExtractedIngredients = emptyList(), rawExtractedTriggers = emptyList(), userConfirmedIngredients = emptyList(), userConfirmedTriggers = emptyList())
        val meals = listOf(meal1)

        whenever(mealDao.getAllMeals()).thenReturn(flowOf(meals))
        whenever(userFeedbackDao.getFeedbackForMeal(1L)).thenReturn(flowOf(emptyList()))

        viewModel.mealHistory.test {
            val emittedHistory = awaitItem()
            val actualHistory = if (emittedHistory.isEmpty() && meals.isNotEmpty()) awaitItem() else emittedHistory


            assertEquals(1, actualHistory.size)
            assertEquals(meal1, actualHistory[0].meal)
            assertTrue(actualHistory[0].feedback.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `test_mealHistory_daoReturnsMealsSortedCorrectly()`() = runTest {
        // Test assumes that the viewmodel relies on DAO to sort meals.
        // Here, we provide meals out of order to see if the final list reflects DAO's (mocked) order.
        val meal1 = Meal(id = 1L, timestamp = 1000L, inputType = "PHOTO", photoUri = "uri1", rawExtractedIngredients = listOf("a"), rawExtractedTriggers = listOf("b"), userConfirmedIngredients = listOf("a"), userConfirmedTriggers = listOf("b"))
        val meal2 = Meal(id = 2L, timestamp = 3000L, inputType = "TEXT", description = "desc2", rawExtractedIngredients = listOf("c"), rawExtractedTriggers = listOf("d"), userConfirmedIngredients = listOf("c"), userConfirmedTriggers = listOf("d"))
        val meal3 = Meal(id = 3L, timestamp = 2000L, inputType = "PHOTO", photoUri = "uri3", rawExtractedIngredients = listOf("e"), rawExtractedTriggers = listOf("f"), userConfirmedIngredients = listOf("e"), userConfirmedTriggers = listOf("f"))

        // Mock DAO to return meals sorted by timestamp DESC (meal2, meal3, meal1)
        val sortedMealsFromDao = listOf(meal2, meal3, meal1)
        whenever(mealDao.getAllMeals()).thenReturn(flowOf(sortedMealsFromDao))
        whenever(userFeedbackDao.getFeedbackForMeal(anyLong())).thenReturn(flowOf(emptyList())) // No feedback for simplicity

        viewModel.mealHistory.test {
            val emittedHistory = awaitItem()
            val actualHistory = if (emittedHistory.isEmpty() && sortedMealsFromDao.isNotEmpty()) awaitItem() else emittedHistory

            assertEquals(3, actualHistory.size)
            assertEquals(meal2.id, actualHistory[0].meal.id) // meal2 (3000L) should be first
            assertEquals(meal3.id, actualHistory[1].meal.id) // meal3 (2000L) should be second
            assertEquals(meal1.id, actualHistory[2].meal.id) // meal1 (1000L) should be third
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
