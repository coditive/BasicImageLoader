package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.StateFlow

interface ImageLoader {

    fun with(context: Context): ImageLoader

    fun load(url: String): ImageLoader

    fun resolveSize(size: IntSize): ImageLoader

    val apiCallCountState: StateFlow<Int>

    suspend fun getImageAsync(): Bitmap?
}