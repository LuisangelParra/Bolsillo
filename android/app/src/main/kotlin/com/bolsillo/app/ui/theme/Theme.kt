package com.bolsillo.app.ui.theme

import androidx.compose.runtime.Composable

// Legacy entry point. The real theme is in :designsystem
// (com.bolsillo.designsystem.theme.BolsilloTheme). This shim forwards calls so
// older imports keep working until they're updated.
@Composable
fun BolsilloTheme(content: @Composable () -> Unit) {
    com.bolsillo.designsystem.theme.BolsilloTheme(content = content)
}
