package com.syrous.imageloadinglib.screen.imageloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syrous.imageloadinglib.MainViewModel
import com.syrous.imageloadinglib.imageLoader.ImageLoader
import com.syrous.imageloadinglib.ui.theme.ImageLoadingLibTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ImageLoaderActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageLoadingLibTheme {
                val photoList = viewModel.photoListStream.collectAsState()
                imageLoader = imageLoader.with(LocalContext.current)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageLoaderScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        photoList.value,
                        imageLoader
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.getPhotoList()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}