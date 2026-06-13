package com.bolsillo.domain.port

import com.bolsillo.domain.model.Account
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.model.Money
import kotlinx.coroutines.flow.Flow

/**
 * Domain port for accounts and the currency catalog. Implemented in the data layer.
 *
 * Balance is **derived** per data-model.md Invariant 2 — implementations MUST
 * NOT return a cached/stored value. Use SUM(amount) over non-deleted legs.
 */
interface AccountRepository {
    /** Enabled currencies; essential ones (USD, COP) are always present. */
    fun observeCurrencies(): Flow<List<Currency>>

    /** Active (non-archived) accounts. */
    fun observeAccounts(): Flow<List<Account>>

    suspend fun getById(id: String): Account?

    /** Derived balance for a single account: initialBalance + SUM(signed, non-deleted). */
    fun observeBalance(accountId: String): Flow<Money>

    /** Derived balances for all active accounts, keyed by account id. */
    fun observeBalances(): Flow<Map<String, Money>>

    /** Used by AccountSeed when the DB is empty. */
    suspend fun upsert(account: Account)
}
