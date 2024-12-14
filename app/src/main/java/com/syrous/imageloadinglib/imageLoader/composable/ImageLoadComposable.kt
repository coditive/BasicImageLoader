package com.syrous.imageloadinglib.imageLoader.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.syrous.imageloadinglib.data.response.Photo
import com.syrous.imageloadinglib.imageLoader.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun ImageLoad(modifier: Modifier = Modifier, photo: Photo, imageLoader: ImageLoader) {
    val context = LocalContext.current
    var imageSize by remember { mutableStateOf<IntSize?>(null) }

    Box(modifier = modifier.onSizeChanged { size ->
        imageSize = size
    }) {
        val bitmap = remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(photo.url) {
            withContext(Dispatchers.IO) {
                imageSize?.let {
                    bitmap.value = imageLoader.resolveSize(it).load(photo.url.regular)
                        .getAsync(photo.url.regular)
                }
            }
        }

        bitmap.value?.asImageBitmap()?.let { Image(it, contentDescription = null) }
    }
}