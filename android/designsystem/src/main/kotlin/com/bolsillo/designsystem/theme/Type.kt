package com.bolsillo.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Sizes/weights come from shared-assets/design/tokens.json → typography.role.
// Money roles set fontFeatureSettings = "tnum" for tabular figures (§1.3).
// Plus Jakarta Sans is the brand family; binary font files are intentionally
// not bundled in the repo — drop weights 400/500/600/700/800 into
// :designsystem/src/main/res/font/ and swap FontFamily.Default below for a
// FontFamily(...) reference once the assets are checked in.

private val Brand: FontFamily = FontFamily.Default

private fun money(
    size: Float,
    height: Float,
    weight: FontWeight,
    letterSpacingEm: Float = 0f,
): TextStyle =
    TextStyle(
        fontFamily = Brand,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = height.sp,
        letterSpacing = letterSpacingEm.em,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

private fun text(
    size: Float,
    height: Float,
    weight: FontWeight,
    letterSpacingEm: Float = 0f,
): TextStyle =
    TextStyle(
        fontFamily = Brand,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = height.sp,
        letterSpacing = letterSpacingEm.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

@Immutable
data class BolsilloTypography(
    val displayBalance: TextStyle,
    val displayAmount: TextStyle,
    val moneyXL: TextStyle,
    val moneyL: TextStyle,
    val keypadDigit: TextStyle,
    val titleXL: TextStyle,
    val titleL: TextStyle,
    val titleM: TextStyle,
    val button: TextStyle,
    val bodyStrong: TextStyle,
    val amountRow: TextStyle,
    val body: TextStyle,
    val bodyValue: TextStyle,
    val label: TextStyle,
    val labelSmall: TextStyle,
    val caption: TextStyle,
    val badge: TextStyle,
    val navLabel: TextStyle,
    val overline: TextStyle,
)

val DefaultBolsilloTypography =
    BolsilloTypography(
        displayBalance = money(46f, 50f, FontWeight.W800, letterSpacingEm = -0.02f),
        displayAmount = money(52f, 56f, FontWeight.W800, letterSpacingEm = -0.03f),
        moneyXL = money(27f, 32f, FontWeight.W800),
        moneyL = money(26f, 30f, FontWeight.W800),
        keypadDigit = text(23f, 28f, FontWeight.W700),
        titleXL = text(21f, 26f, FontWeight.W800),
        titleL = text(18f, 24f, FontWeight.W800),
        titleM = text(17f, 22f, FontWeight.W700),
        button = text(16f, 20f, FontWeight.W800),
        bodyStrong = text(15.5f, 20f, FontWeight.W700),
        amountRow = money(15.5f, 20f, FontWeight.W800),
        body = text(14.5f, 20f, FontWeight.W600),
        bodyValue = text(14.5f, 20f, FontWeight.W700),
        label = text(13f, 16f, FontWeight.W600),
        labelSmall = text(12.5f, 16f, FontWeight.W600),
        caption = text(12f, 15f, FontWeight.W500),
        badge = text(11.5f, 14f, FontWeight.W700),
        navLabel = text(11f, 13f, FontWeight.W500),
        overline = text(12.5f, 16f, FontWeight.W700, letterSpacingEm = 0.04f),
    )

internal fun BolsilloTypography.toMaterialTypography(): Typography =
    Typography(
        headlineLarge = displayBalance,
        titleLarge = titleXL,
        titleMedium = titleM,
        labelLarge = button,
        bodyLarge = body,
        bodyMedium = label,
        labelSmall = badge,
    )

val LocalBolsilloTypography = staticCompositionLocalOf { DefaultBolsilloTypography }
