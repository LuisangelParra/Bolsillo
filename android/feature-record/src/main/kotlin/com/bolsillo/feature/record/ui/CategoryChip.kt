package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.component.CategoryIconTile
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R

@Composable
fun CategoryChip(
    categoryId: String?,
    labelKey: String,
    modifier: Modifier = Modifier,
    showLowConfidenceBorder: Boolean = false,
    onClick: () -> Unit = {},
) {
    val dark = BolsilloTheme.colors.isDark
    val palette = CategoryColorResolver.color(categoryId, dark)
    val border: Color? = if (showLowConfidenceBorder) BolsilloTheme.colors.warningBorder else null
    Row(
        modifier =
            modifier
                .clip(BolsilloTheme.shapes.chip)
                .background(BolsilloTheme.colors.surface)
                .let { m -> if (border != null) m.border(1.5.dp, border, BolsilloTheme.shapes.chip) else m }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryIconTile(foreground = palette.foreground, container = palette.container, glyph = " ")
        Column {
            Text(
                text = stringResource(R.string.record_category),
                color = BolsilloTheme.colors.textMuted,
                style = BolsilloTheme.typography.caption,
            )
            Text(
                text = labelKey,
                color = BolsilloTheme.colors.textPrimary,
                style = BolsilloTheme.typography.bodyValue,
            )
        }
    }
}

@Composable
fun AccountChip(
    accountName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .clip(BolsilloTheme.shapes.chip)
                .background(BolsilloTheme.colors.surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryIconTile(
            foreground = BolsilloTheme.colors.primary,
            container = BolsilloTheme.colors.primaryContainer,
            glyph = " ",
        )
        Column {
            Text(
                text = stringResource(R.string.record_account),
                color = BolsilloTheme.colors.textMuted,
                style = BolsilloTheme.typography.caption,
            )
            Text(
                text = accountName,
                color = BolsilloTheme.colors.textPrimary,
                style = BolsilloTheme.typography.bodyValue,
            )
        }
    }
}
