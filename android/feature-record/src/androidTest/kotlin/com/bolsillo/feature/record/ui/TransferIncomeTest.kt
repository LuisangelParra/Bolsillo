package com.bolsillo.feature.record.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.domain.model.AmountInput
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.feature.record.presentation.RecordUiState
import org.junit.Rule
import org.junit.Test

class TransferIncomeTest {
    @get:Rule val rule = createComposeRule()

    @Test fun transfer_same_account_error_shows_when_source_equals_dest() {
        val state =
            RecordUiState(
                amount = AmountInput("100"),
                type = TransactionType.TRANSFER,
                accountId = "a",
                destAccountId = "a",
                sameAccountError = true,
            )
        rule.setContent { BolsilloTheme { RecordScreen(state = state, onIntent = {}) } }
        rule.onNodeWithContentDescription("transferSameAccountError").assertIsDisplayed()
    }

    @Test fun income_save_button_enabled_with_amount() {
        val state =
            RecordUiState(
                amount = AmountInput("100"),
                type = TransactionType.INCOME,
                accountId = "acc-1",
            )
        rule.setContent { BolsilloTheme { RecordScreen(state = state, onIntent = {}) } }
        rule.onNodeWithContentDescription("saveButton").assertIsEnabled()
    }
}
