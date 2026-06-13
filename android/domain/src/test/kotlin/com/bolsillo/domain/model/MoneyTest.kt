package com.bolsillo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun `minor units round trip as integer Long`() {
        // Constitution Article III: money is integer minor units, never float/double.
        // The type itself enforces Long; this guards exact round-tripping of large values.
        val money = Money(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, money.minorUnits)
    }

    @Test
    fun `addition is exact`() {
        assertEquals(Money(30L), Money(10L) + Money(20L))
    }

    @Test
    fun `subtraction and negation are exact`() {
        assertEquals(Money(-5L), Money(10L) - Money(15L))
        assertEquals(Money(-10L), -Money(10L))
    }

    @Test
    fun `zero constant`() {
        assertTrue(Money.ZERO.isZero)
        assertEquals(Money(0L), Money.ZERO)
    }
}
