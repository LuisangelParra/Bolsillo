package com.bolsillo.domain.model

/**
 * Monetary amount in **integer minor units** (e.g. cents). The number of minor
 * units a currency uses is defined by [Currency.decimalDigits].
 *
 * Constitution Article III: money is NEVER represented as float/double. This type
 * has no floating-point constructor or accessor on purpose.
 */
@JvmInline
value class Money(
    val minorUnits: Long,
) {
    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money = Money(minorUnits - other.minorUnits)

    operator fun unaryMinus(): Money = Money(-minorUnits)

    val isZero: Boolean get() = minorUnits == 0L

    companion object {
        val ZERO = Money(0L)
    }
}
