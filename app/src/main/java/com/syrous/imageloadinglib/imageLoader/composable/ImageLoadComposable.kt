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


/**
 *  There is also flaw in this implementation as well
 *  1. It triggers the api request after url change which is on the user to handle correctly (Users should be tasked with
 *  handling state of image loader)
 *
 *
 *  Solution -
 *  If we use compose inbuilt mechanism which removes element from UI tree, we can invalidate the call
 *
 */

@Composable
fun ImageLoad(modifier: Modifier = Modifier, photo: Photo, imageLoader: ImageLoader) {
    val context = LocalContext.current
    var imageSize by remember { mutableStateOf<IntSize?>(null) }

    Box(modifier = modifier.onSizeChanged { size ->
        imageSize = size
    }) {
        val bitmap = remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(photo.url) {
            imageSize?.let { size ->
                val image = imageLoader.with(context).resolveSize(size).load(photo.url.regular)
                    .getImageAsync()
                bitmap.value = image
            }
        }

        bitmap.value?.asImageBitmap()?.let { Image(it, contentDescription = null) }
    }
}