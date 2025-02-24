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

    /**
     *  Drawbacks -
     *  1. This method will give image async, it wont cancel the previous call for the images which are made by ui
     *  As user scroll past the image which was getting loaded on the UI
     *  2. It is also making api call, even if image is cached in the memory (Inefficient error handling)
     *
     *  Conclusion -
     *  Glide style api works well with traditional android views as they are tied to the imageview which informs glide about
     *  view invalidation and thus canceling call for image.
     *
     **/
    suspend fun getImageAsync(): Bitmap?
}