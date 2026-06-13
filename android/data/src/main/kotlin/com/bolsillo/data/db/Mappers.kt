package com.bolsillo.data.db

import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.data.db.entity.CategoryEntity
import com.bolsillo.data.db.entity.TransactionEntity
import com.bolsillo.domain.model.Account
import com.bolsillo.domain.model.Category
import com.bolsillo.domain.model.Money
import com.bolsillo.domain.model.Transaction

// Entity ↔ domain conversions. Keep Room types out of :domain (Article VIII).

fun AccountEntity.toDomain(): Account =
    Account(
        id = id,
        name = name,
        type = type,
        currencyCode = currencyCode,
        initialBalance = Money(initialBalanceMinor),
        icon = icon,
        color = color,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun Account.toEntity(): AccountEntity =
    AccountEntity(
        id = id,
        name = name,
        type = type,
        currencyCode = currencyCode,
        initialBalanceMinor = initialBalance.minorUnits,
        icon = icon,
        color = color,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        accountId = accountId,
        type = type,
        amount = Money(amountMinor),
        currencyCode = currencyCode,
        amountBase = Money(amountBaseMinor),
        fxRateMillis = fxRateMillis,
        categoryId = categoryId,
        merchant = merchant,
        note = note,
        occurredAt = occurredAt,
        transferGroupId = transferGroupId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        accountId = accountId,
        type = type,
        amountMinor = amount.minorUnits,
        currencyCode = currencyCode,
        amountBaseMinor = amountBase.minorUnits,
        fxRateMillis = fxRateMillis,
        categoryId = categoryId,
        merchant = merchant,
        note = note,
        occurredAt = occurredAt,
        transferGroupId = transferGroupId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        nameKey = nameKey,
        icon = icon,
        colorToken = colorToken,
    )

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id,
        nameKey = nameKey,
        icon = icon,
        colorToken = colorToken,
    )
