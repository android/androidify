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