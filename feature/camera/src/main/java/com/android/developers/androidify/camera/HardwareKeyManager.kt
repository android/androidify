package com.android.developers.androidify.camera

import android.view.KeyEvent
import java.util.concurrent.CopyOnWriteArrayList

object HardwareKeyManager {
    interface Handler {
        /** Higher number = higher priority */
        val priority: Int get() = 0
        fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = false
        fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean = false
    }

    private val handlers = mutableListOf<Handler>()

    fun register(handler: Handler): AutoCloseable {
        synchronized(handlers) {
            handlers.add(handler)
            handlers.sortByDescending { it.priority }
        }
        return AutoCloseable {
            synchronized(handlers) {
                handlers.remove(handler)
            }
        }
    }

    fun dispatchDown(keyCode: Int, event: KeyEvent): Boolean {
        val handlersCopy = synchronized(handlers) { handlers.toList() }
        return handlersCopy.any { it.onKeyDown(keyCode, event) }
    }

    fun dispatchUp(keyCode: Int, event: KeyEvent): Boolean {
        val handlersCopy = synchronized(handlers) { handlers.toList() }
        return handlersCopy.any { it.onKeyUp(keyCode, event) }
    }
}