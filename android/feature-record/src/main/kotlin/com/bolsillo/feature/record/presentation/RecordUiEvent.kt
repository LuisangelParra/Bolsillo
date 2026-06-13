package com.bolsillo.feature.record.presentation

sealed class RecordUiEvent {
    data object Saved : RecordUiEvent()

    data object Undone : RecordUiEvent()

    data class ValidationError(val messageKey: String) : RecordUiEvent()
}
