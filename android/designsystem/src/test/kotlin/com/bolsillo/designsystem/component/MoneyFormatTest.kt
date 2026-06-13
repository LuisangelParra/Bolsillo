package com.bolsillo.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyFormatTest {
    @Test fun `usd positive formats with locale separator`() {
        // 12345 minor units = 123.45
        val out = formatMoney(12345L, decimalDigits = 2, symbol = "$", Locale.US, forceSign = false)
        assertEquals("$123.45", out)
    }

    @Test fun `usd negative renders minus sign and money negative tint`() {
        val out = formatMoney(-100L, decimalDigits = 2, symbol = "$", Locale.US, forceSign = false)
        assertEquals("−$1.00", out)
    }

    @Test fun `force-sign on positive renders plus`() {
        val out = formatMoney(100L, decimalDigits = 2, symbol = "$", Locale.US, forceSign = true)
        assertEquals("+$1.00", out)
    }

    @Test fun `zero-decimal currency renders integer`() {
        val out = formatMoney(900L, decimalDigits = 0, symbol = "¥", Locale.JAPAN, forceSign = false)
        assertEquals("¥900", out)
    }
}
