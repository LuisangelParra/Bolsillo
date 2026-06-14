package com.bolsillo.domain.usecase

import com.bolsillo.domain.ai.ClassificationInput
import com.bolsillo.domain.ai.ClassificationResult
import com.bolsillo.domain.ai.ExpenseClassifier
import com.bolsillo.domain.model.Account
import com.bolsillo.domain.model.AccountType
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import com.bolsillo.domain.port.AccountRepository
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SuggestAndUndoTest {
    private class TxRepo(seed: Transaction? = null) : TransactionRepository {
        val saved = mutableListOf<Transaction>().also { if (seed != null) it += seed }

        override fun observeAll(): Flow<List<Transaction>> = emptyFlow()

        override suspend fun getById(id: String): Transaction? = saved.firstOrNull { it.id == id }

        override suspend fun upsert(transaction: Transaction) {
            saved.removeAll { it.id == transaction.id }
            saved += transaction
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) {
            val i = saved.indexOfFirst { it.id == id }
            if (i >= 0) saved[i] = saved[i].copy(deletedAt = deletedAt)
        }

        override suspend fun restore(id: String) {
            val i = saved.indexOfFirst { it.id == id }
            if (i >= 0) saved[i] = saved[i].copy(deletedAt = null)
        }

        override suspend fun lastUsed(): Transaction? = saved.filter { !it.isDeleted }.maxByOrNull { it.createdAt }

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) = Unit

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) {
            saved.forEachIndexed { i, t ->
                if (t.transferGroupId == transferGroupId) saved[i] = t.copy(deletedAt = deletedAt)
            }
        }

        override suspend fun restoreGroup(transferGroupId: String) {
            saved.forEachIndexed { i, t ->
                if (t.transferGroupId == transferGroupId) saved[i] = t.copy(deletedAt = null)
            }
        }
    }

    private class AcctRepo : AccountRepository {
        override fun observeCurrencies(): Flow<List<Currency>> = flowOf(emptyList())

        override fun observeAccounts(): Flow<List<Account>> =
            flowOf(
                listOf(
                    Account(
                        id = "acc-1", name = "Cash", type = AccountType.CASH, currencyCode = "USD",
                        initialBalance = Money.ZERO, icon = "wallet", color = 0L,
                        createdAt = 0L, updatedAt = 0L,
                    ),
                ),
            )

        override suspend fun getById(id: String): Account? = null

        override fun observeBalance(accountId: String): Flow<Money> = flowOf(Money.ZERO)

        override fun observeBalances(): Flow<Map<String, Money>> = flowOf(emptyMap())

        override suspend fun upsert(account: Account) = Unit
    }

    private val nullClassifier =
        object : ExpenseClassifier {
            override suspend fun suggest(input: ClassificationInput): ClassificationResult =
                ClassificationResult(topCategoryId = null, confidence = 0.0, alternatives = emptyList())

            override suspend fun learn(
                input: ClassificationInput,
                chosenCategoryId: String,
            ) = Unit
        }

    @Test fun `falls back to last-used categoryId and accountId when classifier is empty`() =
        runTest {
            val lastUsed =
                Transaction(
                    id = "t1", accountId = "acc-1", type = TransactionType.EXPENSE,
                    amount = Money(-100L), currencyCode = "USD",
                    amountBase = Money(-100L), fxRateMillis = 1000L,
                    categoryId = "food.coffee", occurredAt = 0L, createdAt = 5L, updatedAt = 5L,
                )
            val s =
                SuggestCategoryAndAccount(nullClassifier, TxRepo(lastUsed), AcctRepo())
                    .invoke(ClassificationInput("", Money.ZERO, "USD", 0L, "CASH"))
            assertEquals("food.coffee", s.categoryId)
            assertEquals("acc-1", s.accountId)
            assertEquals(0.0, s.confidence, 0.0)
        }

    @Test fun `undo soft-deletes the just-created leg`() =
        runTest {
            val repo = TxRepo()
            repo.upsert(
                Transaction(
                    id = "t1", accountId = "acc-1", type = TransactionType.EXPENSE,
                    amount = Money(-100L), currencyCode = "USD",
                    amountBase = Money(-100L), fxRateMillis = 1000L,
                    occurredAt = 0L, createdAt = 1L, updatedAt = 1L,
                ),
            )
            UndoLastRecord(repo).invoke(SavedRef.Single("t1"), now = 100L)
            val stored = repo.getById("t1")
            assertNotNull(stored)
            assertEquals(100L, stored!!.deletedAt)
        }
}
