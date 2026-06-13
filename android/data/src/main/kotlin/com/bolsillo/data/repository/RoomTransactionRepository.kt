package com.bolsillo.data.repository

import androidx.room.withTransaction
import com.bolsillo.data.db.BolsilloDatabase
import com.bolsillo.data.db.dao.TransactionDao
import com.bolsillo.data.db.toDomain
import com.bolsillo.data.db.toEntity
import com.bolsillo.domain.model.Transaction
import com.bolsillo.domain.port.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed transaction repository. Multi-row writes (the transfer pair,
 * group soft-delete/restore) run inside [BolsilloDatabase.withTransaction] for
 * atomicity (Article III / Invariant 9 — all-or-nothing).
 */
@Singleton
class RoomTransactionRepository
    @Inject
    constructor(
        private val db: BolsilloDatabase,
        private val dao: TransactionDao,
    ) : TransactionRepository {
        override fun observeAll(): Flow<List<Transaction>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

        override suspend fun getById(id: String): Transaction? = dao.getById(id)?.toDomain()

        override suspend fun upsert(transaction: Transaction) {
            dao.upsert(transaction.toEntity())
        }

        override suspend fun softDelete(
            id: String,
            deletedAt: Long,
        ) {
            dao.softDelete(id, deletedAt)
        }

        override suspend fun restore(id: String) {
            dao.restore(id, updatedAt = System.currentTimeMillis())
        }

        override suspend fun lastUsed(): Transaction? = dao.lastUsed()?.toDomain()

        override suspend fun upsertTransfer(
            legSource: Transaction,
            legDest: Transaction,
        ) {
            db.withTransaction {
                dao.upsertAll(listOf(legSource.toEntity(), legDest.toEntity()))
            }
        }

        override suspend fun softDeleteGroup(
            transferGroupId: String,
            deletedAt: Long,
        ) {
            db.withTransaction { dao.softDeleteGroup(transferGroupId, deletedAt) }
        }

        override suspend fun restoreGroup(transferGroupId: String) {
            db.withTransaction { dao.restoreGroup(transferGroupId, System.currentTimeMillis()) }
        }
    }
