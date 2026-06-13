package com.bolsillo.data.repository

import com.bolsillo.data.currency.CurrencySeed
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.port.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/** Placeholder account/currency source backed by the essential currency seed. */
@Singleton
class SeedAccountRepository
    @Inject
    constructor() : AccountRepository {
        override fun observeCurrencies(): Flow<List<Currency>> = flowOf(CurrencySeed.essentials)
    }
