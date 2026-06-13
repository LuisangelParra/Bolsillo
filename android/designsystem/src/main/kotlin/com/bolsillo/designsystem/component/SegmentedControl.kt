package com.bolsillo.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme

@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(BolsilloTheme.shapes.control)
                .background(BolsilloTheme.colors.track)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(BolsilloTheme.shapes.control)
                        .let { m ->
                            if (isSelected) {
                                m.shadow(BolsilloTheme.elevation.segmentedThumb, BolsilloTheme.shapes.control)
                                    .background(BolsilloTheme.colors.surface)
                            } else {
                                m
                            }
                        }
                        .clickable(role = Role.Tab) { onSelected(option) }
                        .semantics {
                            this.role = Role.Tab
                            this.selected = isSelected
                        }
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(option),
                    color = if (isSelected) BolsilloTheme.colors.textPrimary else BolsilloTheme.colors.textMuted,
                    style = BolsilloTheme.typography.label,
                )
            }
        }
    }
}
