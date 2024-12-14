package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize

interface ImageLoader {

    fun with(context: Context): ImageLoader

    suspend fun load(url: String): ImageLoader

    fun resolveSize(size: IntSize): ImageLoader

    suspend fun getAsync(url: String): Bitmap?
}