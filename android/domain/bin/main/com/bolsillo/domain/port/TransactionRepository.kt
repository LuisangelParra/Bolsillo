package com.bolsillo.domain.port

import com.bolsillo.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Domain port for transactions. Implemented in the data layer; the domain
 * depends only on this interface (Article VIII).
 *
 * Removal is [softDelete] only (Article III — no hard deletes). Multi-row
 * methods (transfer pair) MUST be atomic — all-or-nothing.
 */
interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>

    suspend fun getById(id: String): Transaction?

    suspend fun upsert(transaction: Transaction)

    /** Move to trash by setting deletedAt. Never physically deletes. */
    suspend fun softDelete(
        id: String,
        deletedAt: Long,
    )

    /** Restore a soft-deleted transaction from trash. */
    suspend fun restore(id: String)

    /** Most recent non-deleted transaction; used by last-used fallback. */
    suspend fun lastUsed(): Transaction?

    /** Atomic pair write: both legs persisted in one DB transaction. */
    suspend fun upsertTransfer(
        legSource: Transaction,
        legDest: Transaction,
    )

    /** Soft-delete every leg sharing the given transferGroupId. */
    suspend fun softDeleteGroup(
        transferGroupId: String,
        deletedAt: Long,
    )

    /** Restore every leg sharing the given transferGroupId. */
    suspend fun restoreGroup(transferGroupId: String)
}
