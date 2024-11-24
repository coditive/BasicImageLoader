package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap

interface ImageLoader {

    fun with(context: Context): ImageLoader

    suspend fun load(url: String): ImageLoader

    fun get(url: String): Bitmap?
}