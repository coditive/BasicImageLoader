package com.syrous.imageloadinglib.imageLoader.bitmap

import android.graphics.Bitmap

interface BitmapPool {

    fun getCurrentMaxSize(): Int

    fun getBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap

    fun getBitmap(key: BitmapKey): Bitmap

    fun putBitmap(bitmap: Bitmap)

    fun addEvictObserver(observer: (BitmapKey) -> Unit)

    fun removeEvictObserver(observer: (BitmapKey) -> Unit)

}