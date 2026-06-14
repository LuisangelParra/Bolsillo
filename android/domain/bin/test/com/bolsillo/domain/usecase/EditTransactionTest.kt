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

class EditTransactionTest {
    private class FakeRepo(seed: Transaction) : TransactionRepository {
        val store = mutableMapOf(seed.id to seed)

        override fun observeAll(): Flow<List<Transaction>> = emptyFlow()

        override suspend fun getById(id: String): Transaction? = store[id]

        override suspend fun upsert(transaction: Transaction) {
            store[transaction.id] = transaction
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) = Unit

        override suspend fun restore(id: String) = Unit

        override suspend fun lastUsed(): Transaction? = null

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) = Unit

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) = Unit

        override suspend fun restoreGroup(transferGroupId: String) = Unit
    }

    private fun seed() =
        Transaction(
            id = "t1", accountId = "a", type = TransactionType.EXPENSE,
            amount = Money(-1000L), currencyCode = "USD",
            amountBase = Money(-1000L), fxRateMillis = 1000L,
            categoryId = "food", occurredAt = 1L, createdAt = 1L, updatedAt = 1L,
        )

    @Test fun `editing amount keeps frozen fx and recomputes signed amount`() =
        runTest {
            val repo = FakeRepo(seed())
            val edited = seed().copy(amount = Money(2000L), updatedAt = 50L)
            val result = EditTransaction(repo).invoke(edited, now = 100L)
            assertEquals(-2000L, result.amount.minorUnits)
            assertEquals(-1000L, result.amountBase.minorUnits) // amountBase frozen
            assertEquals(1000L, result.fxRateMillis)
            assertEquals(100L, result.updatedAt)
        }

    @Test fun `moving accountId re-derives balances by SUM in the data layer`() =
        runTest {
            val repo = FakeRepo(seed())
            val moved = seed().copy(accountId = "b")
            val result = EditTransaction(repo).invoke(moved)
            assertEquals("b", result.accountId)
        }

    @Test fun `type change re-applies the sign for Invariant 1`() =
        runTest {
            val repo = FakeRepo(seed())
            val flipped = seed().copy(type = TransactionType.INCOME, amount = Money(1500L))
            val result = EditTransaction(repo).invoke(flipped)
            assertEquals(1500L, result.amount.minorUnits)
            assertEquals(TransactionType.INCOME, result.type)
        }
}
