/*
 * Copyright 2026 The Android Open Source Project
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

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class CrashlyticsTreeTest {

    private class TestableCrashlyticsTree : Timber.Tree() {
        var loggedPriority: Int = 0
        var loggedTag: String? = null
        var loggedMessage: String? = null
        var recordedException: Throwable? = null

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            loggedPriority = priority
            loggedTag = tag
            loggedMessage = message
            recordedException = t
        }
    }

    private lateinit var tree: TestableCrashlyticsTree

    @Before
    fun setUp() {
        tree = TestableCrashlyticsTree()
        Timber.plant(tree)
    }

    @After
    fun tearDown() {
        Timber.uproot(tree)
    }

    @Test
    fun testLog_verboseAndDebug_ignored() {
        Timber.tag("TEST_TAG").v("Verbose log")
        Timber.tag("TEST_TAG").d("Debug log")

        assertNull(tree.loggedMessage)
        assertNull(tree.recordedException)
    }

    @Test
    fun testLog_infoAndError_logged() {
        val exception = RuntimeException("Test error")
        Timber.tag("TEST_TAG").e(exception, "Error message")

        assertEquals(Log.ERROR, tree.loggedPriority)
        assertEquals("TEST_TAG", tree.loggedTag)
        assertTrue(tree.loggedMessage?.startsWith("Error message") == true)
        assertEquals(exception, tree.recordedException)
    }
}
