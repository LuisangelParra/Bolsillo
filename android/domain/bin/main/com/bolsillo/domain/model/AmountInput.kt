package com.bolsillo.domain.model

/**
 * Raw keypad input — a digit accumulator. NEVER passes through Double/Float;
 * MoneyParser produces a [Money] in integer minor units honoring
 * [Currency.decimalDigits].
 */
data class AmountInput(val digits: String = "") {
    fun append(d: Char): AmountInput {
        require(d.isDigit()) { "AmountInput accepts digits only" }
        if (d == '0' && digits == "0") return this
        if (digits == "0" && d != '0') return AmountInput(d.toString())
        return AmountInput((digits + d).take(MAX_DIGITS))
    }

    fun appendBatch(batch: String): AmountInput {
        var acc = this
        for (c in batch) {
            if (c.isDigit()) acc = acc.append(c)
        }
        return acc
    }

    fun backspace(): AmountInput {
        if (digits.length <= 1) return EMPTY
        return AmountInput(digits.dropLast(1))
    }

    val isEmpty: Boolean get() = digits.isEmpty() || digits == "0"

    companion object {
        const val MAX_DIGITS = 15
        val EMPTY = AmountInput("")
    }
}
