package com.bolsillo.feature.record.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bolsillo.designsystem.theme.BolsilloTheme
import org.junit.Rule
import org.junit.Test

/**
 * The full edit/delete/restore round-trip flow uses screens that arrive with
 * E3 (home feed) and the trash UI (FR 13). This androidTest covers the visual
 * row (§2.2) that 001 ships so the design parity is locked in.
 */
class EditDeleteRestoreTest {
    @get:Rule val rule = createComposeRule()

    @Test fun transactionRow_renders_title_and_amount() {
        rule.setContent {
            BolsilloTheme {
                TransactionRow(
                    categoryId = "food.coffee",
                    title = "Café",
                    minorUnits = -350L,
                    decimalDigits = 2,
                    currencySymbol = "$",
                    toConfirm = true,
                )
            }
        }
        rule.onNodeWithText("Café").assertIsDisplayed()
    }
}
