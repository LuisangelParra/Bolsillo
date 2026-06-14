import Foundation

/// Restores a soft-deleted transaction from trash. If the transaction belongs to a
/// transfer group, both legs are restored atomically to preserve the double-entry
/// invariant (Constitution Article III — Invariants 6 & 7).
public struct RestoreTransaction: Sendable {
    private let transactions: any TransactionRepository

    public init(transactions: some TransactionRepository) {
        self.transactions = transactions
    }

    /// Restore the transaction identified by `id`.
    ///
    /// - Parameter id: The transaction to restore. If it belongs to a transfer group,
    ///   the entire group is restored.
    public func callAsFunction(id: String) async {
        if let tx = await transactions.getById(id), let groupId = tx.transferGroupId {
            // Transfer: restore the whole group atomically (Invariant 7)
            await transactions.restoreGroup(transferGroupId: groupId)
        } else {
            await transactions.restore(id: id)
        }
    }
}
