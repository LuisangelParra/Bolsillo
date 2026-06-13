package com.bolsillo.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Compose's Modifier.shadow takes a single elevation Dp. We approximate the
// token shadow set (offset/blur/alpha) by mapping each role to a single Dp
// elevation that visually lands close on the reference frame — the design
// source uses CSS shadows, which don't translate 1:1.
@Immutable
data class BolsilloElevation(
    val e1: Dp = 3.dp,
    val e2: Dp = 4.dp,
    val e2b: Dp = 5.dp,
    val e3: Dp = 6.dp,
    val e4: Dp = 8.dp,
    val nav: Dp = 10.dp,
    val key: Dp = 3.dp,
    val fab: Dp = 12.dp,
    val buttonPrimary: Dp = 10.dp,
    val segmentedThumb: Dp = 2.dp,
    val toast: Dp = 12.dp,
)

val DefaultBolsilloElevation = BolsilloElevation()

val LocalBolsilloElevation = staticCompositionLocalOf { DefaultBolsilloElevation }
