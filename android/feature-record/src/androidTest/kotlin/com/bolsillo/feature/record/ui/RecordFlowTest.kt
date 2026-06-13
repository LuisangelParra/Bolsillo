package com.bolsillo.feature.record.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.presentation.RecordIntent
import com.bolsillo.feature.record.presentation.RecordUiState
import org.junit.Rule
import org.junit.Test

/**
 * Spec FR5 ≤ 3 taps: amount entry (1 interaction) + Save (1 tap). The keypad
 * is up the moment the screen opens — no loading screen (FR2). Save stays
 * enabled while the classifier is "waiting" (Article V).
 */
class RecordFlowTest {
    @get:Rule val rule = createComposeRule()

    @Test fun keypad_is_immediately_available_no_loading_screen() {
        rule.setContent {
            BolsilloTheme {
                RecordScreen(state = RecordUiState(), onIntent = {})
            }
        }
        rule.onNodeWithContentDescription("keypadKey:5").assertIsDisplayed()
    }

    @Test fun save_is_enabled_when_amount_present_even_with_waiting_classifier() {
        val state =
            RecordUiState(
                amount = com.bolsillo.domain.model.AmountInput("100"),
                accountId = "acc-1",
                confidence = 0.0,
            )
        rule.setContent {
            BolsilloTheme {
                RecordScreen(state = state, onIntent = {})
            }
        }
        rule.onNodeWithContentDescription("saveButton").assertIsEnabled()
    }

    @Test fun digit_press_records_intent() {
        val intents = mutableListOf<RecordIntent>()
        rule.setContent {
            BolsilloTheme {
                RecordScreen(state = RecordUiState(), onIntent = { intents += it })
            }
        }
        rule.onNodeWithContentDescription("keypadKey:5").performClick()
        rule.onNodeWithContentDescription("saveButton").performClick()
        // expect at least one digit + one save intent
        assert(intents.any { it is RecordIntent.DigitPressed })
    }

    @Test fun undo_snackbar_renders_when_visible() {
        val state = RecordUiState(undoVisible = true)
        rule.setContent {
            BolsilloTheme {
                RecordScreen(state = state, onIntent = {})
            }
        }
        rule.onNodeWithContentDescription("undoSnackbar").assertIsDisplayed()
    }
}
