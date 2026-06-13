package com.bolsillo.domain.model

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

/**
 * A financial entry. Amounts are [Money] (integer minor units).
 *
 * - [amountBase] and [fxRateMillis] are frozen at creation (Article III) and never recomputed.
 * - [deletedAt] implements soft delete (trash) — there are NO hard deletes (Article III).
 * - [transferGroupId] links the two legs of a transfer (double entry).
 *
 * Timestamps are epoch milliseconds to keep the domain module free of platform date types.
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
}
