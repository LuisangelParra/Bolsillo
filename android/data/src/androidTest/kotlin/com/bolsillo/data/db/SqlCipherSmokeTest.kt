package com.bolsillo.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.domain.model.AccountType
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Articles I & II smoke test: the DB opens with the Keystore-wrapped passphrase,
 * and the on-disk file cannot be opened without it.
 */
@RunWith(AndroidJUnit4::class)
class SqlCipherSmokeTest {
    @Test fun db_opens_with_keystore_passphrase_and_is_unreadable_without_it() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            System.loadLibrary("sqlcipher")
            val name = "sqlcipher-smoke.db"
            context.deleteDatabase(name)

            val provider = SqlCipherKeyProvider(context)
            val factory = SupportOpenHelperFactory(provider.passphrase())
            val db =
                Room.databaseBuilder(context, BolsilloDatabase::class.java, name)
                    .openHelperFactory(factory)
                    .build()
            db.accountDao().upsert(
                AccountEntity(
                    id = "a", name = "Cash", type = AccountType.CASH, currencyCode = "USD",
                    initialBalanceMinor = 0L, icon = "wallet", color = 0L,
                    archived = false, createdAt = 0L, updatedAt = 0L,
                ),
            )
            assertNotNull(db.accountDao().getById("a"))
            db.close()

            // Opening without the passphrase should fail.
            val emptyFactory = SupportOpenHelperFactory(ByteArray(0))
            val cleartext =
                Room.databaseBuilder(context, BolsilloDatabase::class.java, name)
                    .openHelperFactory(emptyFactory)
                    .build()
            val opened = runCatching { cleartext.accountDao().getById("a") }
            cleartext.close()
            assertTrue(opened.isFailure)
        }
}
