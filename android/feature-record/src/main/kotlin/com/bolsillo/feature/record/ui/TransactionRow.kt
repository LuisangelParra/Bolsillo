package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.component.CategoryIconTile
import com.bolsillo.designsystem.component.MoneyText
import com.bolsillo.designsystem.component.Pill
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R

/**
 * Single transaction row used by the home feed (E3) and the trash list (US3).
 * Renders "Por confirmar" pill when [toConfirm] (§2.2).
 */
@Composable
fun TransactionRow(
    categoryId: String?,
    title: String,
    minorUnits: Long,
    decimalDigits: Int,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    toConfirm: Boolean = false,
) {
    val dark = BolsilloTheme.colors.isDark
    val palette = CategoryColorResolver.color(categoryId, dark)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(BolsilloTheme.elevation.e2, BolsilloTheme.shapes.card)
                .clip(BolsilloTheme.shapes.card)
                .background(BolsilloTheme.colors.surface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CategoryIconTile(foreground = palette.foreground, container = palette.container, glyph = " ")
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = BolsilloTheme.colors.textPrimary,
                style = BolsilloTheme.typography.bodyStrong,
            )
            if (toConfirm) {
                Pill(
                    text = stringResource(R.string.record_toConfirm),
                    background = BolsilloTheme.colors.warningContainer,
                    foreground = BolsilloTheme.colors.warning,
                )
            }
        }
        MoneyText(
            minorUnits = minorUnits,
            decimalDigits = decimalDigits,
            symbol = currencySymbol,
            forceSign = minorUnits > 0L,
        )
    }
}
