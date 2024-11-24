package com.syrous.imageloadinglib.screen.coil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.syrous.imageloadinglib.data.response.Photo

@Composable
fun CoilLoaderScreen(modifier: Modifier = Modifier, photoList: List<Photo>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photoList.size) { index ->
            CoilGridItem(photo = photoList[index])
        }
    }
}


@Composable
fun CoilGridItem(modifier: Modifier = Modifier, photo: Photo) {
    AsyncImage(
        model = photo.url.regular,
        contentDescription = "",
        modifier = modifier
    )
}