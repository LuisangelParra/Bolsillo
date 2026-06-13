package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R

/**
 * Custom 3×4 integer keypad (1–9, 000, 0, ⌫). The OS soft keyboard is never
 * shown (FR1/§2.1) — keypad is up the moment the screen opens.
 */
@Composable
fun AmountKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows =
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("000", "0", "⌫"),
        )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HintStrip()
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    KeypadKey(
                        label = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when {
                                key == "⌫" -> onBackspace()
                                key == "000" -> {
                                    onDigit('0')
                                    onDigit('0')
                                    onDigit('0')
                                }
                                else -> onDigit(key[0])
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HintStrip() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(BolsilloTheme.shapes.control)
                .background(BolsilloTheme.colors.primaryContainer)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.record_keypad_hint),
            color = BolsilloTheme.colors.onPrimaryContainer,
            style = BolsilloTheme.typography.label,
        )
    }
}

@Composable
private fun KeypadKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .height(56.dp)
                .shadow(BolsilloTheme.elevation.key, BolsilloTheme.shapes.control)
                .clip(BolsilloTheme.shapes.control)
                .background(BolsilloTheme.colors.surface)
                .clickable(onClick = onClick)
                .semantics { contentDescription = "keypadKey:$label" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = BolsilloTheme.colors.textPrimary,
            style = BolsilloTheme.typography.keypadDigit,
        )
    }
}
