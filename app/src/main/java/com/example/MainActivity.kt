package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.ui.CreatorViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.views.CreatorHubApp
import coil.ImageLoader
import coil.Coil
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  private val viewModel: CreatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "MainActivity.onCreate called successfully.")

        try {
            MobileAds.initialize(this) {}
            Log.d("MainActivity", "AdMob initialized successfully!")
        } catch (e: Throwable) {
            Log.d("MainActivity", "AdMob SDK initialization failed", e)
        }

        // Configure Coil with long timeouts to support slow AI image generation networks
    val imageLoader = ImageLoader.Builder(this)
      .okHttpClient {
        OkHttpClient.Builder()
          .connectTimeout(60, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS)
          .writeTimeout(60, TimeUnit.SECONDS)
          .build()
      }
      .build()
    Coil.setImageLoader(imageLoader)

    enableEdgeToEdge()
    setContent {
      val userStatsState = viewModel.userStats.collectAsState(initial = null)
      val themeSetting = userStatsState.value?.theme ?: "System"

      MyApplicationTheme(themeSetting = themeSetting) {
        CreatorHubApp(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

