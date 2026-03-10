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
package com.android.developers.androidify.data

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.lifecycle.lifecycleScope
import com.android.developers.androidify.util.LocalFileProvider
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

interface DropBehaviourFactory {
    fun shouldStartDragAndDrop(event: DragAndDropEvent): Boolean
    fun createTargetCallback(
        activity: ComponentActivity,
        onImageDropped: (Uri) -> Unit,
        onDropStarted: () -> Unit = {},
        onDropEnded: () -> Unit = {},
    ): DragAndDropTarget
}

class DropBehaviourFactoryImpl @Inject constructor(val localFileProvider: LocalFileProvider) :
    DropBehaviourFactory {

    override fun shouldStartDragAndDrop(event: DragAndDropEvent): Boolean =
        event.mimeTypes().contains("image/jpeg")

    override fun createTargetCallback(
        activity: ComponentActivity,
        onImageDropped: (Uri) -> Unit,
        onDropStarted: () -> Unit,
        onDropEnded: () -> Unit,
    ) =
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                onDropStarted()
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                onDropEnded()
            }

            /**
             * Dropping an image requires the app to obtain the permission to use the image being
             * dropped. This permission only lasts until the event is completed. The easiest way
             * of being able to display the image being dropped is to temporarily copy it inside
             * the app storage and use that copy for the processing.
             */
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val targetEvent = event.toAndroidDragEvent()

                if (targetEvent.clipData.itemCount == 0) {
                    return false
                }

                activity.lifecycleScope.launch {
                    val permission = activity.requestDragAndDropPermissions(targetEvent)
                    if (permission != null) {
                        try {
                            val inputUri = targetEvent.clipData.getItemAt(0).uri
                            activity.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                bitmap?.let {
                                    val cacheFile =
                                        localFileProvider.createCacheFile("dropped_image_${UUID.randomUUID()}.jpg")
                                    localFileProvider.saveBitmapToFile(bitmap, cacheFile)
                                    onImageDropped(localFileProvider.sharingUriForFile(cacheFile))
                                }
                            }
                        } finally {
                            permission.release()
                        }
                    }
                }
                return true
            }
        }
}
