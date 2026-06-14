package com.bolsillo.domain.usecase

import com.bolsillo.domain.ai.ClassificationInput
import com.bolsillo.domain.ai.ExpenseClassifier
import com.bolsillo.domain.port.AccountRepository
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * FR 3,4,17 — pre-fill category + account. Never throws, never blocks save
 * (Article V). Falls back to last-used when the classifier returns nothing.
 */
data class Suggestion(
    val categoryId: String?,
    val accountId: String?,
    val confidence: Double,
)

class SuggestCategoryAndAccount
    @Inject
    constructor(
        private val classifier: ExpenseClassifier,
        private val transactions: TransactionRepository,
        private val accounts: AccountRepository,
    ) {
        suspend operator fun invoke(input: ClassificationInput): Suggestion {
            val classified = runCatching { classifier.suggest(input) }.getOrNull()
            val lastUsed = runCatching { transactions.lastUsed() }.getOrNull()
            val accountId =
                lastUsed?.accountId
                    ?: accounts.observeAccounts().firstOrNull()?.firstOrNull()?.id
            val categoryId =
                classified?.topCategoryId
                    ?: lastUsed?.categoryId
                    ?: "other"
            return Suggestion(
                categoryId = categoryId,
                accountId = accountId,
                confidence = classified?.confidence ?: 0.0,
            )
        }
    }
