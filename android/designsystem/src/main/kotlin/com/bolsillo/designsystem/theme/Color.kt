package com.bolsillo.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// All literal hex values come from shared-assets/design/tokens.json.
// Composables MUST NOT use literal colors; read via BolsilloTheme.colors.* /
// MaterialTheme.colorScheme.* (Article VI — visual parity).

@Immutable
data class BolsilloColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceInverse: Color,
    val onSurfaceInverse: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color,
    val primaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val outline: Color,
    val divider: Color,
    val track: Color,
    val fill: Color,
    val sheetHandle: Color,
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val warningBorder: Color,
    val onWarningContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val amountPositive: Color,
    val amountNegative: Color,
    val notificationDot: Color,
    val caret: Color,
    val isDark: Boolean,
)

val LightBolsilloColors =
    BolsilloColors(
        background = Color(0xFFF4F3F6),
        surface = Color(0xFFFFFFFF),
        surfaceAlt = Color(0xFFF7F6FA),
        surfaceInverse = Color(0xFF16161B),
        onSurfaceInverse = Color(0xFFFFFFFF),
        primary = Color(0xFF7C5CF0),
        primaryContainer = Color(0xFFEDE7FE),
        onPrimary = Color(0xFFFFFFFF),
        onPrimaryContainer = Color(0xFF6B4FD8),
        primaryAccent = Color(0xFFB79CFF),
        textPrimary = Color(0xFF16161C),
        textSecondary = Color(0xFF56565F),
        textMuted = Color(0xFF9A9AA4),
        textDisabled = Color(0xFFC4C3CC),
        outline = Color(0xFFE7E6EC),
        divider = Color(0xFFF2F1F5),
        track = Color(0xFFF0EFF4),
        fill = Color(0xFFE7E6EC),
        sheetHandle = Color(0xFFD6D5DC),
        success = Color(0xFF16B364),
        successContainer = Color(0xFFDCF5E9),
        onSuccessContainer = Color(0xFF0E8049),
        danger = Color(0xFFF0425A),
        dangerContainer = Color(0xFFFDE6EC),
        onDangerContainer = Color(0xFFC32A40),
        warning = Color(0xFFE0852B),
        warningContainer = Color(0xFFFBEFD6),
        warningBorder = Color(0xFFF0DBB0),
        onWarningContainer = Color(0xFFB36514),
        info = Color(0xFF3B82F6),
        infoContainer = Color(0xFFE3EFFE),
        amountPositive = Color(0xFF16B364),
        amountNegative = Color(0xFFF0425A),
        notificationDot = Color(0xFFFF5470),
        caret = Color(0xFF7C5CF0),
        isDark = false,
    )

val DarkBolsilloColors =
    BolsilloColors(
        background = Color(0xFF121016),
        surface = Color(0xFF1C1A22),
        surfaceAlt = Color(0xFF242230),
        surfaceInverse = Color(0xFF2A2833),
        onSurfaceInverse = Color(0xFFFFFFFF),
        primary = Color(0xFF9B7BF5),
        primaryContainer = Color(0xFF2E2748),
        onPrimary = Color(0xFFFFFFFF),
        onPrimaryContainer = Color(0xFFCDBEFF),
        primaryAccent = Color(0xFFB79CFF),
        textPrimary = Color(0xFFF2F1F5),
        textSecondary = Color(0xFFB6B5C0),
        textMuted = Color(0xFF82808C),
        textDisabled = Color(0xFF5A5862),
        outline = Color(0xFF2F2D38),
        divider = Color(0xFF26242E),
        track = Color(0xFF2A2833),
        fill = Color(0xFF2F2D38),
        sheetHandle = Color(0xFF3A3844),
        success = Color(0xFF2FD17E),
        successContainer = Color(0xFF123226),
        onSuccessContainer = Color(0xFF8DEBBC),
        danger = Color(0xFFFF6378),
        dangerContainer = Color(0xFF3A1620),
        onDangerContainer = Color(0xFFFFB3BF),
        warning = Color(0xFFF0A94E),
        warningContainer = Color(0xFF3A2E14),
        warningBorder = Color(0xFF5C4A1E),
        onWarningContainer = Color(0xFFFAD79A),
        info = Color(0xFF5C9CFF),
        infoContainer = Color(0xFF14233A),
        amountPositive = Color(0xFF2FD17E),
        amountNegative = Color(0xFFFF6378),
        notificationDot = Color(0xFFFF5470),
        caret = Color(0xFF9B7BF5),
        isDark = true,
    )

internal fun BolsilloColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = track,
        onSurfaceVariant = textSecondary,
        outline = outline,
        error = danger,
        onError = onPrimary,
        errorContainer = dangerContainer,
        onErrorContainer = onDangerContainer,
    )
}

object BolsilloGradients {
    val Hero =
        Brush.linearGradient(
            listOf(Color(0xFFA98CF5), Color(0xFF8862EE), Color(0xFF6E45DF)),
        )
    val Primary =
        Brush.linearGradient(
            listOf(Color(0xFF9B7BF5), Color(0xFF7C5CF0)),
        )
    val Fab =
        Brush.linearGradient(
            listOf(Color(0xFF9B7BF5), Color(0xFF7C5CF0)),
        )
    val Avatar =
        Brush.linearGradient(
            listOf(Color(0xFFFFD9A8), Color(0xFFF2A65A)),
        )
}

internal fun blendOverSurface(
    surface: Color,
    accent: Color,
    alpha: Float,
): Color = lerp(surface, accent, alpha)

val LocalBolsilloColors = staticCompositionLocalOf { LightBolsilloColors }
