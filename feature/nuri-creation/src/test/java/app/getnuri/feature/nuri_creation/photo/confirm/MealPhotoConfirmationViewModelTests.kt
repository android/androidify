package app.getnuri.feature.nuri_creation.photo.confirm

import android.net.Uri
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
class MealPhotoConfirmationViewModelTests {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // For LiveData if used, good for main dispatcher

    private lateinit var viewModel: MealPhotoConfirmationViewModel
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
        viewModel = MealPhotoConfirmationViewModel(nuriMealAnalyzer, mealDao, workManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test_analyzeMealPhoto_success()`() = runTest {
        val mockUri = mock<Uri>()
        val mockAnalysisData = MealAnalysisData(listOf("ingredient1"), listOf("trigger1"))
        whenever(nuriMealAnalyzer.analyzeMealFromImage(mockUri)).thenReturn(Result.success(mockAnalysisData))

        viewModel.uiState.test {
            assertEquals(MealPhotoConfirmUiState.Idle(), awaitItem(), "Initial state should be Idle")
            viewModel.analyzeMealPhoto(mockUri)

            val loadingState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Loading>(loadingState, "State should be Loading")
            assertEquals(mockUri, loadingState.imageUri)

            val successState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Success>(successState, "State should be Success")
            assertEquals(mockUri, successState.imageUri)
            assertEquals(mockAnalysisData, successState.analysisData)

            verify(nuriMealAnalyzer).analyzeMealFromImage(mockUri)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_analyzeMealPhoto_failure()`() = runTest {
        val mockUri = mock<Uri>()
        val exception = RuntimeException("Analysis failed")
        whenever(nuriMealAnalyzer.analyzeMealFromImage(mockUri)).thenReturn(Result.failure(exception))

        viewModel.uiState.test {
            assertEquals(MealPhotoConfirmUiState.Idle(), awaitItem(), "Initial state should be Idle")
            viewModel.analyzeMealPhoto(mockUri)

            val loadingState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Loading>(loadingState)
            assertEquals(mockUri, loadingState.imageUri)


            val errorState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Error>(errorState, "State should be Error")
            assertEquals(mockUri, errorState.imageUri)
            assertEquals(exception.message, errorState.message)

            verify(nuriMealAnalyzer).analyzeMealFromImage(mockUri)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_saveMeal_success()`() = runTest {
        val mockUri = mock<Uri>()
        val mockAnalysisData = MealAnalysisData(listOf("apple", "banana"), listOf("nuts"))
        val initialSuccessState = MealPhotoConfirmUiState.Success(mockUri, mockAnalysisData)
        val mockMealId = 123L

        // Set initial state to Success
        viewModel.setImageUri(mockUri) // Set URI first
        viewModel.analyzeMealPhoto(mockUri) // Trigger analysis to reach success state
         whenever(nuriMealAnalyzer.analyzeMealFromImage(mockUri)).thenReturn(Result.success(mockAnalysisData))
        advanceUntilIdle() // Ensure analyzeMealPhoto coroutine completes

        // Mock DAO and WorkManager interactions
        whenever(mealDao.insertMeal(any())).thenReturn(mockMealId)

        viewModel.uiState.test {
            // Consume initial Idle, Loading, and then the Success state set up
            assertEquals(MealPhotoConfirmUiState.Idle(mockUri), awaitItem())
            assertEquals(MealPhotoConfirmUiState.Loading(mockUri), awaitItem())
            assertEquals(initialSuccessState, awaitItem())


            viewModel.saveMeal()

            val savingState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Saving>(savingState)
            assertEquals(mockUri, savingState.imageUri)
            assertEquals(mockAnalysisData, savingState.analysisData)

            val savedState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Saved>(savedState)

            // Verify mealDao.insertMeal was called with a Meal object
            val mealCaptor = argumentCaptor<Meal>()
            verify(mealDao).insertMeal(mealCaptor.capture())
            assertEquals(mockUri.toString(), mealCaptor.firstValue.photoUri)
            assertEquals("PHOTO", mealCaptor.firstValue.inputType)
            assertEquals(mockAnalysisData.extractedIngredients, mealCaptor.firstValue.rawExtractedIngredients)

            // Verify WorkManager was called
            verify(workManager).enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `test_saveMeal_daoFailure()`() = runTest {
        val mockUri = mock<Uri>()
        val mockAnalysisData = MealAnalysisData(listOf("pasta", "cheese"), listOf("dairy"))
        val initialSuccessState = MealPhotoConfirmUiState.Success(mockUri, mockAnalysisData)
        val exception = RuntimeException("DAO insert failed")

        // Set initial state to Success
        viewModel.setImageUri(mockUri)
        viewModel.analyzeMealPhoto(mockUri)
        whenever(nuriMealAnalyzer.analyzeMealFromImage(mockUri)).thenReturn(Result.success(mockAnalysisData))
        advanceUntilIdle()

        // Mock DAO failure
        whenever(mealDao.insertMeal(any())).thenThrow(exception)

        viewModel.uiState.test {
            assertEquals(MealPhotoConfirmUiState.Idle(mockUri), awaitItem())
            assertEquals(MealPhotoConfirmUiState.Loading(mockUri), awaitItem())
            assertEquals(initialSuccessState, awaitItem())

            viewModel.saveMeal()

            val savingState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Saving>(savingState)

            val errorState = awaitItem()
            assertIs<MealPhotoConfirmUiState.Error>(errorState)
            assertTrue(errorState.message.contains("Failed to save meal"))

            verify(mealDao).insertMeal(any())
            verify(workManager, never()).enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `test_resetState()`() = runTest {
         val mockUri = mock<Uri>()
        // Set some non-idle state first
        viewModel.setImageUri(mockUri)
        viewModel.analyzeMealPhoto(mockUri)
        whenever(nuriMealAnalyzer.analyzeMealFromImage(mockUri)).thenReturn(Result.success(MealAnalysisData(emptyList(), emptyList())))
        advanceUntilIdle() // Ensure it's in Success state or similar

        viewModel.uiState.test {
            // Consume states until it's no longer Idle
            skipItems(2) // Skip initial Idle, Loading
            assertIs<MealPhotoConfirmUiState.Success>(awaitItem())


            viewModel.resetState(mockUri) // Reset with a URI
            assertEquals(MealPhotoConfirmUiState.Idle(mockUri), awaitItem())

            viewModel.resetState() // Reset without URI
            assertEquals(MealPhotoConfirmUiState.Idle(null), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

     @Test
    fun `setImageUri updates state when idle`() = runTest {
        val mockUri = mock<Uri>()
        viewModel.uiState.test {
            assertEquals(MealPhotoConfirmUiState.Idle(null), awaitItem()) // Initial state
            viewModel.setImageUri(mockUri)
            assertEquals(MealPhotoConfirmUiState.Idle(mockUri), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
