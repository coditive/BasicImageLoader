package com.syrous.imageloadinglib.imageLoader.bitmap

import android.graphics.Bitmap
import android.graphics.Bitmap.Config

class UrlBitmapManager(private val bitmapPool: BitmapPool) {

    private val urlToBitmapMap = mutableMapOf<String, BitmapKey>()

    init {
        bitmapPool.addEvictObserver { evictedKey ->
            urlToBitmapMap.entries.removeIf { it.value == evictedKey }
        }
    }

    fun getBitmapForUrl(url: String, width: Int, height: Int, config: Config): Bitmap {
        val key = urlToBitmapMap[url]
        return if(key != null) {
            bitmapPool.getBitmap(width, height, config)
        } else {
            val bitmap = bitmapPool.getBitmap(width, height, config)
            urlToBitmapMap[url] = BitmapKey(width, height, config)
            bitmap
        }
    }

    fun getBitmapForUrl(url: String): Bitmap {
        return bitmapPool.getBitmap(urlToBitmapMap[url]!!)
    }

    fun putBitmapForUrl(url: String, bitmap: Bitmap) {
        val key = BitmapKey(bitmap.width, bitmap.height, bitmap.config!!)
        bitmapPool.putBitmap(bitmap)
        urlToBitmapMap[url] = key
    }
}