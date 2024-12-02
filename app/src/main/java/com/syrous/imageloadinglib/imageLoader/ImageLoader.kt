package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap

interface ImageLoader {

    fun with(context: Context): ImageLoader

    suspend fun load(url: String): ImageLoader

    suspend fun getAsync(url: String): Bitmap?
}