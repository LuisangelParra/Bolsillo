package com.bolsillo.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme

@Composable
fun CategoryIconTile(
    foreground: Color,
    container: Color,
    glyph: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(BolsilloTheme.shapes.iconTileSm)
                .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, color = foreground, style = BolsilloTheme.typography.label)
    }
}
