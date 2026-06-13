package com.bolsillo.data.repository

import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legacy placeholder kept only so the InMemory test still exercises the
 * soft-delete contract. The production binding in [com.bolsillo.data.di.DataModule]
 * is [RoomTransactionRepository] — never this class.
 *
 * Soft delete (Article III): nothing is ever physically removed.
 */
@Singleton
class InMemoryTransactionRepository
    @Inject
    constructor() : TransactionRepository {
        private val state = MutableStateFlow<Map<String, Transaction>>(emptyMap())

        override fun observeAll(): Flow<List<Transaction>> = state.map { byId -> byId.values.filter { !it.isDeleted } }

        override suspend fun getById(id: String): Transaction? = state.value[id]

        override suspend fun upsert(transaction: Transaction) {
            state.update { it + (transaction.id to transaction) }
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) {
            state.update { byId ->
                val existing = byId[id] ?: return@update byId
                byId + (id to existing.copy(deletedAt = deletedAt, updatedAt = deletedAt))
            }
        }

        override suspend fun restore(id: String) {
            state.update { byId ->
                val existing = byId[id] ?: return@update byId
                byId + (id to existing.copy(deletedAt = null))
            }
        }

        override suspend fun lastUsed(): Transaction? =
            state.value.values.filter { !it.isDeleted }.maxByOrNull { it.createdAt }

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) {
            state.update { it + (legSource.id to legSource) + (legDest.id to legDest) }
        }

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) {
            state.update { byId ->
                byId.mapValues { (_, t) ->
                    if (t.transferGroupId == transferGroupId) {
                        t.copy(deletedAt = deletedAt, updatedAt = deletedAt)
                    } else {
                        t
                    }
                }
            }
        }

        override suspend fun restoreGroup(transferGroupId: String) {
            state.update { byId ->
                byId.mapValues { (_, t) ->
                    if (t.transferGroupId == transferGroupId) t.copy(deletedAt = null) else t
                }
            }
        }
    }
