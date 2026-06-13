package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloGradients
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R

@Composable
fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    if (enabled) BolsilloTheme.elevation.buttonPrimary else 0.dp,
                    BolsilloTheme.shapes.control,
                )
                .clip(BolsilloTheme.shapes.control)
                .background(
                    if (enabled) {
                        BolsilloGradients.Primary
                    } else {
                        androidx.compose.ui.graphics.SolidColor(
                            BolsilloTheme.colors.track,
                        )
                    },
                )
                .clickable(enabled = enabled, onClick = onClick)
                .semantics { contentDescription = "saveButton" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.record_save),
            color = if (enabled) BolsilloTheme.colors.onPrimary else BolsilloTheme.colors.textDisabled,
            style = BolsilloTheme.typography.button,
        )
    }
}
