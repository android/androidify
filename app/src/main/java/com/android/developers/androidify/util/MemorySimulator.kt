package com.android.developers.androidify.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID

object MemorySimulator {
    private val javaHeapLeaker = mutableListOf<String>()
    private val bitmapPool = mutableListOf<Bitmap>()
    private val directBuffers = mutableListOf<ByteBuffer>()
    private val mappedBuffers = mutableListOf<MappedByteBuffer>()
    private val webViews = mutableListOf<WebView>()
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Toggles massive Java Heap string allocations.
     * Excellent for forcing the OS to swap compressed pages to zRAM (Swap).
     */
    fun startJavaHeapSwapBloat(targetMegaBytes: Int, intervalMs: Long = 100) {
        val bytesPerAlloc = 1024 * 1024 // 1 MB increments
        val totalLoops = targetMegaBytes

        var allocated = 0
        mainHandler.post(object : Runnable {
            override fun run() {
                if (allocated < totalLoops) {
                    val stringChunk = StringBuilder().apply {
                        repeat(bytesPerAlloc / 2) { append(UUID.randomUUID().toString().take(2)) }
                    }.toString()
                    
                    javaHeapLeaker.add(stringChunk)
                    allocated++
                    mainHandler.postDelayed(this, intervalMs)
                }
            }
        })
    }

    /**
     * Toggles Native Bitmap Allocation (Anon RSS).
     * Directly inflates the private dirty native heap without exhausting JVM heap limits.
     */
    fun startNativeBitmapBloat(targetMegaBytes: Int, intervalMs: Long = 200) {
        val width = 512
        val height = 512
        var allocated = 0

        mainHandler.post(object : Runnable {
            override fun run() {
                if (allocated < targetMegaBytes) {
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.RED)
                    bitmapPool.add(bitmap)
                    allocated++
                    mainHandler.postDelayed(this, intervalMs)
                }
            }
        })
    }

    /**
     * Toggles Direct Native Allocations (C/C++ malloc equivalent).
     * Bypasses the JVM heap completely and allocates raw native memory.
     */
    fun startDirectNativeBloat(targetMegaBytes: Int, intervalMs: Long = 100) {
        var allocated = 0
        mainHandler.post(object : Runnable {
            override fun run() {
                if (allocated < targetMegaBytes) {
                    // Allocate 1MB of direct native memory
                    val buffer = ByteBuffer.allocateDirect(1024 * 1024)
                    // Touch the memory so the OS maps it to physical RAM
                    for (i in 0 until buffer.capacity() step 4096) {
                        buffer.put(i, 1)
                    }
                    directBuffers.add(buffer)
                    allocated++
                    mainHandler.postDelayed(this, intervalMs)
                }
            }
        })
    }

    /**
     * Toggles the WebView "Hidden Tab" trick.
     * Rapidly consumes massive amounts of memory via Chromium rendering processes.
     */
    fun startWebViewBloat(context: Context, targetInstances: Int = 10, intervalMs: Long = 500) {
        var allocated = 0
        mainHandler.post(object : Runnable {
            override fun run() {
                if (allocated < targetInstances) {
                    val webView = WebView(context).apply {
                        settings.javaScriptEnabled = true
                        // Load a heavy WebGL/rendering page (or just a heavy DOM)
                        loadUrl("https://threejs.org/examples/#webgl_animation_keyframes")
                    }
                    webViews.add(webView)
                    allocated++
                    mainHandler.postDelayed(this, intervalMs)
                }
            }
        })
    }

    /**
     * Memory maps massive files to inflate the virtual memory footprint.
     */
    fun startFileMmapBloat(context: Context, targetMegaBytes: Int) {
        Thread {
            try {
                // Create a huge dummy file
                val dummyFile = File(context.cacheDir, "huge_dummy_file.tmp")
                val raf = RandomAccessFile(dummyFile, "rw")
                val targetBytes = targetMegaBytes * 1024L * 1024L
                raf.setLength(targetBytes)
                
                // mmap the file into memory
                val channel = raf.channel
                val mappedBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, targetBytes)
                
                // Touch the bytes to force physical allocation
                for (i in 0 until targetBytes step 4096) {
                    mappedBuffer.put(i.toInt(), 1.toByte())
                }
                
                mappedBuffers.add(mappedBuffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * Clears all simulated allocations to allow the OS to reclaim space.
     */
    fun reset() {
        mainHandler.removeCallbacksAndMessages(null)
        javaHeapLeaker.clear()
        
        bitmapPool.forEach { it.recycle() }
        bitmapPool.clear()
        
        directBuffers.clear()
        mappedBuffers.clear()
        
        webViews.forEach { 
            it.stopLoading()
            it.destroy()
        }
        webViews.clear()
        
        System.gc()
    }
}
