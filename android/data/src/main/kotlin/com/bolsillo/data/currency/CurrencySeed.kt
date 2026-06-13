package com.bolsillo.data.currency

import com.bolsillo.domain.model.Currency

/**
 * Seed catalog. USD and COP are essential (non-removable) and enabled by default
 * (Constitution Article VII). Other ISO 4217 currencies can be added later.
 * decimalDigits follow ISO 4217 (USD=2, COP=2).
 */
object CurrencySeed {
    val essentials: List<Currency> =
        listOf(
            Currency(code = "USD", symbol = "$", decimalDigits = 2, isEnabled = true, isEssential = true),
            Currency(code = "COP", symbol = "$", decimalDigits = 2, isEnabled = true, isEssential = true),
        )
}
