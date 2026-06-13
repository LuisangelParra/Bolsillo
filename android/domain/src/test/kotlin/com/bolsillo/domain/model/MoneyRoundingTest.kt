package com.bolsillo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyRoundingTest {
    @Test fun `same currency identity - rate 1000 is no-op`() {
        assertEquals(12345L, MoneyRounding.convert(12345L, 1000L))
        assertEquals(-12345L, MoneyRounding.convert(-12345L, 1000L))
    }

    @Test fun `banker's half-up - half rounds to even`() {
        // value=5 * rate=100 /1000 = 0.5 → even → 0
        assertEquals(0L, MoneyRounding.convert(5L, 100L))
        // value=15 * 100 /1000 = 1.5 → even → 2
        assertEquals(2L, MoneyRounding.convert(15L, 100L))
        // value=25 * 100 /1000 = 2.5 → even → 2
        assertEquals(2L, MoneyRounding.convert(25L, 100L))
        // value=35 * 100 /1000 = 3.5 → even → 4
        assertEquals(4L, MoneyRounding.convert(35L, 100L))
    }

    @Test fun `non-tie rounds normally`() {
        // 23 * 100 /1000 = 2.3 → 2
        assertEquals(2L, MoneyRounding.convert(23L, 100L))
        // 27 * 100 /1000 = 2.7 → 3
        assertEquals(3L, MoneyRounding.convert(27L, 100L))
    }

    @Test fun `negative values follow the same policy`() {
        // -15 * 100 /1000 = -1.5 → even → -2
        assertEquals(-2L, MoneyRounding.convert(-15L, 100L))
    }
}
