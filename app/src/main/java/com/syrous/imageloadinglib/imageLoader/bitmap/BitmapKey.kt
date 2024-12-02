package com.syrous.imageloadinglib.imageLoader.bitmap

import android.graphics.Bitmap

data class BitmapKey (
    val width: Int,
    val height: Int,
    val config: Bitmap.Config
)