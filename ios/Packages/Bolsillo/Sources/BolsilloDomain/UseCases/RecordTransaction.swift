import Foundation

/// Records a single expense, income, or single-leg transfer draft as a persisted
/// `Transaction`. Signs the amount according to type (Constitution Article III — Invariant 1)
/// and freezes `fxRateMillis` / `amountBase` at creation (Invariant 8).
///
/// For transfer pairs use `RecordTransfer` instead, which creates both legs atomically.
public struct RecordTransaction: Sendable {
    private let transactions: any TransactionRepository

    public init(transactions: some TransactionRepository) {
        self.transactions = transactions
    }

    /// Persist the draft and return the saved `Transaction`.
    ///
    /// - Sign rule (Invariant 1): expense → negative, income → positive,
    ///   transfer (single leg) → negative (source). For atomic transfer pairs
    ///   use `RecordTransfer`.
    /// - `fxRateMillis` is frozen to 1000 (same-currency 001); `amountBase == amount`.
    public func callAsFunction(_ draft: TransactionDraft) async -> Transaction {
        let now = Date()
        let signedAmount: Money
        switch draft.type {
        case .expense:  signedAmount = -draft.amount
        case .income:   signedAmount =  draft.amount
        case .transfer: signedAmount = -draft.amount   // source leg only
        }

        let tx = Transaction(
            id: UUID().uuidString,
            accountId: draft.accountId,
            type: draft.type,
            amount: signedAmount,
            currencyCode: draft.currencyCode,
            amountBase: signedAmount,   // frozen at creation (Invariant 8)
            fxRateMillis: 1000,         // frozen at creation — same-currency (Invariant 8)
            categoryId: draft.categoryId,
            merchant: draft.merchant,
            note: draft.note,
            occurredAt: draft.occurredAt,
            transferGroupId: nil,
            createdAt: now,
            updatedAt: now
        )
        await transactions.upsert(tx)
        return tx
    }
}
