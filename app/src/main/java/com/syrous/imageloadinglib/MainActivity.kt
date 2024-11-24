package com.syrous.imageloadinglib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.syrous.imageloadinglib.screen.coil.CoilActivity
import com.syrous.imageloadinglib.screen.imageloader.ImageLoaderActivity
import com.syrous.imageloadinglib.ui.theme.ImageLoadingLibTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageLoadingLibTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(modifier = Modifier.fillMaxWidth().wrapContentHeight(), onClick = {
                            val intent = Intent(this@MainActivity, CoilActivity::class.java)
                            startActivity(intent)
                        }) {
                            Text("Coil Image Loading")
                        }

                        Button(modifier = Modifier.fillMaxWidth().wrapContentHeight(), onClick = {
                            val intent = Intent(this@MainActivity, ImageLoaderActivity::class.java)
                            startActivity(intent)
                        }) {
                            Text("Scratch Image Loading")
                        }
                    }
                }
            }
        }
    }
}
