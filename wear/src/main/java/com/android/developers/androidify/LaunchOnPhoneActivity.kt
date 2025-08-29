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
package com.android.developers.androidify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.widget.ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking

/**
 * A helper activity that launches the phone Androidify app. This Activity is only started from the
 * default watch face and does not form part of the rest of the Wear OS app experience.
 */
class LaunchOnPhoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(Intent.ACTION_VIEW, "androidify://launch".toUri())
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val helper = RemoteActivityHelper(this)
        val message: CharSequence = getString(R.string.continue_on_phone)

        ConfirmationOverlay()
            .setType(OPEN_ON_PHONE_ANIMATION)
            .setDuration(5000)
            .setMessage(message)
            .setOnAnimationFinishedListener {
                onAnimationFinished()
            }
            .showOn(this)

        runBlocking {
            try {
                helper.startRemoteActivity(intent).await()
            } catch (e: RemoteActivityHelper.RemoteIntentException) {
                Log.e("LaunchOnPhoneActivity", "Error launching on phone", e)
            }
        }
    }

    fun onAnimationFinished() {
        finish()
    }
}
