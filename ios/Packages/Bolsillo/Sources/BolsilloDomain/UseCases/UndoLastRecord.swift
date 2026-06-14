import Foundation

/// Soft-deletes the most recently saved record — either a single transaction or
/// both legs of a transfer group — without hard-deleting any data
/// (Constitution Article III — no hard deletes).
///
/// The caller supplies a `SavedRef` obtained from the previous `RecordTransaction`
/// or `RecordTransfer` call. This use case is designed for the ≤5 s undo affordance
/// in the fast-recording flow (spec 001-R14).
public struct UndoLastRecord: Sendable {
    private let transactions: any TransactionRepository

    public init(transactions: some TransactionRepository) {
        self.transactions = transactions
    }

    /// Soft-delete the referenced transaction(s).
    ///
    /// - Parameters:
    ///   - lastSaved: The `SavedRef` returned by the prior record operation.
    ///   - now: The deletion timestamp (injected for testability).
    public func callAsFunction(_ lastSaved: SavedRef, now: Date) async {
        switch lastSaved {
        case .single(let id):
            await transactions.softDelete(id: id, deletedAt: now)
        case .group(let groupId):
            await transactions.softDeleteGroup(transferGroupId: groupId, deletedAt: now)
        }
    }
}
