package app.getnuri.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Update
    suspend fun updateMeal(meal: Meal)

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    fun getMealById(mealId: Long): Flow<Meal?>
}

@Dao
interface UserFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: UserFeedback): Long

    @Query("SELECT * FROM user_feedback WHERE mealId = :mealId ORDER BY feedbackTimestamp DESC")
    fun getFeedbackForMeal(mealId: Long): Flow<List<UserFeedback>>
}
