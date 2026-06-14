import Foundation

/// Persists an edited `Transaction`, preserving the frozen fields that must never be
/// recomputed (Constitution Article III — Invariant 8).
///
/// The caller supplies a fully-formed `Transaction` (e.g. from the edit screen).
/// This use case stamps a fresh `updatedAt` and delegates to the repository.
/// Balance re-derivation is automatic via `SUM(transactions)` (Invariant 2).
///
/// Fields never recomputed on edit:
/// - `fxRateMillis` — frozen to the rate at creation
/// - `amountBase`   — frozen to the base-currency amount at creation
public struct EditTransaction: Sendable {
    private let transactions: any TransactionRepository

    public init(transactions: some TransactionRepository) {
        self.transactions = transactions
    }

    /// Persist the updated transaction.
    ///
    /// - Parameter updated: The edited transaction. `fxRateMillis` and `amountBase`
    ///   are carried through unchanged from the caller — this use case does NOT
    ///   recompute them.
    public func callAsFunction(_ updated: Transaction) async {
        let now = Date()
        let withTimestamp = Transaction(
            id: updated.id,
            accountId: updated.accountId,
            type: updated.type,
            amount: updated.amount,
            currencyCode: updated.currencyCode,
            amountBase: updated.amountBase,       // preserved — never recomputed (Invariant 8)
            fxRateMillis: updated.fxRateMillis,   // preserved — never recomputed (Invariant 8)
            categoryId: updated.categoryId,
            merchant: updated.merchant,
            note: updated.note,
            occurredAt: updated.occurredAt,
            transferGroupId: updated.transferGroupId,
            createdAt: updated.createdAt,
            updatedAt: now
        )
        await transactions.upsert(withTimestamp)
    }
}
