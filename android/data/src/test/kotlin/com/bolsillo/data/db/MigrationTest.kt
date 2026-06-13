package com.bolsillo.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.data.db.entity.TransactionEntity
import com.bolsillo.domain.model.AccountType
import com.bolsillo.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Schema v1 baseline check. When v2 lands, this file gains a real
 * MigrationTestHelper-backed v1→v2 test that seeds sample rows and asserts no
 * data loss (Article IX — release is blocked on data loss).
 *
 * In v1 the harness just proves: opening a fresh DB, writing sample rows, and
 * reopening preserves everything (no implicit destructive migration).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MigrationTest {
    @Test fun `v1 baseline preserves sample data across re-open`() =
        runTest {
            val name = "migration-v1.db"
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            context.deleteDatabase(name)

            Room.databaseBuilder(context, BolsilloDatabase::class.java, name)
                .allowMainThreadQueries()
                .build()
                .use { db ->
                    db.accountDao().upsert(
                        AccountEntity(
                            id = "a", name = "Cash", type = AccountType.CASH, currencyCode = "USD",
                            initialBalanceMinor = 1000L, icon = "wallet", color = 0L,
                            archived = false, createdAt = 1L, updatedAt = 1L,
                        ),
                    )
                    db.transactionDao().upsert(
                        TransactionEntity(
                            id = "t", accountId = "a", type = TransactionType.EXPENSE,
                            amountMinor = -500L, currencyCode = "USD",
                            amountBaseMinor = -500L, fxRateMillis = 1000L,
                            categoryId = "food", merchant = null, note = null, occurredAt = 1L,
                            transferGroupId = null, createdAt = 1L, updatedAt = 1L, deletedAt = null,
                        ),
                    )
                }

            Room.databaseBuilder(context, BolsilloDatabase::class.java, name)
                .allowMainThreadQueries()
                .build()
                .use { db ->
                    assertNotNull(db.accountDao().getById("a"))
                    val tx = db.transactionDao().getById("t")
                    assertNotNull(tx)
                    assertEquals(-500L, tx!!.amountMinor)
                }
            context.deleteDatabase(name)
        }

    private inline fun <R> BolsilloDatabase.use(block: (BolsilloDatabase) -> R): R =
        try {
            block(this)
        } finally {
            close()
        }
}
