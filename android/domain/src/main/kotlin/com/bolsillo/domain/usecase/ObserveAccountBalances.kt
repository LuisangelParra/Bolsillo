package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.Money
import com.bolsillo.domain.port.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * FR 6,15 — derived balances stream for the UI. Per Invariant 2 the
 * repository must compute these by SUM, never read a cached column.
 */
class ObserveAccountBalances
    @Inject
    constructor(private val accounts: AccountRepository) {
        operator fun invoke(): Flow<Map<String, Money>> = accounts.observeBalances()

        fun forAccount(accountId: String): Flow<Money> = accounts.observeBalance(accountId)
    }
