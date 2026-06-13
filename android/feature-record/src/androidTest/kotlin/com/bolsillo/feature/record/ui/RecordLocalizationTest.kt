package com.bolsillo.feature.record.ui

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bolsillo.designsystem.theme.BolsilloTheme
import com.bolsillo.feature.record.presentation.RecordUiState
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class RecordLocalizationTest {
    @get:Rule val rule = createComposeRule()

    @Test fun renders_spanish_strings_by_default() {
        rule.setContent {
            withLocale(Locale("es")) {
                BolsilloTheme {
                    RecordScreen(state = RecordUiState(), onIntent = {})
                }
            }
        }
        rule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test fun renders_english_strings_when_locale_is_en() {
        rule.setContent {
            withLocale(Locale.ENGLISH) {
                BolsilloTheme {
                    RecordScreen(state = RecordUiState(), onIntent = {})
                }
            }
        }
        rule.onNodeWithText("Save").assertIsDisplayed()
    }

    @androidx.compose.runtime.Composable
    private fun withLocale(
        locale: Locale,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        val baseConfig = LocalConfiguration.current
        val newConfig = Configuration(baseConfig).apply { setLocale(locale) }
        val newContext = LocalContext.current.createConfigurationContext(newConfig)
        CompositionLocalProvider(
            LocalConfiguration provides newConfig,
            LocalContext provides newContext,
        ) {
            content()
        }
    }
}
