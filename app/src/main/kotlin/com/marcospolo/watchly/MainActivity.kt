package com.marcospolo.watchly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.marcospolo.watchly.presentation.navigation.WatchlyNavHost
import com.marcospolo.watchly.presentation.theme.WatchlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WatchlyTheme {
                WatchlyNavHost()
            }
        }
    }
}
