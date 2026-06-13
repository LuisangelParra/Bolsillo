package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R
import com.bolsillo.feature.record.presentation.RecordMode

/**
 * Texto / Recibo modes ship in spec 007 (§3). They render disabled here so
 * the layout matches the canonical design.
 */
@Composable
fun ModeTabs(
    selected: RecordMode,
    modifier: Modifier = Modifier,
) {
    val labels =
        mapOf(
            RecordMode.Keypad to stringResource(R.string.record_mode_keypad),
            RecordMode.Text to stringResource(R.string.record_mode_text),
            RecordMode.Receipt to stringResource(R.string.record_mode_receipt),
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(BolsilloTheme.shapes.full)
                .background(BolsilloTheme.colors.track)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RecordMode.entries.forEach { mode ->
            val active = mode == selected
            val color =
                when {
                    mode != RecordMode.Keypad -> BolsilloTheme.colors.textDisabled
                    active -> BolsilloTheme.colors.textPrimary
                    else -> BolsilloTheme.colors.textMuted
                }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(BolsilloTheme.shapes.full)
                        .background(
                            if (active) {
                                BolsilloTheme.colors.surface
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            },
                        )
                        .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(labels[mode]!!, color = color, style = BolsilloTheme.typography.label)
            }
        }
    }
}
