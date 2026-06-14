package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.TransactionRepository
import javax.inject.Inject

/**
 * FR 11 — edit a transaction. Balance is re-derived (no cached column), so
 * touching `accountId`/`amount` automatically fixes both affected balances.
 *
 * Article III / Invariant 8 — `fxRateMillis` and `amountBase` are FROZEN at
 * creation; this use case preserves whatever was stored and never recomputes.
 */
class EditTransaction
    @Inject
    constructor(private val transactions: TransactionRepository) {
        suspend operator fun invoke(
            updated: Transaction,
            now: Long = System.currentTimeMillis(),
        ): Transaction {
            val existing =
                transactions.getById(updated.id)
                    ?: error("Cannot edit non-existent transaction: ${updated.id}")
            // Re-apply the sign from the type to keep Invariant 1 consistent
            val minor = updated.amount.minorUnits
            val magnitude = if (minor < 0L) -minor else minor
            val signed =
                when (updated.type) {
                    TransactionType.EXPENSE -> -magnitude
                    TransactionType.INCOME -> magnitude
                    TransactionType.TRANSFER -> updated.amount.minorUnits
                }
            val preserved =
                updated.copy(
                    amount = Money(signed),
                    amountBase = existing.amountBase,
                    fxRateMillis = existing.fxRateMillis,
                    createdAt = existing.createdAt,
                    updatedAt = now,
                )
            transactions.upsert(preserved)
            return preserved
        }
    }
