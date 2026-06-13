package com.bolsillo.domain.usecase

import com.bolsillo.domain.port.TransactionRepository
import javax.inject.Inject

/** FR 13 — restore from trash. Transfer legs come back together as a pair. */
class RestoreTransaction
    @Inject
    constructor(private val transactions: TransactionRepository) {
        suspend operator fun invoke(id: String) {
            val target = transactions.getById(id) ?: return
            val group = target.transferGroupId
            if (group != null) {
                transactions.restoreGroup(group)
            } else {
                transactions.restore(id)
            }
        }
    }
