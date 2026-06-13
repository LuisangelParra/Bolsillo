package com.bolsillo.domain.model

/**
 * Account aggregate. Current balance is **derived**, never stored
 * (data-model.md Invariant 2). Only the opening [initialBalance] lives on the row.
 */
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val initialBalance: Money,
    val icon: String,
    val color: Long,
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
