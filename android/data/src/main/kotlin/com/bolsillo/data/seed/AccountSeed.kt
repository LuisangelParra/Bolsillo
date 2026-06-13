package com.bolsillo.data.seed

import com.bolsillo.data.db.entity.AccountEntity
import com.bolsillo.domain.model.AccountType

/**
 * First-launch seed so the app has somewhere to record into (FR1). Name uses
 * the localization key — UI resolves it at render time (Article VII).
 */
object AccountSeed {
    const val DEFAULT_CASH_ACCOUNT_ID = "acc-default-cash"

    fun defaultCash(now: Long): AccountEntity =
        AccountEntity(
            id = DEFAULT_CASH_ACCOUNT_ID,
            name = "account.default.cash",
            type = AccountType.CASH,
            currencyCode = "USD",
            initialBalanceMinor = 0L,
            icon = "wallet",
            color = 0xFF7C5CF0L,
            archived = false,
            createdAt = now,
            updatedAt = now,
        )
}
