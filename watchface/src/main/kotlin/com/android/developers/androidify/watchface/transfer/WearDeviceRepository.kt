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
@file:OptIn(ExperimentalSerializationApi::class)

package com.android.developers.androidify.watchface.transfer

import android.content.Context
import com.android.developers.androidify.wear.common.ConnectedWatch
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_INSTALLED_WEAR
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject
import javax.inject.Singleton

interface WearDeviceRepository {
    val connectedWatch: Flow<ConnectedWatch?>
}

@Singleton
class WearDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
) : WearDeviceRepository {
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }

    override val connectedWatch = callbackFlow {
        var capabilityListener: CapabilityClient.OnCapabilityChangedListener? = null

        /**
         * Some devices don't have access to Wearable API via Play Services, so it is necessary to
         * check for this scenario first before trying to use the API.
         */
        val apiAvailable = WearableApiAvailability.isAvailable(nodeClient)
        if (apiAvailable) {
            val allDevices = nodeClient.connectedNodes.await().toSet()
            val reachableCapability =
                capabilityClient.getCapability(
                    ANDROIDIFY_INSTALLED_WEAR,
                    CapabilityClient.FILTER_REACHABLE,
                )
                    .await()

            val installedDevices = reachableCapability.nodes.toSet()
            trySend(selectConnectedDevice(installedDevices, allDevices))

            capabilityListener =
                CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
                    val installedDevicesUpdated = capabilityInfo.nodes.toSet()

                    trySend(selectConnectedDevice(installedDevicesUpdated, allDevices))
                }
            capabilityClient.addListener(capabilityListener, ANDROIDIFY_INSTALLED_WEAR)
        } else {
            trySend(null)
        }
        awaitClose {
            capabilityListener?.let {
                capabilityClient.removeListener(it)
            }
        }
    }

    /**
     * Selects a [com.android.developers.androidify.wear.common.ConnectedWatch] if one is available, prioritizing devices with Androidify.
     * Returns null where no device at all is available.
     */
    private fun selectConnectedDevice(
        installedDevices: Set<Node>,
        allDevices: Set<Node>,
    ): ConnectedWatch? {
        return if (installedDevices.isNotEmpty()) {
            ConnectedWatch(
                nodeId = installedDevices.first().id,
                displayName = installedDevices.first().displayName,
                hasAndroidify = true,
            )
        } else if (allDevices.isNotEmpty()) {
            ConnectedWatch(
                nodeId = allDevices.first().id,
                displayName = allDevices.first().displayName,
                hasAndroidify = false,
            )
        } else {
            null
        }
    }
}
