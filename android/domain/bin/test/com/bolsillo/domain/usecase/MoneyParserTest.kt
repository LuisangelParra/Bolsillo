package com.bolsillo.domain.usecase

import com.bolsillo.domain.model.AmountInput
import com.bolsillo.domain.model.Currency
import com.bolsillo.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyParserTest {
    private val usd = Currency("USD", "$", decimalDigits = 2, isEnabled = true, isEssential = true)
    private val jpy = Currency("JPY", "¥", decimalDigits = 0, isEnabled = true, isEssential = false)
    private val parser = MoneyParser()

    @Test fun `empty input yields zero`() {
        assertEquals(Money.ZERO, parser.parse(AmountInput.EMPTY, usd))
    }

    @Test fun `leading zeros are stripped`() {
        assertEquals(Money(1234L), parser.parse(AmountInput("0001234"), usd))
    }

    @Test fun `digits become minor units verbatim`() {
        assertEquals(Money(12345L), parser.parse(AmountInput("12345"), usd))
    }

    @Test fun `zero decimal currency keeps integer minor units`() {
        assertEquals(Money(900L), parser.parse(AmountInput("900"), jpy))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non-digit input is rejected`() {
        parser.parse("12a3", usd)
    }
}
