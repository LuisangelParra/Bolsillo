package com.bolsillo.feature.record.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountDisplay(
    digits: String,
    decimalDigits: Int,
    symbol: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = BolsilloTheme.colors
    val isEmpty = digits.isEmpty() || digits == "0"
    val display = formatKeypadDigits(digits, decimalDigits)
    val tint =
        when {
            isEmpty -> colors.textDisabled
            isIncome -> colors.amountPositive
            else -> colors.textPrimary
        }
    val transition = rememberInfiniteTransition(label = "caret")
    val alpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "caret-alpha",
    )

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, color = tint, style = BolsilloTheme.typography.moneyL)
            Text(
                text = display,
                color = tint,
                style = BolsilloTheme.typography.displayAmount,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.padding(start = 4.dp).semantics {
                        contentDescription = "amountDisplay:$display"
                    },
            )
            Box(
                modifier =
                    Modifier
                        .padding(start = 4.dp)
                        .width(3.dp)
                        .height(40.dp)
                        .alpha(alpha)
                        .size(3.dp, 40.dp),
            )
        }
        Text(
            text = stringRes(R.string.record_amount),
            color = colors.textMuted,
            style = BolsilloTheme.typography.caption,
        )
    }
}

@Composable
private fun stringRes(resId: Int): String = androidx.compose.ui.res.stringResource(resId)

internal fun formatKeypadDigits(
    digits: String,
    decimalDigits: Int,
): String {
    if (digits.isEmpty()) return "0"
    val clean = digits.trimStart('0').ifEmpty { "0" }
    if (decimalDigits <= 0) return groupInts(clean, Locale.getDefault())
    val padded = clean.padStart(decimalDigits + 1, '0')
    val whole = padded.dropLast(decimalDigits).trimStart('0').ifEmpty { "0" }
    val fraction = padded.takeLast(decimalDigits)
    val locale = Locale.getDefault()
    val nf = NumberFormat.getInstance(locale) as? java.text.DecimalFormat
    val sep = nf?.decimalFormatSymbols?.decimalSeparator ?: '.'
    return "${groupInts(whole, locale)}$sep$fraction"
}

private fun groupInts(
    value: String,
    locale: Locale,
): String = NumberFormat.getIntegerInstance(locale).format(value.toLong())
