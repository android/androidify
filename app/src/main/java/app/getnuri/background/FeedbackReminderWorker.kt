@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package app.getnuri.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.getnuri.MainActivity // Assuming MainActivity is the entry point
import app.getnuri.R // Assuming R class is in app.getnuri

const val MEAL_ID_KEY = "MEAL_ID_KEY"
const val FEEDBACK_CHANNEL_ID = "nuri_feedback_channel" // Must match channel created in Application class

class FeedbackReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val mealId = inputData.getLong(MEAL_ID_KEY, -1L)

        if (mealId == -1L) {
            return Result.failure()
        }

        // Create notification
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification Tap Action
        // This intent will be launched when the user taps the notification.
        // It should navigate to FeedbackEntryScreen with mealId.
        // For now, it opens MainActivity. Navigation to the specific screen
        // would be handled in MainActivity based on intent extras.
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "FeedbackEntryScreen") // Custom flag
            putExtra(MEAL_ID_KEY, mealId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            mealId.toInt(), // Use mealId as request code for uniqueness
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(applicationContext, FEEDBACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome) // Use available icon
            .setContentTitle("Nuri Meal Feedback")
            .setContentText("How are you feeling after your meal? Tap to give feedback.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification when tapped
            .build()

        try {
            notificationManager.notify(mealId.toInt(), notification) // Use mealId as notification ID
        } catch (e: SecurityException) {
            // This can happen if POST_NOTIFICATIONS permission is not granted on Android 13+
            // For this subtask, we assume permission is handled or will be declared.
            // In a real app, you'd request permission or handle this gracefully.
            return Result.failure() // Or Result.retry()
        }
        
        return Result.success()
    }
}
