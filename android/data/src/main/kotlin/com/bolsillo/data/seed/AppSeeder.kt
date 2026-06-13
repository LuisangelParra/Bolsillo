package com.bolsillo.data.seed

import com.bolsillo.data.db.dao.AccountDao
import com.bolsillo.data.db.dao.CategoryDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs once after the DB is opened to make sure there is a default account
 * and the category taxonomy is present. Idempotent — uses Insert-or-Ignore so
 * re-running on launch is safe.
 */
@Singleton
class AppSeeder
    @Inject
    constructor(
        private val accountDao: AccountDao,
        private val categoryDao: CategoryDao,
    ) {
        suspend fun seed(now: Long = System.currentTimeMillis()) {
            if (accountDao.count() == 0) {
                accountDao.insertIfMissing(AccountSeed.defaultCash(now))
            }
            if (categoryDao.count() == 0) {
                categoryDao.insertAllIfMissing(CategorySeed.ALL)
            }
        }
    }
