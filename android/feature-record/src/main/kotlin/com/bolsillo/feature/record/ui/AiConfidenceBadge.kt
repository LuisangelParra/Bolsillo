package com.bolsillo.feature.record.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bolsillo.designsystem.component.Pill
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.designsystem.theme.confidenceVisual
import com.bolsillo.feature.record.R

@Composable
fun AiConfidenceBadge(
    confidence: Double,
    threshold: Double,
    modifier: Modifier = Modifier,
) {
    val visual = confidenceVisual(confidence, threshold, BolsilloTheme.colors)
    val labelRes =
        when (visual.labelKey) {
            "record.ai.waiting" -> R.string.record_ai_waiting
            "record.ai.toConfirm" -> R.string.record_ai_toConfirm
            else -> R.string.record_ai_confident
        }
    Pill(
        text = stringResource(labelRes),
        background = visual.container,
        foreground = visual.foreground,
        border = visual.chipBorder,
        modifier = modifier,
    )
}
