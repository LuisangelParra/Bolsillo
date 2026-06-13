package com.bolsillo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bolsillo.app.ui.theme.BolsilloTheme
import com.bolsillo.domain.port.TransactionRepository
import com.bolsillo.feature.record.ui.RecordScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Field injection proves the data → domain wiring resolves through Hilt.
    @Inject
    lateinit var transactionRepository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BolsilloTheme {
                RecordScreen()
            }
        }
    }
}
