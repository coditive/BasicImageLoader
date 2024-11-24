package com.syrous.imageloadinglib.screen.imageloader

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.syrous.imageloadinglib.data.response.Photo
import com.syrous.imageloadinglib.imageLoader.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun ImageLoaderScreen(modifier: Modifier = Modifier, photoList: List<Photo>, imageLoader: ImageLoader) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photoList.size) { index ->
            GridItem(photo = photoList[index], imageLoader)
        }
    }
}

@Composable
fun GridItem(photo: Photo, imageLoader: ImageLoader) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Makes the item a square
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val bitmap = remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(photo.url) {
            withContext(Dispatchers.IO) {
                bitmap.value = imageLoader.load(photo.url.regular).get(photo.url.regular)
            }
        }

        bitmap.value?.asImageBitmap()?.let { Image(it, contentDescription = null) }

    }
}