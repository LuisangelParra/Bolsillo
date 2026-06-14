package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.AccountRepository
import com.bolsillo.domain.port.TransactionRepository
import java.util.UUID
import javax.inject.Inject

data class TransferPair(val source: Transaction, val destination: Transaction)

/**
 * FR 9,10 — atomic linked double entry. Same-account rejected (FR 10),
 * cross-currency rejected (Invariant 6 — E8 will lift this). FX is frozen at
 * creation on both legs (Invariant 8 — same-currency 001 keeps rate = 1000).
 */
class RecordTransfer
    @Inject
    constructor(
        private val transactions: TransactionRepository,
        private val accounts: AccountRepository,
    ) {
        suspend operator fun invoke(
            sourceAccountId: String,
            destAccountId: String,
            amount: Money,
            occurredAt: Long,
            now: Long = System.currentTimeMillis(),
            groupIdOverride: String? = null,
        ): TransferPair {
            require(sourceAccountId != destAccountId) { "record.transfer.sameAccountError" }
            require(amount.minorUnits > 0L) { "Transfer amount must be a positive magnitude" }
            val src =
                accounts.getById(sourceAccountId)
                    ?: error("Source account not found: $sourceAccountId")
            val dest =
                accounts.getById(destAccountId)
                    ?: error("Destination account not found: $destAccountId")
            require(src.currencyCode == dest.currencyCode) {
                "Cross-currency transfers are not supported in spec 001"
            }
            val groupId = groupIdOverride ?: UUID.randomUUID().toString()
            val signedMinor = amount.minorUnits
            val legSource =
                Transaction(
                    id = UUID.randomUUID().toString(),
                    accountId = sourceAccountId,
                    type = TransactionType.TRANSFER,
                    amount = Money(-signedMinor),
                    currencyCode = src.currencyCode,
                    amountBase = Money(-signedMinor),
                    fxRateMillis = 1000L,
                    categoryId = "transfer",
                    occurredAt = occurredAt,
                    transferGroupId = groupId,
                    createdAt = now,
                    updatedAt = now,
                )
            val legDest =
                Transaction(
                    id = UUID.randomUUID().toString(),
                    accountId = destAccountId,
                    type = TransactionType.TRANSFER,
                    amount = Money(signedMinor),
                    currencyCode = dest.currencyCode,
                    amountBase = Money(signedMinor),
                    fxRateMillis = 1000L,
                    categoryId = "transfer",
                    occurredAt = occurredAt,
                    transferGroupId = groupId,
                    createdAt = now,
                    updatedAt = now,
                )
            transactions.upsertTransfer(legSource, legDest)
            return TransferPair(legSource, legDest)
        }
    }
