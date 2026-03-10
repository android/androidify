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
package com.android.developers.androidify.watchface

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.developers.androidify.watchface.creator.WatchFaceCreatorImpl
import com.android.developers.androidify.watchface.creator.WatchFacePackage
import com.android.developers.androidify.watchface.transfer.MIN_WATCH_FACE_SDK_VERSION
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = MIN_WATCH_FACE_SDK_VERSION)
class WatchFaceCreatorImplTest {
    private lateinit var context: Context
    private lateinit var watchFaceCreator: WatchFaceCreatorImpl
    private var tempWatchFaceFile: File? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        watchFaceCreator = WatchFaceCreatorImpl(context)
    }

    @Test
    fun createWatchFacePackage_withRealAssetsAndRealValidator_success() {
        val sampleBitmap = Bitmap.createBitmap(312, 312, Bitmap.Config.ARGB_8888)
        val watchFaceName = "androiddigital"

        var watchFacePackage: WatchFacePackage? = null
        var exception: Exception? = null

        try {
            watchFacePackage = watchFaceCreator.createWatchFacePackage(sampleBitmap, watchFaceName)
            tempWatchFaceFile = watchFacePackage.file
        } catch (e: IllegalStateException) {
            exception = e
            println("Watch face validation failed: ${e.message}")
        } catch (e: IOException) {
            exception = e
            println("Asset loading failed: ${e.message}")
        } catch (e: Exception) {
            exception = e
            println("An unexpected error occurred: ${e.message}")
            e.printStackTrace()
        }

        assertNull("Test failed due to an exception: ${exception?.message}", exception)
        assertNotNull("WatchFacePackage should not be null", watchFacePackage)
        assertNotNull("Watch face file should not be null", watchFacePackage!!.file)
        assertTrue("Watch face file should exist", watchFacePackage.file.exists())
        assertTrue("File name should start with 'watchface'", watchFacePackage.file.name.startsWith("watchface"))
        assertTrue("File name should end with '.apk'", watchFacePackage.file.name.endsWith(".apk"))

        assertNotNull("Validation token should not be null", watchFacePackage.validationToken)
        assertFalse("Validation token should not be empty", watchFacePackage.validationToken.isEmpty())
    }

    @Test
    fun createWatchFacePackage_invalidAssets_throwsIllegalStateException() {
        val watchFaceNameForFailureTest = "invalid_watchface_xml"
        val sampleBitmap = Bitmap.createBitmap(312, 312, Bitmap.Config.ARGB_8888)
        var exception: IllegalStateException? = null

        try {
            val watchFacePackage = watchFaceCreator.createWatchFacePackage(sampleBitmap, watchFaceNameForFailureTest)
            tempWatchFaceFile = watchFacePackage.file
        } catch (e: IllegalStateException) {
            exception = e
        } catch (e: Exception) {
            fail("Expected IllegalStateException for validation failure, but got ${e.javaClass.simpleName}: ${e.message}")
        }

        assertNotNull(
            "Expected IllegalStateException because watch face validation should fail for '$watchFaceNameForFailureTest'",
            exception,
        )
    }

    @After
    fun tearDown() {
        tempWatchFaceFile?.deleteOnExit()
        tempWatchFaceFile?.delete()
        tempWatchFaceFile = null
    }
}
