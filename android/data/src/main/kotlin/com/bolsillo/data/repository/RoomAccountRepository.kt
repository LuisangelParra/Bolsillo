package com.bolsillo.data.repository

import com.bolsillo.data.currency.CurrencySeed
import com.bolsillo.data.db.dao.AccountDao
import com.bolsillo.data.db.dao.TransactionDao
import com.bolsillo.data.db.toDomain
import com.bolsillo.data.db.toEntity
import com.bolsillo.domain.model.Account
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.port.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Balance is **derived** (data-model.md Invariant 2):
 *   balance = initialBalance + SUM(signed amount of non-deleted legs)
 * NO stored balance column. Reconciliation holds by construction.
 */
@Singleton
class RoomAccountRepository
    @Inject
    constructor(
        private val accountDao: AccountDao,
        private val transactionDao: TransactionDao,
    ) : AccountRepository {
        override fun observeCurrencies(): Flow<List<Currency>> = flowOf(CurrencySeed.essentials)

        override fun observeAccounts(): Flow<List<Account>> =
            accountDao.observeAccounts().map { rows -> rows.map { it.toDomain() } }

        override suspend fun getById(id: String): Account? = accountDao.getById(id)?.toDomain()

        override fun observeBalance(accountId: String): Flow<Money> =
            combine(
                accountDao.observeAccounts(),
                transactionDao.observeSignedSum(accountId),
            ) { accounts, sum ->
                val initial = accounts.firstOrNull { it.id == accountId }?.initialBalanceMinor ?: 0L
                Money(initial + sum)
            }

        override fun observeBalances(): Flow<Map<String, Money>> =
            combine(
                accountDao.observeAccounts(),
                transactionDao.observeSignedSums(),
            ) { accounts, sums ->
                val sumByAccount = sums.associate { it.accountId to it.sum }
                accounts.associate { acc ->
                    acc.id to Money(acc.initialBalanceMinor + (sumByAccount[acc.id] ?: 0L))
                }
            }

        override suspend fun upsert(account: Account) {
            accountDao.upsert(account.toEntity())
        }
    }
