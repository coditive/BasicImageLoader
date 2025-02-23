package com.syrous.imageloadinglib.screen.imageloader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syrous.imageloadinglib.data.response.Photo
import com.syrous.imageloadinglib.imageLoader.ImageLoader
import com.syrous.imageloadinglib.imageLoader.composable.ImageLoad
import kotlinx.coroutines.launch


@Composable
fun ImageLoaderScreen(
    modifier: Modifier = Modifier, photoList: List<Photo>, imageLoader: ImageLoader
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val apiCallCount by remember { imageLoader.apiCallCountState }.collectAsState()
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Api call number = $apiCallCount"
                    )
                }
            }) {
                Text("Get Api Call Count")
            }

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
    }
}

@Composable
fun GridItem(photo: Photo, imageLoader: ImageLoader) {
    ImageLoad(
        modifier = Modifier
            .aspectRatio(1f) // Makes the item a square
            .fillMaxWidth()
            .padding(8.dp), photo = photo, imageLoader = imageLoader
    )
}