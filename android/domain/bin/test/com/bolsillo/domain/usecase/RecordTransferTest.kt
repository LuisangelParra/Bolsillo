package com.bolsillo.domain.usecase

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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RecordTransferTest {
    private class TxRepo : TransactionRepository {
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

        override suspend fun lastUsed(): Transaction? = null

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) {
            saved += legSource
            saved += legDest
        }

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) = Unit

        override suspend fun restoreGroup(transferGroupId: String) = Unit
    }

    private class AcctRepo(private val accounts: List<Account>) : AccountRepository {
        override fun observeCurrencies(): Flow<List<Currency>> = flowOf(emptyList())

        override fun observeAccounts(): Flow<List<Account>> = flowOf(accounts)

        override suspend fun getById(id: String): Account? = accounts.firstOrNull { it.id == id }

        override fun observeBalance(accountId: String): Flow<Money> = flowOf(Money.ZERO)

        override fun observeBalances(): Flow<Map<String, Money>> = flowOf(emptyMap())

        override suspend fun upsert(account: Account) = Unit
    }

    private fun account(
        id: String,
        currency: String = "USD",
    ) = Account(
        id = id, name = id, type = AccountType.CASH, currencyCode = currency,
        initialBalance = Money.ZERO, icon = "wallet", color = 0L,
        createdAt = 0L, updatedAt = 0L,
    )

    @Test fun `signed pair sums to zero and is marked TRANSFER`() =
        runTest {
            val txRepo = TxRepo()
            val acctRepo = AcctRepo(listOf(account("a"), account("b")))
            val pair =
                RecordTransfer(txRepo, acctRepo)
                    .invoke("a", "b", Money(2500L), occurredAt = 1L, now = 1L)
            assertEquals(-2500L, pair.source.amount.minorUnits)
            assertEquals(2500L, pair.destination.amount.minorUnits)
            assertEquals(0L, pair.source.amount.minorUnits + pair.destination.amount.minorUnits)
            assertEquals(pair.source.transferGroupId, pair.destination.transferGroupId)
            assertNotNull(pair.source.transferGroupId)
            assertEquals(TransactionType.TRANSFER, pair.source.type)
            assertEquals(TransactionType.TRANSFER, pair.destination.type)
            // FX frozen identity in same-currency 001
            assertEquals(1000L, pair.source.fxRateMillis)
            assertEquals(pair.source.amount, pair.source.amountBase)
        }

    @Test fun `same account rejected`() =
        runTest {
            val txRepo = TxRepo()
            val acctRepo = AcctRepo(listOf(account("a")))
            try {
                RecordTransfer(txRepo, acctRepo).invoke("a", "a", Money(1L), occurredAt = 0L)
                fail("Expected IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertEquals("record.transfer.sameAccountError", e.message)
            }
        }

    @Test fun `cross-currency rejected`() =
        runTest {
            val txRepo = TxRepo()
            val acctRepo = AcctRepo(listOf(account("a", "USD"), account("b", "COP")))
            try {
                RecordTransfer(txRepo, acctRepo).invoke("a", "b", Money(1L), occurredAt = 0L)
                fail("Expected IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message!!.contains("Cross-currency"))
            }
        }

    @Test fun `transfer legs are excluded from expense and income totals`() =
        runTest {
            val txRepo = TxRepo()
            val acctRepo = AcctRepo(listOf(account("a"), account("b")))
            RecordTransfer(txRepo, acctRepo).invoke("a", "b", Money(1000L), occurredAt = 0L)
            val nonTransfer = txRepo.saved.filter { it.type != TransactionType.TRANSFER }
            assertEquals(0, nonTransfer.size)
            // ids are distinct between legs
            assertNotEquals(txRepo.saved[0].id, txRepo.saved[1].id)
        }
}
