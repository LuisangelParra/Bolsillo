package com.bolsillo.domain.port

import com.bolsillo.domain.model.Currency
import kotlinx.coroutines.flow.Flow

/**
 * Domain port for accounts and the currency catalog. Implemented in the data layer.
 */
interface AccountRepository {
    /** Enabled currencies; essential ones (USD, COP) are always present. */
    fun observeCurrencies(): Flow<List<Currency>>
}
