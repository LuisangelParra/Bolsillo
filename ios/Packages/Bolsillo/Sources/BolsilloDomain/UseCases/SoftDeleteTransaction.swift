import Foundation

/// Soft-deletes a transaction (moves it to trash). If the transaction belongs to a
/// transfer group, both legs are soft-deleted atomically to preserve the double-entry
/// invariant (Constitution Article III — Invariants 6 & 7).
///
/// There are NO hard deletes (Constitution Article III).
public struct SoftDeleteTransaction: Sendable {
    private let transactions: any TransactionRepository

    public init(transactions: some TransactionRepository) {
        self.transactions = transactions
    }

    /// Soft-delete the transaction identified by `id`.
    ///
    /// - Parameters:
    ///   - id: The transaction to delete.
    ///   - now: The deletion timestamp (injected for testability).
    public func callAsFunction(id: String, now: Date) async {
        if let tx = await transactions.getById(id), let groupId = tx.transferGroupId {
            // Transfer: soft-delete the whole group atomically (Invariant 7)
            await transactions.softDeleteGroup(transferGroupId: groupId, deletedAt: now)
        } else {
            await transactions.softDelete(id: id, deletedAt: now)
        }
    }
}
