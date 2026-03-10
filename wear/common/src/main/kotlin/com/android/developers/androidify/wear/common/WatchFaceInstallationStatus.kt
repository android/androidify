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
package com.android.developers.androidify.wear.common

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@Serializable
sealed class WatchFaceInstallationStatus() {
    @Serializable
    object Unknown : WatchFaceInstallationStatus()

    @Serializable
    object NotStarted : WatchFaceInstallationStatus()

    @Serializable
    data class Receiving(
        val otherNodeId: String,
        val transferId: String,
        val validationToken: String,
        val activationStrategy: WatchFaceActivationStrategy,
    ) : WatchFaceInstallationStatus()

    object Preparing : WatchFaceInstallationStatus()

    object Sending : WatchFaceInstallationStatus()

    @Serializable
    data class Complete(
        val success: Boolean,
        val otherNodeId: String,
        val transferId: String = "",
        val validationToken: String = "",
        val activationStrategy: WatchFaceActivationStrategy = WatchFaceActivationStrategy.NO_ACTION_NEEDED,
        val installError: WatchFaceInstallError,
    ) : WatchFaceInstallationStatus()
}

@OptIn(ExperimentalSerializationApi::class)
object WatchFaceInstallationStatusSerializer : Serializer<WatchFaceInstallationStatus> {
    override val defaultValue = WatchFaceInstallationStatus.NotStarted

    override suspend fun readFrom(input: InputStream): WatchFaceInstallationStatus {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Palette", serialization)
        }
    }

    override suspend fun writeTo(t: WatchFaceInstallationStatus, output: OutputStream) {
        output.write(
            ProtoBuf.encodeToByteArray(t),
        )
    }
}

val Context.watchFaceInstallationStatusDataStore: DataStore<WatchFaceInstallationStatus> by dataStore(
    fileName = "watch_face_installation_status_data_store",
    serializer = WatchFaceInstallationStatusSerializer,
)
