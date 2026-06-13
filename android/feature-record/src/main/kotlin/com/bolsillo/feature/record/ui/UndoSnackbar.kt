package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R
import kotlinx.coroutines.delay

const val UNDO_TIMEOUT_MS = 5000L

@Composable
fun UndoSnackbar(
    visible: Boolean,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    LaunchedEffect(Unit) {
        delay(UNDO_TIMEOUT_MS)
        onDismiss()
    }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(BolsilloTheme.elevation.toast, BolsilloTheme.shapes.full)
                .clip(BolsilloTheme.shapes.full)
                .background(BolsilloTheme.colors.surfaceInverse)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics { contentDescription = "undoSnackbar" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.record_saved_toast),
            color = BolsilloTheme.colors.onSurfaceInverse,
            style = BolsilloTheme.typography.body,
        )
        Text(
            text = stringResource(R.string.record_undo),
            color = BolsilloTheme.colors.primaryAccent,
            style = BolsilloTheme.typography.button,
            modifier =
                Modifier
                    .clip(BolsilloTheme.shapes.full)
                    .clickable(onClick = onUndo)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .semantics { contentDescription = "undoSnackbarAction" },
        )
    }
}
