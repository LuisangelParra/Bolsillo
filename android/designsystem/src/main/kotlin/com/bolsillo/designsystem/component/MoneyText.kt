package com.bolsillo.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.bolsillo.designsystem.theme.BolsilloTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Single money renderer used everywhere a monetary amount is displayed.
 *
 * Takes PRIMITIVES (not the domain `Money` value class) so :designsystem stays
 * dependency-free of :domain (Article VIII / parity with iOS).
 *
 * - `minorUnits` is the signed amount; negative renders as `−$X`, positive as `$X`.
 * - `decimalDigits` comes from `Currency.decimalDigits` (USD=2, COP=2, ...).
 * - Locale drives the grouping/decimal separator. The number formatter NEVER
 *   touches `Double` — integer math splits into integer/fraction parts.
 * - `tabular figures` are guaranteed by the typography style (fontFeatureSettings).
 */
@Composable
fun MoneyText(
    minorUnits: Long,
    decimalDigits: Int,
    symbol: String,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    style: TextStyle = BolsilloTheme.typography.amountRow,
    forceSign: Boolean = false,
    color: Color = signColor(minorUnits, forceSign),
) {
    Text(
        text = formatMoney(minorUnits, decimalDigits, symbol, locale, forceSign),
        modifier = modifier,
        style = style,
        color = color,
    )
}

@Composable
private fun signColor(
    minorUnits: Long,
    forceSign: Boolean,
): Color {
    val colors = BolsilloTheme.colors
    return when {
        minorUnits < 0L -> colors.amountNegative
        minorUnits > 0L && forceSign -> colors.amountPositive
        else -> colors.textPrimary
    }
}

internal fun formatMoney(
    minorUnits: Long,
    decimalDigits: Int,
    symbol: String,
    locale: Locale,
    forceSign: Boolean,
): String {
    val magnitude = abs(minorUnits)
    val divisor = pow10(decimalDigits)
    val whole = magnitude / divisor
    val fraction = magnitude % divisor
    val nf = NumberFormat.getIntegerInstance(locale)
    val wholeStr = nf.format(whole)
    val decimalSep = decimalSeparator(locale)
    val body =
        if (decimalDigits == 0) {
            wholeStr
        } else {
            val fractionStr = fraction.toString().padStart(decimalDigits, '0')
            "$wholeStr$decimalSep$fractionStr"
        }
    val prefix =
        when {
            minorUnits < 0L -> "−$symbol"
            minorUnits > 0L && forceSign -> "+$symbol"
            else -> symbol
        }
    return "$prefix$body"
}

private fun pow10(digits: Int): Long {
    var v = 1L
    repeat(digits) { v *= 10L }
    return v
}

private fun decimalSeparator(locale: Locale): String {
    val sym =
        (NumberFormat.getInstance(locale) as? java.text.DecimalFormat)
            ?.decimalFormatSymbols?.decimalSeparator ?: '.'
    return sym.toString()
}
