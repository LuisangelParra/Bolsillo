package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordTransactionTest {
    private class FakeRepo : TransactionRepository {
        val saved = mutableListOf<Transaction>()

        override fun observeAll(): Flow<List<Transaction>> = emptyFlow()

        override suspend fun getById(id: String): Transaction? = saved.firstOrNull { it.id == id }

        override suspend fun upsert(transaction: Transaction) {
            saved += transaction
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) = Unit

        override suspend fun restore(id: String) = Unit

        override suspend fun lastUsed(): Transaction? = saved.lastOrNull()

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

    private fun draft(
        type: TransactionType,
        amount: Long,
    ) = TransactionDraft(
        accountId = "acc-1",
        type = type,
        amount = Money(amount),
        currencyCode = "USD",
        categoryId = "food",
        occurredAt = 1000L,
    )

    @Test fun `expense persists as negative signed minor units`() =
        runTest {
            val repo = FakeRepo()
            val tx = RecordTransaction(repo).invoke(draft(TransactionType.EXPENSE, 5000), now = 42L)
            assertEquals(-5000L, tx.amount.minorUnits)
            assertEquals(-5000L, tx.amountBase.minorUnits)
            assertEquals(1, repo.saved.size)
        }

    @Test fun `income persists as positive signed minor units`() =
        runTest {
            val repo = FakeRepo()
            val tx = RecordTransaction(repo).invoke(draft(TransactionType.INCOME, 8000), now = 42L)
            assertEquals(8000L, tx.amount.minorUnits)
            assertEquals(8000L, tx.amountBase.minorUnits)
        }

    @Test fun `fx is frozen at creation - rate 1000 and amountBase equals amount`() =
        runTest {
            val repo = FakeRepo()
            val tx = RecordTransaction(repo).invoke(draft(TransactionType.EXPENSE, 5000), now = 42L)
            assertEquals(1000L, tx.fxRateMillis)
            assertEquals(tx.amount, tx.amountBase)
        }

    @Test fun `overdraft is allowed - amount applied without rejection`() =
        runTest {
            val repo = FakeRepo()
            val tx = RecordTransaction(repo).invoke(draft(TransactionType.EXPENSE, 999_999_999L))
            assertNotNull(tx.id)
            assertEquals(-999_999_999L, tx.amount.minorUnits)
            assertNull(tx.deletedAt)
            assertTrue(tx.id.isNotEmpty())
        }

    @Test(expected = IllegalArgumentException::class)
    fun `transfer drafts are rejected here`() {
        TransactionDraft(
            accountId = "a",
            type = TransactionType.TRANSFER,
            amount = Money(1L),
            currencyCode = "USD",
            categoryId = null,
            occurredAt = 0L,
        )
    }
}
