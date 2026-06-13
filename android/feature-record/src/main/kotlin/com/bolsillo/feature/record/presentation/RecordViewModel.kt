package com.bolsillo.feature.record.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolsillo.domain.ai.ClassificationInput
import com.bolsillo.domain.ai.ExpenseClassifier
import com.bolsillo.domain.model.AmountInput
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.AccountRepository
import com.bolsillo.domain.usecase.EditTransaction
import com.bolsillo.domain.usecase.MoneyParser
import com.bolsillo.domain.usecase.ObserveAccountBalances
import com.bolsillo.domain.usecase.RecordTransaction
import com.bolsillo.domain.usecase.RecordTransfer
import com.bolsillo.domain.usecase.RestoreTransaction
import com.bolsillo.domain.usecase.SavedRef
import com.bolsillo.domain.usecase.SoftDeleteTransaction
import com.bolsillo.domain.usecase.SuggestCategoryAndAccount
import com.bolsillo.domain.usecase.TransactionDraft
import com.bolsillo.domain.usecase.UndoLastRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel
    @Inject
    constructor(
        private val recordTransaction: RecordTransaction,
        private val recordTransfer: RecordTransfer,
        private val undoLastRecord: UndoLastRecord,
        private val editTransaction: EditTransaction,
        private val softDeleteTransaction: SoftDeleteTransaction,
        private val restoreTransaction: RestoreTransaction,
        private val suggestCategoryAndAccount: SuggestCategoryAndAccount,
        private val observeBalances: ObserveAccountBalances,
        private val accounts: AccountRepository,
        private val transactions: com.bolsillo.domain.port.TransactionRepository,
        private val moneyParser: MoneyParser,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RecordUiState(classifierThreshold = ExpenseClassifier.DEFAULT_THRESHOLD))
        val state: StateFlow<RecordUiState> = _state.asStateFlow()

        private val _events = Channel<RecordUiEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        private var balancesJob: Job? = null

        init {
            viewModelScope.launch { bootstrap() }
            observeBalanceStream()
        }

        private suspend fun bootstrap() {
            val suggestion =
                suggestCategoryAndAccount(
                    ClassificationInput(
                        text = "",
                        amount = Money.ZERO,
                        currencyCode = _state.value.currencyCode,
                        occurredAt = System.currentTimeMillis(),
                        accountType = "CASH",
                    ),
                )
            val firstAccount = accounts.observeAccounts().firstOrNull()?.firstOrNull()
            _state.update {
                it.copy(
                    categoryId = suggestion.categoryId,
                    accountId = suggestion.accountId ?: firstAccount?.id,
                    confidence = suggestion.confidence,
                    accountName = firstAccount?.name ?: it.accountName,
                    accountNameKey = firstAccount?.name,
                )
            }
        }

        private fun observeBalanceStream() {
            balancesJob?.cancel()
            balancesJob =
                viewModelScope.launch {
                    observeBalances().collect { balances ->
                        val current = _state.value
                        val accId = current.accountId
                        _state.update {
                            it.copy(
                                balances = balances,
                                balance = if (accId != null) balances[accId] ?: Money.ZERO else Money.ZERO,
                            )
                        }
                    }
                }
        }

        fun onIntent(intent: RecordIntent) {
            when (intent) {
                is RecordIntent.DigitPressed ->
                    _state.update {
                        val next =
                            if (intent.digit == '0' && it.amount.isEmpty) {
                                it.amount
                            } else {
                                it.amount.append(intent.digit)
                            }
                        it.copy(amount = next)
                    }
                is RecordIntent.Backspace -> _state.update { it.copy(amount = it.amount.backspace()) }
                is RecordIntent.TypeSelected ->
                    _state.update {
                        val resetDest = if (intent.type != TransactionType.TRANSFER) null else it.destAccountId
                        it.copy(
                            type = intent.type,
                            destAccountId = resetDest,
                            sameAccountError = false,
                        )
                    }
                is RecordIntent.CategorySelected -> _state.update { it.copy(categoryId = intent.categoryId) }
                is RecordIntent.AccountSelected ->
                    _state.update {
                        it.copy(
                            accountId = intent.accountId,
                            sameAccountError = it.destAccountId == intent.accountId,
                        )
                    }
                is RecordIntent.DestinationSelected ->
                    _state.update {
                        it.copy(
                            destAccountId = intent.accountId,
                            sameAccountError = it.accountId == intent.accountId,
                        )
                    }
                is RecordIntent.ModeSelected -> _state.update { it.copy(mode = intent.mode) }
                is RecordIntent.Save -> handleSave()
                is RecordIntent.Undo -> handleUndo()
                is RecordIntent.DismissUndo ->
                    _state.update {
                        it.copy(undoVisible = false, lastSavedRef = null)
                    }
                is RecordIntent.DeleteSaved ->
                    viewModelScope.launch {
                        softDeleteTransaction(intent.id)
                    }
                is RecordIntent.RestoreSaved ->
                    viewModelScope.launch {
                        restoreTransaction(intent.id)
                    }
                is RecordIntent.EditAmount ->
                    viewModelScope.launch {
                        val existing = transactions.getById(intent.id) ?: return@launch
                        val minor = intent.newAmountMinor
                        val magnitude = if (minor < 0L) -minor else minor
                        editTransaction(existing.copy(amount = Money(magnitude)))
                    }
            }
        }

        private fun handleSave() {
            val s = _state.value
            if (!s.canSave) return
            val accountId = s.accountId ?: return
            if (s.type == TransactionType.TRANSFER) {
                val dest = s.destAccountId
                if (dest == null || dest == accountId) {
                    _state.update { it.copy(sameAccountError = true) }
                    _events.trySend(RecordUiEvent.ValidationError("record.transfer.sameAccountError"))
                    return
                }
                _state.update { it.copy(isSaving = true) }
                viewModelScope.launch {
                    val amount = moneyParser.parse(s.amount, currency())
                    val pair =
                        recordTransfer(
                            sourceAccountId = accountId,
                            destAccountId = dest,
                            amount = amount,
                            occurredAt = System.currentTimeMillis(),
                        )
                    _state.update {
                        it.copy(
                            isSaving = false,
                            amount = AmountInput.EMPTY,
                            lastSavedRef = LastSaved.Group(pair.source.transferGroupId!!),
                            undoVisible = true,
                        )
                    }
                    _events.trySend(RecordUiEvent.Saved)
                }
                return
            }
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                val amount = moneyParser.parse(s.amount, currency())
                val draft =
                    TransactionDraft(
                        accountId = accountId,
                        type = s.type,
                        amount = amount,
                        currencyCode = s.currencyCode,
                        categoryId = s.categoryId,
                        occurredAt = System.currentTimeMillis(),
                    )
                val saved = recordTransaction(draft)
                _state.update {
                    it.copy(
                        isSaving = false,
                        amount = AmountInput.EMPTY,
                        lastSavedRef = LastSaved.Single(saved.id),
                        undoVisible = true,
                    )
                }
                _events.trySend(RecordUiEvent.Saved)
            }
        }

        private fun handleUndo() {
            val ref = _state.value.lastSavedRef ?: return
            viewModelScope.launch {
                when (ref) {
                    is LastSaved.Single -> undoLastRecord(SavedRef.Single(ref.id))
                    is LastSaved.Group -> undoLastRecord(SavedRef.Group(ref.transferGroupId))
                }
                _state.update { it.copy(undoVisible = false, lastSavedRef = null) }
                _events.trySend(RecordUiEvent.Undone)
            }
        }

        private fun currency() =
            com.bolsillo.domain.model.Currency(
                code = _state.value.currencyCode,
                symbol = _state.value.currencySymbol,
                decimalDigits = _state.value.decimalDigits,
                isEnabled = true,
                isEssential = true,
            )
    }
