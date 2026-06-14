package com.bolsillo.domain.model

import java.math.BigInteger

/**
 * Banker's half-up rounding policy (Article III). For same-currency 001 this is
 * an identity — the harness exists from day one so cross-currency (E8) drops
 * straight into a tested code path.
 *
 * Rounds the result of `valueMinor * fxRateMillis / 1000` to the nearest integer,
 * ties going to the even result.
 */
object MoneyRounding {
    /** Convert a signed minor amount by [fxRateMillis] (×1000). */
    fun convert(
        valueMinor: Long,
        fxRateMillis: Long,
    ): Long {
        if (fxRateMillis == 1000L) return valueMinor
        val product = BigInteger.valueOf(valueMinor).multiply(BigInteger.valueOf(fxRateMillis))
        val divisor = BigInteger.valueOf(1000L)
        return divideHalfEven(product, divisor).toLong()
    }

    private fun divideHalfEven(
        numerator: BigInteger,
        divisor: BigInteger,
    ): BigInteger {
        val (quot, rem) = numerator.divideAndRemainder(divisor)
        if (rem.signum() == 0) return quot
        val twiceRem = rem.abs().shiftLeft(1)
        val cmp = twiceRem.compareTo(divisor.abs())
        val sign = if (numerator.signum() * divisor.signum() < 0) -1 else 1
        return when {
            cmp < 0 -> quot
            cmp > 0 -> quot.add(BigInteger.valueOf(sign.toLong()))
            else -> if (quot.testBit(0)) quot.add(BigInteger.valueOf(sign.toLong())) else quot
        }
    }
}
