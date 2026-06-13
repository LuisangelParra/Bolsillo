package com.bolsillo.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing scale from shared-assets/design/tokens.json → spacing.
@Immutable
data class BolsilloSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val s: Dp = 6.dp,
    val sm: Dp = 8.dp,
    val m: Dp = 10.dp,
    val base: Dp = 12.dp,
    val ml: Dp = 14.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 18.dp,
    val xxl: Dp = 20.dp,
    val xxxl: Dp = 22.dp,
    val xxxxl: Dp = 24.dp,
    val xxxxxl: Dp = 26.dp,
    val xxxxxxl: Dp = 30.dp,
    val screenPaddingX: Dp = 18.dp,
    val sheetPaddingX: Dp = 20.dp,
)

val DefaultBolsilloSpacing = BolsilloSpacing()

val LocalBolsilloSpacing = staticCompositionLocalOf { DefaultBolsilloSpacing }
