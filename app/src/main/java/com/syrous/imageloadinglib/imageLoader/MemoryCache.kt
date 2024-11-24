package com.syrous.imageloadinglib.imageLoader

import android.graphics.Bitmap
import androidx.collection.LruCache

class MemoryCache(maxSize: Int) {

    private val cache = LruCache<String, Bitmap>(maxSize)

    fun get(key: String): Bitmap? {
        return cache[key]
    }

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun clear() {
        cache.evictAll()
    }
}