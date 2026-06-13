package com.bolsillo.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.data.db.entity.TransactionEntity
import com.bolsillo.domain.model.AccountType
import com.bolsillo.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TransactionDaoTest {
    private lateinit var db: BolsilloDatabase

    @Before fun setup() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                BolsilloDatabase::class.java,
            ).allowMainThreadQueries().build()
    }

    @After fun tearDown() {
        db.close()
    }

    private fun seedAccount(
        id: String = "acc-1",
        initial: Long = 100_00L,
    ) = runTest {
        db.accountDao().upsert(
            AccountEntity(
                id = id, name = "Cash", type = AccountType.CASH, currencyCode = "USD",
                initialBalanceMinor = initial, icon = "wallet", color = 0L,
                archived = false, createdAt = 1L, updatedAt = 1L,
            ),
        )
    }

    private fun tx(
        id: String,
        amount: Long,
        type: TransactionType = TransactionType.EXPENSE,
        deletedAt: Long? = null,
        group: String? = null,
        accountId: String = "acc-1",
    ) = TransactionEntity(
        id = id, accountId = accountId, type = type, amountMinor = amount,
        currencyCode = "USD", amountBaseMinor = amount, fxRateMillis = 1000L,
        categoryId = "food", merchant = null, note = null, occurredAt = 100L,
        transferGroupId = group, createdAt = 100L, updatedAt = 100L, deletedAt = deletedAt,
    )

    @Test fun `observeAll filters soft-deleted rows`() =
        runTest {
            seedAccount()
            val dao = db.transactionDao()
            dao.upsert(tx("t1", -1000L))
            dao.upsert(tx("t2", -2000L, deletedAt = 5L))
            val rows = dao.observeAll().first()
            assertEquals(1, rows.size)
            assertEquals("t1", rows.first().id)
        }

    @Test fun `signed sum ignores soft-deleted legs`() =
        runTest {
            seedAccount()
            val dao = db.transactionDao()
            dao.upsert(tx("t1", -1000L))
            dao.upsert(tx("t2", -500L))
            dao.upsert(tx("t3", 2000L, type = TransactionType.INCOME))
            dao.upsert(tx("t4", -9999L, deletedAt = 1L))
            val sum = dao.observeSignedSum("acc-1").first()
            // -1000 - 500 + 2000 = 500
            assertEquals(500L, sum)
        }

    @Test fun `softDelete + restore round trip`() =
        runTest {
            seedAccount()
            val dao = db.transactionDao()
            dao.upsert(tx("t1", -1000L))
            dao.softDelete("t1", deletedAt = 7L)
            assertNotNull(dao.getById("t1"))
            assertEquals(0, dao.observeAll().first().size)
            dao.restore("t1", updatedAt = 8L)
            assertEquals(1, dao.observeAll().first().size)
        }

    @Test fun `softDeleteGroup hits every leg in the group`() =
        runTest {
            seedAccount("acc-1")
            seedAccount("acc-2", initial = 0L)
            val dao = db.transactionDao()
            dao.upsertAll(
                listOf(
                    tx("t1", -1000L, type = TransactionType.TRANSFER, group = "g1"),
                    tx("t2", 1000L, type = TransactionType.TRANSFER, group = "g1", accountId = "acc-2"),
                ),
            )
            dao.softDeleteGroup("g1", deletedAt = 9L)
            val rows = dao.byTransferGroup("g1")
            assertEquals(2, rows.size)
            rows.forEach { assertEquals(9L, it.deletedAt) }
        }
}
