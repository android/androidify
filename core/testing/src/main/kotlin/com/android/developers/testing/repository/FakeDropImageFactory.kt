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
package com.android.developers.testing.repository

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import com.android.developers.androidify.data.DropBehaviourFactory

class FakeDropImageFactory : DropBehaviourFactory {
    override fun shouldStartDragAndDrop(event: DragAndDropEvent): Boolean = true

    override fun createTargetCallback(
        activity: ComponentActivity,
        onImageDropped: (Uri) -> Unit,
        onDropStarted: () -> Unit,
        onDropEnded: () -> Unit,
    ): DragAndDropTarget = object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            return false
        }
    }
}
