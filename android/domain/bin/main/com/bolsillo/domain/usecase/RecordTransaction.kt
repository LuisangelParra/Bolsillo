package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.TransactionRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Draft handed to [RecordTransaction]. `amount` is a magnitude (positive
 * minor units); sign is applied based on [type].
 */
data class TransactionDraft(
    val accountId: String,
    val type: TransactionType,
    val amount: Money,
    val currencyCode: String,
    val categoryId: String?,
    val merchant: String? = null,
    val note: String? = null,
    val occurredAt: Long,
) {
    init {
        require(type != TransactionType.TRANSFER) {
            "TRANSFER drafts go through RecordTransfer, not RecordTransaction"
        }
        require(amount.minorUnits >= 0L) { "Draft amount must be a magnitude" }
    }
}

/**
 * FR 5,8,14,16,19 — single transactional write of an expense/income leg.
 *   - Signed amount per data-model.md Invariant 1 (expense<0, income>0).
 *   - Frozen FX (Invariant 8): `fxRateMillis = 1000`, `amountBase = amount` at
 *     creation — never recomputed on edit. Article III.
 *   - Overdraft is allowed (FR 19) — caller decides whether to warn.
 */
class RecordTransaction
    @Inject
    constructor(private val transactions: TransactionRepository) {
        suspend operator fun invoke(
            draft: TransactionDraft,
            now: Long = System.currentTimeMillis(),
            idOverride: String? = null,
        ): Transaction {
            val sign = if (draft.type == TransactionType.EXPENSE) -1L else 1L
            val signedMinor = sign * draft.amount.minorUnits
            val tx =
                Transaction(
                    id = idOverride ?: UUID.randomUUID().toString(),
                    accountId = draft.accountId,
                    type = draft.type,
                    amount = Money(signedMinor),
                    currencyCode = draft.currencyCode,
                    amountBase = Money(signedMinor),
                    fxRateMillis = 1000L,
                    categoryId = draft.categoryId,
                    merchant = draft.merchant,
                    note = draft.note,
                    occurredAt = draft.occurredAt,
                    transferGroupId = null,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null,
                )
            transactions.upsert(tx)
            return tx
        }
    }
