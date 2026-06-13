package com.bolsillo.feature.record.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.R

/**
 * Source/dest picker with inline same-account error (FR 10, §3 record sheet).
 * Built minimal — full account picker arrives in spec 011; this surfaces the
 * accounts already known to the ViewModel.
 */
@Composable
fun TransferAccountsRow(
    sourceAccountId: String?,
    destAccountId: String?,
    sameAccountError: Boolean,
    accountIds: List<String>,
    modifier: Modifier = Modifier,
    onSourceSelected: (String) -> Unit,
    onDestSelected: (String) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccountPicker(
                labelRes = R.string.record_transfer_source,
                selected = sourceAccountId,
                options = accountIds,
                modifier = Modifier.weight(1f),
                onSelected = onSourceSelected,
            )
            AccountPicker(
                labelRes = R.string.record_transfer_destination,
                selected = destAccountId,
                options = accountIds,
                modifier = Modifier.weight(1f),
                onSelected = onDestSelected,
            )
        }
        if (sameAccountError) {
            Text(
                text = stringResource(R.string.record_transfer_sameAccountError),
                color = BolsilloTheme.colors.danger,
                style = BolsilloTheme.typography.caption,
                modifier = Modifier.semantics { contentDescription = "transferSameAccountError" },
            )
        }
    }
}

@Composable
private fun AccountPicker(
    labelRes: Int,
    selected: String?,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(BolsilloTheme.shapes.chip)
                .background(BolsilloTheme.colors.surface)
                .clickable {
                    val next = options.firstOrNull { it != selected } ?: return@clickable
                    onSelected(next)
                }
                .padding(10.dp),
    ) {
        Text(
            text = stringResource(labelRes),
            color = BolsilloTheme.colors.textMuted,
            style = BolsilloTheme.typography.caption,
        )
        Text(
            text = selected ?: "—",
            color = BolsilloTheme.colors.textPrimary,
            style = BolsilloTheme.typography.bodyValue,
        )
    }
}
