package com.bolsillo.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme

@Composable
fun Pill(
    text: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
    border: Color? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .clip(BolsilloTheme.shapes.full)
                .background(background)
                .let { m -> if (border != null) m.border(1.5.dp, border, BolsilloTheme.shapes.full) else m }
                .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading?.invoke()
        Text(text = text, color = foreground, style = BolsilloTheme.typography.badge)
    }
}
