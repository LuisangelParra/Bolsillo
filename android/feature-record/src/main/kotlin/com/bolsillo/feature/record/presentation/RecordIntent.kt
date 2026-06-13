package com.bolsillo.feature.record.presentation

import com.bolsillo.domain.model.TransactionType

sealed class RecordIntent {
    data class DigitPressed(val digit: Char) : RecordIntent()

    data object Backspace : RecordIntent()

    data class TypeSelected(val type: TransactionType) : RecordIntent()

    data class CategorySelected(val categoryId: String) : RecordIntent()

    data class AccountSelected(val accountId: String) : RecordIntent()

    data class DestinationSelected(val accountId: String) : RecordIntent()

    data class ModeSelected(val mode: RecordMode) : RecordIntent()

    data object Save : RecordIntent()

    data object Undo : RecordIntent()

    data object DismissUndo : RecordIntent()

    data class DeleteSaved(val id: String) : RecordIntent()

    data class RestoreSaved(val id: String) : RecordIntent()

    data class EditAmount(val id: String, val newAmountMinor: Long) : RecordIntent()
}
