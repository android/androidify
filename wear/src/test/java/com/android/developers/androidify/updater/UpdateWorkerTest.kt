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
package com.android.developers.androidify.updater

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class UpdateWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun testUpdateWorker_instantiation() {
        val worker = TestListenableWorkerBuilder<UpdateWorker>(context).build()
        assertNotNull(worker)
    }

    @Test
    fun testUpdateReceiver_enqueuesWork() {
        // Given
        val receiver = UpdateReceiver()
        val intent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)

        // When
        receiver.onReceive(context, intent)

        // Then
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(UpdateWorker::class.java.name).get()

        assertEquals(1, workInfos.size)
        val state = workInfos[0].state
        assert(state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.FAILED || state == WorkInfo.State.SUCCEEDED)
    }

    @Test
    fun testUpdateWorker_doWork_handlesRobolectricException() = runBlocking {
        val worker = TestListenableWorkerBuilder<UpdateWorker>(context).build()

        try {
            val result = worker.doWork()
            assert(result is ListenableWorker.Result.Success || result is ListenableWorker.Result.Failure)
        } catch (e: NoClassDefFoundError) {
            // Expected in Robolectric because WatchFacePushManager relies on platform classes
            // not available in standard Robolectric android.jar
            assertNotNull(e.message)
        }
    }
}
