package com.bolsillo.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bolsillo.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(name = "idx_tx_account_deleted", value = ["account_id", "deleted_at"]),
        Index(name = "idx_tx_transfer_group", value = ["transfer_group_id"]),
        Index(name = "idx_tx_occurred", value = ["occurred_at"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    val type: TransactionType,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: String,
    @ColumnInfo(name = "amount_base_minor") val amountBaseMinor: Long,
    @ColumnInfo(name = "fx_rate_millis") val fxRateMillis: Long,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    val merchant: String?,
    val note: String?,
    @ColumnInfo(name = "occurred_at") val occurredAt: Long,
    @ColumnInfo(name = "transfer_group_id") val transferGroupId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
