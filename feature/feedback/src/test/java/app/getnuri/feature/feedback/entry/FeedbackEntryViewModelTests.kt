package app.getnuri.feature.feedback.entry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import app.getnuri.data.UserFeedbackDao
import app.getnuri.data.UserFeedback // Ensure correct import
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

@ExperimentalCoroutinesApi
class FeedbackEntryViewModelTests {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FeedbackEntryViewModel
    private lateinit var userFeedbackDao: UserFeedbackDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userFeedbackDao = mock()
        viewModel = FeedbackEntryViewModel(userFeedbackDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test_setMealId_updatesState()`() = runTest {
        val mealId = 1L
        viewModel.uiState.test {
            assertEquals(FeedbackEntryUiState(), awaitItem()) // Initial state

            viewModel.setMealId(mealId)
            val updatedState = awaitItem()
            assertEquals(mealId, updatedState.mealId)

            // Try setting again, should not emit if same and not saving
            viewModel.setMealId(mealId)
            expectNoEvents() // No new state if ID is the same and not saving

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_selectFeeling_updatesState()`() = runTest {
        val feeling = "Great"
        viewModel.uiState.test {
            assertEquals(FeedbackEntryUiState(), awaitItem())

            viewModel.selectFeeling(feeling)
            val updatedState = awaitItem()
            assertEquals(feeling, updatedState.selectedFeeling)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_updateNotes_updatesState()`() = runTest {
        val notes = "Feeling much better after this meal."
        viewModel.uiState.test {
            assertEquals(FeedbackEntryUiState(), awaitItem())

            viewModel.updateNotes(notes)
            val updatedState = awaitItem()
            assertEquals(notes, updatedState.notes)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_saveFeedback_success()`() = runTest {
        val mealId = 1L
        val feeling = "Good"
        val notes = "No issues."

        viewModel.setMealId(mealId)
        viewModel.selectFeeling(feeling)
        viewModel.updateNotes(notes)
        advanceUntilIdle() // ensure state updates are processed

        // Mock DAO interaction
        whenever(userFeedbackDao.insertFeedback(any())).thenReturn(Unit) // insertFeedback is suspend but returns Unit

        viewModel.uiState.test {
            // Initial state after updates
            assertEquals(FeedbackEntryUiState(mealId, feeling, "", notes, SubmissionState.Idle), awaitItem())

            viewModel.saveFeedback()

            assertEquals(FeedbackEntryUiState(mealId, feeling, "", notes, SubmissionState.Saving), awaitItem())
            assertEquals(FeedbackEntryUiState(mealId, feeling, "", notes, SubmissionState.Saved), awaitItem())

            val feedbackCaptor = argumentCaptor<UserFeedback>()
            verify(userFeedbackDao).insertFeedback(feedbackCaptor.capture())
            assertEquals(mealId, feedbackCaptor.firstValue.mealId)
            assertEquals(feeling, feedbackCaptor.firstValue.feelingDescription)
            assertEquals(notes, feedbackCaptor.firstValue.feedbackNotes)

            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `test_saveFeedback_noFeelingSelected_returnsError()`() = runTest {
        viewModel.setMealId(1L)
        // No feeling selected

        viewModel.uiState.test {
            skipItems(1) // Skip initial state, get the one with mealId set

            viewModel.saveFeedback()
            val errorState = awaitItem()
            assertIs<SubmissionState.Error>(errorState.submissionState)
            assertEquals("Please select a feeling.", (errorState.submissionState as SubmissionState.Error).message)
            
            verify(userFeedbackDao, never()).insertFeedback(any())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test_saveFeedback_mealIdNotSet_returnsError()`() = runTest {
        viewModel.selectFeeling("Okay")
        // mealId is still 0L (default)

        viewModel.uiState.test {
             skipItems(1) // Skip initial state, get the one with feeling set

            viewModel.saveFeedback()
            val errorState = awaitItem()
            assertIs<SubmissionState.Error>(errorState.submissionState)
            assertEquals("Meal ID is not set.", (errorState.submissionState as SubmissionState.Error).message)

            verify(userFeedbackDao, never()).insertFeedback(any())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `test_saveFeedback_daoFailure()`() = runTest {
        val mealId = 1L
        val feeling = "Bad"
        val exception = RuntimeException("DAO insert failed")

        viewModel.setMealId(mealId)
        viewModel.selectFeeling(feeling)
        advanceUntilIdle()

        whenever(userFeedbackDao.insertFeedback(any())).thenThrow(exception)

        viewModel.uiState.test {
            skipItems(1) // Initial state after updates

            viewModel.saveFeedback()

            assertIs<SubmissionState.Saving>((awaitItem()).submissionState)

            val errorState = awaitItem()
            assertIs<SubmissionState.Error>(errorState.submissionState)
            assertEquals("Failed to save feedback: ${exception.message}", (errorState.submissionState as SubmissionState.Error).message)

            verify(userFeedbackDao).insertFeedback(any())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `resetSubmissionState resets only submission state`() = runTest {
        viewModel.setMealId(1L)
        viewModel.selectFeeling("Good")
        viewModel.updateNotes("Test notes")
        // Simulate an error state
        _ დღემდე ((viewModel as Any).javaClass.getDeclaredField("_uiState").apply { isAccessible = true }.get(viewModel) as MutableStateFlow<FeedbackEntryUiState>)
            .value = FeedbackEntryUiState(1L, "Good", "", "Test notes", SubmissionState.Error("Previous error"))


        viewModel.uiState.test {
            assertEquals(SubmissionState.Error("Previous error"), (awaitItem()).submissionState)

            viewModel.resetSubmissionState()
            val newState = awaitItem()
            assertEquals(SubmissionState.Idle, newState.submissionState)
            assertEquals(1L, newState.mealId) // mealId should be preserved
            assertEquals("Good", newState.selectedFeeling) // feeling should be preserved
            assertEquals("Test notes", newState.notes) // notes should be preserved
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
