package app.getnuri.feature.nuri_creation.text.entry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import app.cash.turbine.test
import app.getnuri.data.MealDao
import app.getnuri.data.NuriMealAnalyzer
import app.getnuri.data.model.MealAnalysisData
import app.getnuri.data.Meal // Ensure correct import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class MealTextEntryViewModelTests {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MealTextEntryViewModel
    private lateinit var nuriMealAnalyzer: NuriMealAnalyzer
    private lateinit var mealDao: MealDao
    private lateinit var workManager: WorkManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        nuriMealAnalyzer = mock()
        mealDao = mock()
        workManager = mock()
        viewModel = MealTextEntryViewModel(nuriMealAnalyzer, mealDao, workManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test_onMealDescriptionChange_updatesState()`() = runTest {
        val newDescription = "Chicken and rice"
        viewModel.uiState.test {
            assertEquals(MealTextEntryUiState(), awaitItem()) // Initial state

            viewModel.onMealDescriptionChange(newDescription)
            val updatedState = awaitItem()
            assertEquals(newDescription, updatedState.mealDescription)
            assertIs<TextEntryAnalysisState.Idle>(updatedState.analysisState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_analyzeMealDescription_success()`() = runTest {
        val description = "Salad with dressing"
        val mockAnalysisData = MealAnalysisData(listOf("lettuce", "tomato"), listOf("dairy"))
        whenever(nuriMealAnalyzer.analyzeMealFromText(description)).thenReturn(Result.success(mockAnalysisData))

        viewModel.onMealDescriptionChange(description) // Set description first

        viewModel.uiState.test {
            assertEquals(MealTextEntryUiState(description), awaitItem()) // State after description change

            viewModel.analyzeMealDescription()

            assertEquals(MealTextEntryUiState(description, TextEntryAnalysisState.Loading), awaitItem())

            val successState = awaitItem()
            assertIs<TextEntryAnalysisState.Success>(successState.analysisState)
            assertEquals(mockAnalysisData, (successState.analysisState as TextEntryAnalysisState.Success).analysisData)
            assertEquals(description, successState.mealDescription)

            verify(nuriMealAnalyzer).analyzeMealFromText(description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_analyzeMealDescription_failure()`() = runTest {
        val description = "Spicy curry"
        val exception = RuntimeException("AI analysis failed")
        whenever(nuriMealAnalyzer.analyzeMealFromText(description)).thenReturn(Result.failure(exception))

        viewModel.onMealDescriptionChange(description)

        viewModel.uiState.test {
            assertEquals(MealTextEntryUiState(description), awaitItem())

            viewModel.analyzeMealDescription()

            assertEquals(MealTextEntryUiState(description, TextEntryAnalysisState.Loading), awaitItem())

            val errorState = awaitItem()
            assertIs<TextEntryAnalysisState.Error>(errorState.analysisState)
            assertEquals(exception.message, (errorState.analysisState as TextEntryAnalysisState.Error).message)
            assertEquals(description, errorState.mealDescription)

            verify(nuriMealAnalyzer).analyzeMealFromText(description)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `test_analyzeMealDescription_emptyDescription_returnsError()`() = runTest {
        viewModel.onMealDescriptionChange("") // Ensure description is blank

        viewModel.uiState.test {
            assertEquals(MealTextEntryUiState(""), awaitItem())

            viewModel.analyzeMealDescription()

            val errorState = awaitItem()
            assertIs<TextEntryAnalysisState.Error>(errorState.analysisState)
            assertEquals("Description cannot be empty.", (errorState.analysisState as TextEntryAnalysisState.Error).message)
            
            verify(nuriMealAnalyzer, never()).analyzeMealFromText(any())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `test_saveMeal_success()`() = runTest {
        val description = "Omelette with spinach"
        val mockAnalysisData = MealAnalysisData(listOf("eggs", "spinach"), emptyList())
        val mockMealId = 456L

        // Set initial state to Success
        viewModel.onMealDescriptionChange(description)
        whenever(nuriMealAnalyzer.analyzeMealFromText(description)).thenReturn(Result.success(mockAnalysisData))
        viewModel.analyzeMealDescription()
        advanceUntilIdle() // Ensure analyzeMealDescription coroutine completes

        whenever(mealDao.insertMeal(any())).thenReturn(mockMealId)

        viewModel.uiState.test {
            // Consume states: initial, description set, loading, success
            assertEquals(MealTextEntryUiState(description, TextEntryAnalysisState.Success(mockAnalysisData)), awaitItem())
            
            viewModel.saveMeal()

            assertEquals(MealTextEntryUiState(description, TextEntryAnalysisState.Saving), awaitItem())
            assertEquals(MealTextEntryUiState(description, TextEntryAnalysisState.Saved), awaitItem())

            val mealCaptor = argumentCaptor<Meal>()
            verify(mealDao).insertMeal(mealCaptor.capture())
            assertEquals(description, mealCaptor.firstValue.description)
            assertEquals("TEXT", mealCaptor.firstValue.inputType)
            assertEquals(null, mealCaptor.firstValue.photoUri)
            assertEquals(mockAnalysisData.extractedIngredients, mealCaptor.firstValue.rawExtractedIngredients)

            verify(workManager).enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `test_saveMeal_notInSuccessState_emitsError()`() = runTest {
        val description = "Initial description"
        viewModel.onMealDescriptionChange(description) // State is Idle

        viewModel.uiState.test {
            assertEquals(MealTextEntryUiState(description, TextEntryAnalysisState.Idle), awaitItem())

            viewModel.saveMeal() // Attempt to save when not in Success state

            val errorState = awaitItem()
            assertIs<TextEntryAnalysisState.Error>(errorState.analysisState)
            assertEquals("Cannot save meal, previous analysis not successful.", (errorState.analysisState as TextEntryAnalysisState.Error).message)
            
            verify(mealDao, never()).insertMeal(any())
            verify(workManager, never()).enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_startNewEntry_resetsState()`() = runTest {
        // Set some state first
        viewModel.onMealDescriptionChange("Old entry")
        whenever(nuriMealAnalyzer.analyzeMealFromText(any())).thenReturn(Result.success(MealAnalysisData(emptyList(), emptyList())))
        viewModel.analyzeMealDescription()
        advanceUntilIdle() // Ensure it's in Success state

        viewModel.uiState.test {
            assertIs<TextEntryAnalysisState.Success>((awaitItem()).analysisState)

            viewModel.startNewEntry()
            val newState = awaitItem()
            assertEquals("", newState.mealDescription)
            assertIs<TextEntryAnalysisState.Idle>(newState.analysisState)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
