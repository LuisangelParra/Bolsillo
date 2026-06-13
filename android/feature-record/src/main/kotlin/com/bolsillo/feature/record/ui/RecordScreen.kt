package com.bolsillo.feature.record.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.feature.record.R
import com.bolsillo.feature.record.presentation.RecordIntent
import com.bolsillo.feature.record.presentation.RecordUiState
import com.bolsillo.feature.record.presentation.RecordViewModel

@Composable
fun RecordRoute(viewModel: RecordViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecordScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun RecordScreen(
    state: RecordUiState,
    onIntent: (RecordIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = BolsilloTheme.colors.background) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.record_title),
                    style = BolsilloTheme.typography.titleL,
                    color = BolsilloTheme.colors.textPrimary,
                )

                TypeSelector(selected = state.type, onSelected = { onIntent(RecordIntent.TypeSelected(it)) })

                AmountDisplay(
                    digits = state.amount.digits,
                    decimalDigits = state.decimalDigits,
                    symbol = state.currencySymbol,
                    isIncome = state.type == TransactionType.INCOME,
                )

                CategoryChip(
                    categoryId = state.categoryId,
                    labelKey = state.categoryId ?: "",
                    showLowConfidenceBorder = state.confidence > 0.0 && state.confidence < state.classifierThreshold,
                    modifier = Modifier.fillMaxWidth(),
                )
                AccountChip(accountName = state.accountName.ifEmpty { state.accountId.orEmpty() })

                AiConfidenceBadge(confidence = state.confidence, threshold = state.classifierThreshold)

                ModeTabs(selected = state.mode)

                if (state.type == TransactionType.TRANSFER) {
                    TransferAccountsRow(
                        sourceAccountId = state.accountId,
                        destAccountId = state.destAccountId,
                        sameAccountError = state.sameAccountError,
                        accountIds = state.balances.keys.toList(),
                        onSourceSelected = { onIntent(RecordIntent.AccountSelected(it)) },
                        onDestSelected = { onIntent(RecordIntent.DestinationSelected(it)) },
                    )
                }

                AmountKeypad(
                    onDigit = { onIntent(RecordIntent.DigitPressed(it)) },
                    onBackspace = { onIntent(RecordIntent.Backspace) },
                )

                SaveButton(enabled = state.canSave, onClick = { onIntent(RecordIntent.Save) })
            }

            UndoSnackbar(
                visible = state.undoVisible,
                onUndo = { onIntent(RecordIntent.Undo) },
                onDismiss = { onIntent(RecordIntent.DismissUndo) },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
            )
        }
    }
}
