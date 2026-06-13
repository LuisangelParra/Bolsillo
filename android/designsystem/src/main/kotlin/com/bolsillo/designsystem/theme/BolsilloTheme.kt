package com.bolsillo.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Bolsillo's Compose theme. Provides Material3 [androidx.compose.material3.MaterialTheme] for
 * standard slots and BolsilloTheme.* CompositionLocals for app-specific roles
 * (extra colors, typography, shapes, spacing, elevation) that M3 doesn't model.
 *
 * Reads light/dark token sets per [isSystemInDarkTheme]; do not hardcode colors
 * or sizes in composables (Article VI — visual parity).
 */
@Composable
fun BolsilloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkBolsilloColors else LightBolsilloColors
    val typography = DefaultBolsilloTypography
    val shapes = DefaultBolsilloShapes

    CompositionLocalProvider(
        LocalBolsilloColors provides colors,
        LocalBolsilloTypography provides typography,
        LocalBolsilloShapes provides shapes,
        LocalBolsilloSpacing provides DefaultBolsilloSpacing,
        LocalBolsilloElevation provides DefaultBolsilloElevation,
        LocalContentColor provides colors.textPrimary,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = typography.toMaterialTypography(),
            shapes = shapes.toMaterialShapes(),
        ) {
            ProvideTextStyle(value = typography.body, content = content)
        }
    }
}

object BolsilloTheme {
    val colors: BolsilloColors
        @Composable @ReadOnlyComposable
        get() = LocalBolsilloColors.current
    val typography: BolsilloTypography
        @Composable @ReadOnlyComposable
        get() = LocalBolsilloTypography.current
    val shapes: BolsilloShapes
        @Composable @ReadOnlyComposable
        get() = LocalBolsilloShapes.current
    val spacing: BolsilloSpacing
        @Composable @ReadOnlyComposable
        get() = LocalBolsilloSpacing.current
    val elevation: BolsilloElevation
        @Composable @ReadOnlyComposable
        get() = LocalBolsilloElevation.current
}
