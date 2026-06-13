package com.bolsillo.domain.usecase

import com.bolsillo.domain.port.TransactionRepository
import javax.inject.Inject

/**
 * Reference to the most recent save so [UndoLastRecord] knows what to revert.
 * For a transfer this is the [transferGroupId]; for a single leg it's the [id].
 */
sealed class SavedRef {
    data class Single(val id: String) : SavedRef()

    data class Group(val transferGroupId: String) : SavedRef()
}

/**
 * FR 6,7 — undo the just-created record/group via soft delete (research R6).
 * Same code path as trash; the row remains restorable. Article III: no hard
 * deletes on user action.
 */
class UndoLastRecord
    @Inject
    constructor(private val transactions: TransactionRepository) {
        suspend operator fun invoke(
            lastSaved: SavedRef,
            now: Long = System.currentTimeMillis(),
        ) {
            when (lastSaved) {
                is SavedRef.Single -> transactions.softDelete(lastSaved.id, now)
                is SavedRef.Group -> transactions.softDeleteGroup(lastSaved.transferGroupId, now)
            }
        }
    }
