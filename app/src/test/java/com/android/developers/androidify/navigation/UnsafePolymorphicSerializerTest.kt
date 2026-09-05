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
package com.android.developers.androidify.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Serializable
data class TestData(val id: Int, val name: String)

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class UnsafePolymorphicSerializerTest {

    private val json = Json { serializersModule = Json.serializersModule }

    @Test
    fun testUnsafePolymorphicSerializer_serializeAndDeserialize() {
        val serializer = UnsafePolymorphicSerializer<TestData>()
        val data = TestData(42, "Androidify")

        val encoded = json.encodeToString(serializer, data)
        val decoded = json.decodeFromString(serializer, encoded)

        assertEquals(data, decoded)
    }
}
