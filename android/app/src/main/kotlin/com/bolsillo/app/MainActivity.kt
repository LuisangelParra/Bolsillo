package com.bolsillo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.ui.RecordRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * The app opens straight into the record surface (FR1) — no home, no nav.
 * Full-screen presentation is decided in spec.md and binding on both platforms.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BolsilloTheme {
                RecordRoute()
            }
        }
    }
}
