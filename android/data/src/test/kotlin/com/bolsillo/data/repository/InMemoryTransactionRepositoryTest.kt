package com.bolsillo.data.repository

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryTransactionRepositoryTest {
    private fun sample(id: String) =
        Transaction(
            id = id,
            accountId = "acc-1",
            type = TransactionType.EXPENSE,
            amount = Money(1500L),
            currencyCode = "COP",
            amountBase = Money(1500L),
            fxRateMillis = 1000L,
            occurredAt = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )

    @Test
    fun `soft delete hides from observeAll but keeps the record`() =
        runTest {
            val repo = InMemoryTransactionRepository()
            repo.upsert(sample("t1"))

            repo.softDelete("t1", deletedAt = 123L)

            // Not visible in the active list...
            assertTrue(repo.observeAll().first().isEmpty())
            // ...but the record still exists (no hard delete) and is restorable.
            val stored = repo.getById("t1")
            assertNotNull(stored)
            assertEquals(123L, stored!!.deletedAt)

            repo.restore("t1")
            assertEquals(1, repo.observeAll().first().size)
        }
}
