package com.bolsillo.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.data.repository.RoomTransactionRepository
import com.bolsillo.domain.model.AccountType
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TransferAtomicityTest {
    private lateinit var db: BolsilloDatabase
    private lateinit var repo: RoomTransactionRepository

    @Before fun setup() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                BolsilloDatabase::class.java,
            ).allowMainThreadQueries().build()
        repo = RoomTransactionRepository(db, db.transactionDao())
    }

    @After fun tearDown() {
        db.close()
    }

    private fun seedAccounts() =
        runTest {
            listOf("a", "b").forEach { id ->
                db.accountDao().upsert(
                    AccountEntity(
                        id = id, name = id, type = AccountType.CASH, currencyCode = "USD",
                        initialBalanceMinor = 0L, icon = "wallet", color = 0L,
                        archived = false, createdAt = 0L, updatedAt = 0L,
                    ),
                )
            }
        }

    private fun leg(
        id: String,
        accountId: String,
        amount: Long,
        group: String,
    ) = Transaction(
        id = id, accountId = accountId, type = TransactionType.TRANSFER,
        amount = Money(amount), currencyCode = "USD",
        amountBase = Money(amount), fxRateMillis = 1000L,
        categoryId = "transfer", occurredAt = 1L, transferGroupId = group,
        createdAt = 1L, updatedAt = 1L,
    )

    @Test fun `pair write persists both rows`() =
        runTest {
            seedAccounts()
            repo.upsertTransfer(leg("t1", "a", -1000L, "g1"), leg("t2", "b", 1000L, "g1"))
            val rows = db.transactionDao().byTransferGroup("g1")
            assertEquals(2, rows.size)
            assertEquals(0L, rows.sumOf { it.amountMinor })
        }

    @Test fun `softDeleteGroup soft-deletes every leg in the group`() =
        runTest {
            seedAccounts()
            repo.upsertTransfer(leg("t1", "a", -1000L, "g1"), leg("t2", "b", 1000L, "g1"))
            repo.softDeleteGroup("g1", deletedAt = 99L)
            db.transactionDao().byTransferGroup("g1").forEach { assertEquals(99L, it.deletedAt) }
        }

    @Test fun `restoreGroup clears deletedAt on every leg`() =
        runTest {
            seedAccounts()
            repo.upsertTransfer(leg("t1", "a", -1000L, "g1"), leg("t2", "b", 1000L, "g1"))
            repo.softDeleteGroup("g1", deletedAt = 99L)
            repo.restoreGroup("g1")
            db.transactionDao().byTransferGroup("g1").forEach { assertEquals(null, it.deletedAt) }
        }

    @Test fun `pair upsert is idempotent under withTransaction`() =
        runTest {
            seedAccounts()
            repo.upsertTransfer(leg("t1", "a", -100L, "g1"), leg("t2", "b", 100L, "g1"))
            repo.upsertTransfer(leg("t1", "a", -100L, "g1"), leg("t2", "b", 100L, "g1"))
            assertEquals(2, db.transactionDao().byTransferGroup("g1").size)
        }
}
