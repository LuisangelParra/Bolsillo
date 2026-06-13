package com.bolsillo.domain.port

import com.bolsillo.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Domain port for transactions. Implemented in the data layer; the domain depends
 * only on this interface (Constitution Article VIII).
 *
 * Note: there is intentionally NO hard-delete method. Removal is [softDelete] only
 * (Constitution Article III — no hard deletes).
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
}
