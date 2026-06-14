import Foundation

/// Records a transfer as two linked `Transaction` legs in a single atomic operation
/// (Constitution Article III — Invariant 6: transfers are always double-entry pairs).
///
/// Invariant enforcement:
/// - Source leg is negative, destination leg is positive (Invariant 1).
/// - Both legs share the same `transferGroupId` (Invariant 6).
/// - Cross-currency transfers are rejected in feature 001; same-currency only (E8 deferred).
/// - `fxRateMillis` is frozen to 1000 and `amountBase == amount` (Invariant 8).
public struct RecordTransfer: Sendable {
    private let transactions: any TransactionRepository
    private let accounts: any AccountRepository

    public init(transactions: some TransactionRepository, accounts: some AccountRepository) {
        self.transactions = transactions
        self.accounts = accounts
    }

    /// Atomically persist a transfer and return both legs.
    ///
    /// - Parameters:
    ///   - sourceAccountId: The account being debited.
    ///   - destAccountId: The account being credited.
    ///   - amount: The transfer magnitude (positive minor-unit value).
    ///   - occurredAt: The effective date of the transfer.
    /// - Throws: `TransferError.sameAccount` if both ids are identical.
    ///           `TransferError.crossCurrency` if the accounts use different currencies.
    public func callAsFunction(
        sourceAccountId: String,
        destAccountId: String,
        amount: Money,
        occurredAt: Date
    ) async throws -> TransferPair {
        guard sourceAccountId != destAccountId else { throw TransferError.sameAccount }

        // Resolve source currency (used for both legs in same-currency 001)
        let srcAccount = await accounts.getById(sourceAccountId)
        let dstAccount = await accounts.getById(destAccountId)

        if let src = srcAccount, let dst = dstAccount, src.currencyCode != dst.currencyCode {
            throw TransferError.crossCurrency
        }

        let currencyCode = srcAccount?.currencyCode ?? "USD"
        let groupId = UUID().uuidString
        let now = Date()

        let legSource = Transaction(
            id: UUID().uuidString,
            accountId: sourceAccountId,
            type: .transfer,
            amount: -amount,            // negative: source is debited (Invariant 1)
            currencyCode: currencyCode,
            amountBase: -amount,        // frozen at creation (Invariant 8)
            fxRateMillis: 1000,         // same-currency (Invariant 8)
            categoryId: "transfer",
            merchant: nil,
            note: nil,
            occurredAt: occurredAt,
            transferGroupId: groupId,
            createdAt: now,
            updatedAt: now
        )

        let legDest = Transaction(
            id: UUID().uuidString,
            accountId: destAccountId,
            type: .transfer,
            amount: amount,             // positive: destination is credited (Invariant 1)
            currencyCode: currencyCode,
            amountBase: amount,         // frozen at creation (Invariant 8)
            fxRateMillis: 1000,         // same-currency (Invariant 8)
            categoryId: "transfer",
            merchant: nil,
            note: nil,
            occurredAt: occurredAt,
            transferGroupId: groupId,
            createdAt: now,
            updatedAt: now
        )

        await transactions.upsertTransfer(legSource: legSource, legDest: legDest)
        return TransferPair(legSource: legSource, legDest: legDest)
    }
}
