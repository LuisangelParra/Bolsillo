package com.bolsillo.feature.record.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bolsillo.designsystem.component.SegmentedControl
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.feature.record.R

@Composable
fun TypeSelector(
    selected: TransactionType,
    modifier: Modifier = Modifier,
    onSelected: (TransactionType) -> Unit,
) {
    val options = listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)
    val labelExpense = stringResource(R.string.record_expense)
    val labelIncome = stringResource(R.string.record_income)
    val labelTransfer = stringResource(R.string.record_transfer)
    SegmentedControl(
        options = options,
        selected = selected,
        label = { t ->
            when (t) {
                TransactionType.EXPENSE -> labelExpense
                TransactionType.INCOME -> labelIncome
                TransactionType.TRANSFER -> labelTransfer
            }
        },
        modifier = modifier,
        onSelected = onSelected,
    )
}
