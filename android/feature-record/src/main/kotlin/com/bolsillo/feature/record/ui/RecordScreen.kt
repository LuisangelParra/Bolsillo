package com.bolsillo.feature.record.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bolsillo.feature.record.R

/**
 * Placeholder for the fast-expense-recording screen (feature 001). All user-facing
 * text comes from string resources — no hard-coded strings (Constitution Article VII).
 */
@Composable
fun RecordScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.record_title))
            Button(onClick = { /* TODO: wire to RecordViewModel */ }) {
                Text(text = stringResource(R.string.record_save))
            }
        }
    }
}

@Preview
@Composable
private fun RecordScreenPreview() {
    RecordScreen()
}
