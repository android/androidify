/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.getnuri

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
@HiltAndroidApp
class AndroidifyApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import app.getnuri.background.FEEDBACK_CHANNEL_ID // Import the constant

    override fun onCreate() {
        super.onCreate()
        setStrictModePolicy()
        createNotificationChannels() // Create notification channels
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Feedback Reminder Channel
            val feedbackChannelName = "Nuri Feedback Reminders"
            val feedbackChannelDescription = "Reminders to provide feedback on your meals."
            val feedbackChannelImportance = NotificationManager.IMPORTANCE_DEFAULT
            val feedbackChannel = NotificationChannel(FEEDBACK_CHANNEL_ID, feedbackChannelName, feedbackChannelImportance).apply {
                description = feedbackChannelDescription
            }

            // Get the NotificationManager service
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(feedbackChannel)
        }
    }

    /**
     * Return true if the application is debuggable.
     */
    private fun isDebuggable(): Boolean {
        return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }

    /**
     * Set a thread policy that detects all potential problems on the main thread, such as network
     * and disk access.
     *
     * If a problem is found, the offending call will be logged and the application will be killed.
     */
    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                Builder().detectAll().penaltyLog().build(),
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return imageLoader.get()
    }
}
