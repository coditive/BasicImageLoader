package com.syrous.imageloadinglib.screen.coil

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
import com.syrous.imageloadinglib.MainViewModel
import com.syrous.imageloadinglib.ui.theme.ImageLoadingLibTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CoilActivity: ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageLoadingLibTheme {
                val photoList = viewModel.photoListStream.collectAsState()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CoilLoaderScreen(Modifier.fillMaxSize().padding(innerPadding), photoList.value)
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