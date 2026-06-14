package com.bolsillo.domain.model

/**
 * A currency in the catalog. [isEssential] currencies (USD, COP) are preloaded and
 * cannot be removed (Constitution Article VII). [decimalDigits] drives minor-unit handling.
 */
data class Currency(
    val code: String,
    val symbol: String,
    val decimalDigits: Int,
    val isEnabled: Boolean,
    val isEssential: Boolean,
)
