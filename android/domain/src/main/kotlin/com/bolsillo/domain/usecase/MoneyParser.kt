package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.AmountInput
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.model.Money
import javax.inject.Inject

/**
 * Pure-integer keypad → [Money]. NO float/double path (Article III). The user
 * enters minor units directly (typing "100" on a 2-decimal currency means 1.00),
 * matching the canonical mock's amount strip.
 */
class MoneyParser
    @Inject
    constructor() {
    fun parse(
        input: AmountInput,
        currency: Currency,
    ): Money = parse(input.digits, currency)

    fun parse(
        digits: String,
        currency: Currency,
    ): Money {
        val clean = digits.trimStart('0')
        if (clean.isEmpty()) return Money.ZERO
        require(clean.all { it.isDigit() }) { "MoneyParser requires digit-only input" }
        require(currency.decimalDigits in 0..6) {
            "Unsupported decimalDigits=${currency.decimalDigits}"
        }
        return Money(clean.toLong())
    }

    /** Render a [Money] as the digit string the keypad would produce. */
    fun formatDigits(money: Money): String = if (money.minorUnits <= 0L) "" else money.minorUnits.toString()
}
