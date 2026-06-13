package com.bolsillo.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bolsillo.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deleted_at IS NULL ORDER BY occurred_at DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 1")
    suspend fun lastUsed(): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE transfer_group_id = :groupId")
    suspend fun byTransferGroup(groupId: String): List<TransactionEntity>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET deleted_at = :deletedAt, updated_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(
        id: String,
        deletedAt: Long,
    )

    @Query("UPDATE transactions SET deleted_at = NULL, updated_at = :updatedAt WHERE id = :id")
    suspend fun restore(
        id: String,
        updatedAt: Long,
    )

    @Query(
        "UPDATE transactions SET deleted_at = :deletedAt, updated_at = :deletedAt " +
            "WHERE transfer_group_id = :groupId",
    )
    suspend fun softDeleteGroup(
        groupId: String,
        deletedAt: Long,
    )

    @Query(
        "UPDATE transactions SET deleted_at = NULL, updated_at = :updatedAt " +
            "WHERE transfer_group_id = :groupId",
    )
    suspend fun restoreGroup(
        groupId: String,
        updatedAt: Long,
    )

    /**
     * Derived balance contribution for one account: SUM of signed minor amounts over
     * non-deleted legs. Combined with [AccountEntity.initialBalanceMinor] by the
     * repository (Invariant 2 — no stored balance column).
     */
    @Query(
        "SELECT COALESCE(SUM(amount_minor), 0) FROM transactions " +
            "WHERE account_id = :accountId AND deleted_at IS NULL",
    )
    fun observeSignedSum(accountId: String): Flow<Long>

    @Query(
        "SELECT account_id AS accountId, COALESCE(SUM(amount_minor), 0) AS sum " +
            "FROM transactions WHERE deleted_at IS NULL GROUP BY account_id",
    )
    fun observeSignedSums(): Flow<List<AccountSumRow>>
}

data class AccountSumRow(
    val accountId: String,
    val sum: Long,
)
