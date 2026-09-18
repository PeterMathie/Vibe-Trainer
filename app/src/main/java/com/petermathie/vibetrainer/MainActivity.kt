package com.petermathie.vibetrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.petermathie.vibetrainer.ui.VibeTrainerApp
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibeTrainerTheme {
                VibeTrainerApp()
            }
        }
    }
}
