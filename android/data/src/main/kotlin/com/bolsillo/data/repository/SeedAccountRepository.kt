package com.bolsillo.data.repository

import com.bolsillo.data.currency.CurrencySeed
import com.bolsillo.domain.model.Account
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.port.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legacy placeholder. The production binding in [com.bolsillo.data.di.DataModule]
 * is [RoomAccountRepository] — never this class. Kept so a missing Room dependency
 * doesn't make the InMemory test compile-fail.
 */
@Singleton
class SeedAccountRepository
    @Inject
    constructor() : AccountRepository {
        override fun observeCurrencies(): Flow<List<Currency>> = flowOf(CurrencySeed.essentials)

        override fun observeAccounts(): Flow<List<Account>> = flowOf(emptyList())

        override suspend fun getById(id: String): Account? = null

        override fun observeBalance(accountId: String): Flow<Money> = flowOf(Money.ZERO)

        override fun observeBalances(): Flow<Map<String, Money>> = flowOf(emptyMap())

        override suspend fun upsert(account: Account) = Unit
    }
