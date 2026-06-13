package com.bolsillo.domain.model

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

/**
 * A financial entry. Amounts are [Money] (integer minor units).
 *
 * - [amountBase] and [fxRateMillis] are frozen at creation (Article III) and never recomputed.
 * - [deletedAt] implements soft delete (trash) — there are NO hard deletes (Article III).
 * - [transferGroupId] links the two legs of a transfer (double entry).
 * - Timestamps are epoch milliseconds to keep the domain module free of platform date types.
 *
 * data-model.md Invariant 1 — signed amounts:
 *   - EXPENSE        → amount < 0
 *   - INCOME         → amount > 0
 *   - TRANSFER source → amount < 0; TRANSFER destination → amount > 0
 *
 * UI displays the magnitude; balance is `initialBalance + SUM(amount)` over
 * non-deleted legs (Invariant 2).
 */
data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Money,
    val currencyCode: String,
    val amountBase: Money,
    val fxRateMillis: Long,
    val categoryId: String? = null,
    val merchant: String? = null,
    val note: String? = null,
    val occurredAt: Long,
    val transferGroupId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
) {
    val isDeleted: Boolean get() = deletedAt != null
    val magnitude: Money get() = if (amount.minorUnits < 0L) -amount else amount
}
