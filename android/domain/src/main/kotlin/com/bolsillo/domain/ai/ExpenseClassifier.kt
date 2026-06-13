package com.bolsillo.domain.ai

import com.bolsillo.domain.model.Money

/** Input features for on-device categorization (Constitution Article V). */
data class ClassificationInput(
    val text: String,
    val amount: Money,
    val currencyCode: String,
    val occurredAt: Long,
    val accountType: String,
)

/** Suggested category with calibrated confidence and up to top-3 alternatives. */
data class ClassificationResult(
    val topCategoryId: String?,
    val confidence: Double,
    val alternatives: List<String>,
)

/**
 * Domain port for the AI categorization cascade (user rules → merchant dictionary →
 * on-device ML). The AI is accessed ONLY through this interface and is replaceable
 * without touching the domain (Constitution Articles V & VIII). It MUST never block saving.
 */
interface ExpenseClassifier {
    suspend fun suggest(input: ClassificationInput): ClassificationResult

    /** Incremental, on-device-only learning from a user confirmation/correction. */
    suspend fun learn(
        input: ClassificationInput,
        chosenCategoryId: String,
    )

    companion object {
        /** Default confidence threshold for auto-apply (Constitution Article V). */
        const val DEFAULT_THRESHOLD: Double = 0.75
    }
}
