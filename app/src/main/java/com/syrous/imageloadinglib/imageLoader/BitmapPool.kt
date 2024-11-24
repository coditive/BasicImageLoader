package com.syrous.imageloadinglib.imageLoader

import android.graphics.Bitmap
import java.util.LinkedList

class BitmapPool(private val maxSize: Int) {
    private val pool: LinkedList<Bitmap> = LinkedList()

    fun getBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        synchronized(pool) {
            val iterator = pool.iterator()
            while (iterator.hasNext()) {
                val bitmap = iterator.next()
                if (bitmap.width == width && bitmap.height == height && bitmap.config == config) {
                    iterator.remove()
                    return bitmap
                }
            }
        }
        return Bitmap.createBitmap(width, height, config)
    }

    fun putBitmap(bitmap: Bitmap) {
        synchronized(pool) {
            if(pool.size < maxSize) {
                pool.add(bitmap)
            } else {
                pool.removeFirst().recycle()
                pool.add(bitmap)
            }
        }
    }

    fun clear() {
        synchronized(pool) {
            pool.forEach { it.recycle() }
            pool.clear()
        }
    }
}