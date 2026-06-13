package com.bolsillo.data.ai

import com.bolsillo.domain.ai.ClassificationInput
import com.bolsillo.domain.ai.ClassificationResult
import com.bolsillo.domain.ai.ExpenseClassifier
import com.bolsillo.domain.port.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub classifier used until spec 003 ships the real cascade (research R7).
 * Returns the last-used category id with confidence 0.0; never blocks save
 * (Article V). `learn()` is a no-op — on-device personalization is E4.
 */
@Singleton
class LastUsedExpenseClassifier
    @Inject
    constructor(private val transactions: TransactionRepository) : ExpenseClassifier {
        override suspend fun suggest(input: ClassificationInput): ClassificationResult {
            val last = transactions.lastUsed()
            return ClassificationResult(
                topCategoryId = last?.categoryId,
                confidence = 0.0,
                alternatives = emptyList(),
            )
        }

        override suspend fun learn(
            input: ClassificationInput,
            chosenCategoryId: String,
        ) = Unit
    }
