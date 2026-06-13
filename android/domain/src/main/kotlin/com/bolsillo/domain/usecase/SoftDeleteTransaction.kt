package com.bolsillo.domain.usecase

import com.bolsillo.domain.port.TransactionRepository
import javax.inject.Inject

/**
 * FR 12 — soft delete a single transaction, or the whole transfer group if
 * the row carries a [transferGroupId]. Article III: no hard deletes.
 */
class SoftDeleteTransaction
    @Inject
    constructor(private val transactions: TransactionRepository) {
        suspend operator fun invoke(
            id: String,
            now: Long = System.currentTimeMillis(),
        ) {
            val target = transactions.getById(id) ?: return
            val group = target.transferGroupId
            if (group != null) {
                transactions.softDeleteGroup(group, now)
            } else {
                transactions.softDelete(id, now)
            }
        }
    }
