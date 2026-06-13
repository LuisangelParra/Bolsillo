package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

/**
 * Property test (FR 15): under any sequence of record / edit / softDelete /
 * restore / transfer ops, derived balance is always
 *   initialBalance + SUM(signed amount of non-deleted legs)
 * and reconciles by construction (Invariant 2).
 */
class ReconciliationTest {
    private class Store : TransactionRepository {
        val rows = mutableMapOf<String, Transaction>()

        override fun observeAll(): Flow<List<Transaction>> = emptyFlow()

        override suspend fun getById(id: String): Transaction? = rows[id]

        override suspend fun upsert(transaction: Transaction) {
            rows[transaction.id] = transaction
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) {
            rows[id]?.let { rows[id] = it.copy(deletedAt = deletedAt) }
        }

        override suspend fun restore(id: String) {
            rows[id]?.let { rows[id] = it.copy(deletedAt = null) }
        }

        override suspend fun lastUsed(): Transaction? = rows.values.lastOrNull { !it.isDeleted }

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) {
            rows[legSource.id] = legSource
            rows[legDest.id] = legDest
        }

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) {
            rows.values.filter { it.transferGroupId == transferGroupId }.forEach {
                rows[it.id] = it.copy(deletedAt = deletedAt)
            }
        }

        override suspend fun restoreGroup(transferGroupId: String) {
            rows.values.filter { it.transferGroupId == transferGroupId }.forEach {
                rows[it.id] = it.copy(deletedAt = null)
            }
        }
    }

    private fun derivedBalance(
        initial: Long,
        store: Store,
        accountId: String,
    ): Long =
        initial +
            store.rows.values
                .filter { !it.isDeleted && it.accountId == accountId }
                .sumOf { it.amount.minorUnits }

    @Test fun `random op sequence keeps both account balances reconciling`() =
        runTest {
            val rng = Random(42)
            val store = Store()
            val initialA = 10_000L
            val initialB = 5_000L
            repeat(500) { step ->
                when (rng.nextInt(6)) {
                    0 -> {
                        // record expense on A
                        val mag = rng.nextLong(1L, 5_000L)
                        val id = "tx-$step"
                        store.upsert(
                            Transaction(
                                id = id, accountId = "A", type = TransactionType.EXPENSE,
                                amount = Money(-mag), currencyCode = "USD",
                                amountBase = Money(-mag), fxRateMillis = 1000L,
                                occurredAt = step.toLong(), createdAt = step.toLong(), updatedAt = step.toLong(),
                            ),
                        )
                    }
                    1 -> {
                        // record income on A
                        val mag = rng.nextLong(1L, 5_000L)
                        val id = "tx-$step"
                        store.upsert(
                            Transaction(
                                id = id, accountId = "A", type = TransactionType.INCOME,
                                amount = Money(mag), currencyCode = "USD",
                                amountBase = Money(mag), fxRateMillis = 1000L,
                                occurredAt = step.toLong(), createdAt = step.toLong(), updatedAt = step.toLong(),
                            ),
                        )
                    }
                    2 -> {
                        // record transfer A→B
                        val mag = rng.nextLong(1L, 1_000L)
                        val group = "g-$step"
                        store.upsertTransfer(
                            Transaction(
                                id = "txA-$step", accountId = "A", type = TransactionType.TRANSFER,
                                amount = Money(-mag), currencyCode = "USD",
                                amountBase = Money(-mag), fxRateMillis = 1000L,
                                occurredAt = step.toLong(), transferGroupId = group,
                                createdAt = step.toLong(), updatedAt = step.toLong(),
                            ),
                            Transaction(
                                id = "txB-$step", accountId = "B", type = TransactionType.TRANSFER,
                                amount = Money(mag), currencyCode = "USD",
                                amountBase = Money(mag), fxRateMillis = 1000L,
                                occurredAt = step.toLong(), transferGroupId = group,
                                createdAt = step.toLong(), updatedAt = step.toLong(),
                            ),
                        )
                    }
                    3 -> {
                        // Soft-delete via the use case so transfer legs go together.
                        val target = store.rows.keys.randomOrNull(rng) ?: return@repeat
                        SoftDeleteTransaction(store).invoke(target, now = step.toLong())
                    }
                    4 -> {
                        // Restore via the use case so transfer pairs come back together.
                        val target = store.rows.keys.randomOrNull(rng) ?: return@repeat
                        RestoreTransaction(store).invoke(target)
                    }
                    5 -> {
                        // edit amount of a random non-transfer
                        val target =
                            store.rows.values
                                .filter { !it.isDeleted && it.transferGroupId == null }
                                .randomOrNull(rng) ?: return@repeat
                        val mag = rng.nextLong(1L, 3_000L)
                        val sign = if (target.type == TransactionType.EXPENSE) -1L else 1L
                        store.upsert(
                            target.copy(amount = Money(sign * mag), updatedAt = step.toLong()),
                        )
                    }
                }

                val a = derivedBalance(initialA, store, "A")
                val b = derivedBalance(initialB, store, "B")
                // Independent reconciliation: SUM(signed) over both accounts equals the
                // total non-transfer flow (transfer legs sum to 0 by Invariant 4).
                val nonTransferSum =
                    store.rows.values
                        .filter { !it.isDeleted && it.transferGroupId == null }
                        .sumOf { it.amount.minorUnits }
                assertEquals(initialA + initialB + nonTransferSum, a + b)
            }
        }
}
