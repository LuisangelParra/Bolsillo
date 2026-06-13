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
 * Placeholder repository (in-memory) until Room/SQLCipher persistence is implemented.
 * Demonstrates the soft-delete contract: [softDelete] sets deletedAt; nothing is ever
 * physically removed (Constitution Article III).
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
    }
