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

class IncomeTest {
    private class FakeRepo : TransactionRepository {
        val saved = mutableListOf<Transaction>()

        override fun observeAll(): Flow<List<Transaction>> = emptyFlow()

        override suspend fun getById(id: String): Transaction? = null

        override suspend fun upsert(transaction: Transaction) {
            saved += transaction
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

    @Test fun `income raises balance with positive signed amount and is marked income`() =
        runTest {
            val repo = FakeRepo()
            val tx =
                RecordTransaction(repo).invoke(
                    TransactionDraft(
                        accountId = "acc-1",
                        type = TransactionType.INCOME,
                        amount = Money(7500L),
                        currencyCode = "USD",
                        categoryId = "income",
                        occurredAt = 0L,
                    ),
                    now = 1L,
                )
            assertEquals(TransactionType.INCOME, tx.type)
            assertEquals(7500L, tx.amount.minorUnits)
        }
}
