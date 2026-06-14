package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeleteRestoreTest {
    private class FakeRepo(seed: List<Transaction>) : TransactionRepository {
        val store = seed.associateBy { it.id }.toMutableMap()

        override fun observeAll(): Flow<List<Transaction>> = emptyFlow()

        override suspend fun getById(id: String): Transaction? = store[id]

        override suspend fun upsert(transaction: Transaction) {
            store[transaction.id] = transaction
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) {
            store[id]?.let { store[id] = it.copy(deletedAt = deletedAt) }
        }

        override suspend fun restore(id: String) {
            store[id]?.let { store[id] = it.copy(deletedAt = null) }
        }

        override suspend fun lastUsed(): Transaction? = null

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) {
            store[legSource.id] = legSource
            store[legDest.id] = legDest
        }

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) {
            store.values.filter { it.transferGroupId == transferGroupId }.forEach {
                store[it.id] = it.copy(deletedAt = deletedAt)
            }
        }

        override suspend fun restoreGroup(transferGroupId: String) {
            store.values.filter { it.transferGroupId == transferGroupId }.forEach {
                store[it.id] = it.copy(deletedAt = null)
            }
        }
    }

    private fun expense(id: String) =
        Transaction(
            id = id, accountId = "a", type = TransactionType.EXPENSE,
            amount = Money(-1000L), currencyCode = "USD",
            amountBase = Money(-1000L), fxRateMillis = 1000L,
            occurredAt = 0L, createdAt = 0L, updatedAt = 0L,
        )

    private fun transferLeg(
        id: String,
        accountId: String,
        signedAmount: Long,
        group: String,
    ) = Transaction(
        id = id, accountId = accountId, type = TransactionType.TRANSFER,
        amount = Money(signedAmount), currencyCode = "USD",
        amountBase = Money(signedAmount), fxRateMillis = 1000L,
        categoryId = "transfer", occurredAt = 0L, transferGroupId = group,
        createdAt = 0L, updatedAt = 0L,
    )

    @Test fun `single soft-delete clears active row`() =
        runTest {
            val repo = FakeRepo(listOf(expense("t1")))
            SoftDeleteTransaction(repo).invoke("t1", now = 50L)
            assertEquals(50L, repo.getById("t1")?.deletedAt)
        }

    @Test fun `restore clears deletedAt`() =
        runTest {
            val repo = FakeRepo(listOf(expense("t1").copy(deletedAt = 5L)))
            RestoreTransaction(repo).invoke("t1")
            assertNull(repo.getById("t1")?.deletedAt)
        }

    @Test fun `deleting one transfer leg deletes both`() =
        runTest {
            val repo =
                FakeRepo(
                    listOf(
                        transferLeg("t1", "a", -1000L, "g1"),
                        transferLeg("t2", "b", 1000L, "g1"),
                    ),
                )
            SoftDeleteTransaction(repo).invoke("t1", now = 9L)
            assertEquals(9L, repo.getById("t1")?.deletedAt)
            assertEquals(9L, repo.getById("t2")?.deletedAt)
        }

    @Test fun `restoring one transfer leg restores both`() =
        runTest {
            val repo =
                FakeRepo(
                    listOf(
                        transferLeg("t1", "a", -1000L, "g1").copy(deletedAt = 9L),
                        transferLeg("t2", "b", 1000L, "g1").copy(deletedAt = 9L),
                    ),
                )
            RestoreTransaction(repo).invoke("t1")
            assertNull(repo.getById("t1")?.deletedAt)
            assertNull(repo.getById("t2")?.deletedAt)
        }
}
