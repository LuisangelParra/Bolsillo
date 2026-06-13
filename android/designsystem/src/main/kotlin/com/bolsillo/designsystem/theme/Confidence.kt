package com.bolsillo.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Confidence buckets mirror shared-assets/design/tokens.json → confidence.
// The threshold default 0.75 lives in domain (ExpenseClassifier.DEFAULT_THRESHOLD,
// Article V); this module accepts it as a parameter so :designsystem stays
// dependency-free of :domain.

enum class ConfidenceState { Waiting, Low, High }

@Immutable
data class ConfidenceVisual(
    val state: ConfidenceState,
    val foreground: Color,
    val container: Color,
    val chipBorder: Color?,
    val labelKey: String,
)

fun confidenceVisual(
    confidence: Double,
    threshold: Double,
    colors: BolsilloColors,
): ConfidenceVisual =
    when {
        confidence <= 0.0 ->
            ConfidenceVisual(
                state = ConfidenceState.Waiting,
                foreground = colors.textMuted,
                container = colors.fill,
                chipBorder = null,
                labelKey = "record.ai.waiting",
            )
        confidence < threshold ->
            ConfidenceVisual(
                state = ConfidenceState.Low,
                foreground = colors.warning,
                container = colors.warningContainer,
                chipBorder = colors.warningBorder,
                labelKey = "record.ai.toConfirm",
            )
        else ->
            ConfidenceVisual(
                state = ConfidenceState.High,
                foreground = colors.success,
                container = colors.successContainer,
                chipBorder = null,
                labelKey = "record.ai.confident",
            )
    }
