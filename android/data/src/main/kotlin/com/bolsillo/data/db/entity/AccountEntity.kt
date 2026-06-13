package com.bolsillo.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bolsillo.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: AccountType,
    @ColumnInfo(name = "currency_code") val currencyCode: String,
    @ColumnInfo(name = "initial_balance_minor") val initialBalanceMinor: Long,
    val icon: String,
    val color: Long,
    val archived: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
