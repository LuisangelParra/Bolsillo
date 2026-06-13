package com.bolsillo.feature.record.presentation

import com.bolsillo.domain.model.AmountInput
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.TransactionType

enum class RecordMode { Keypad, Text, Receipt }

data class RecordUiState(
    val amount: AmountInput = AmountInput.EMPTY,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: String? = null,
    val accountId: String? = null,
    val destAccountId: String? = null,
    val currencyCode: String = "USD",
    val decimalDigits: Int = 2,
    val currencySymbol: String = "$",
    val confidence: Double = 0.0,
    val classifierThreshold: Double = 0.75,
    val mode: RecordMode = RecordMode.Keypad,
    val isSaving: Boolean = false,
    val sameAccountError: Boolean = false,
    val balance: Money = Money.ZERO,
    val balances: Map<String, Money> = emptyMap(),
    val accountName: String = "",
    val accountNameKey: String? = null,
    val lastSavedRef: LastSaved? = null,
    val undoVisible: Boolean = false,
) {
    val canSave: Boolean get() = !amount.isEmpty && !isSaving && !sameAccountError
}

sealed class LastSaved {
    data class Single(val id: String) : LastSaved()

    data class Group(val transferGroupId: String) : LastSaved()
}
