import Foundation

public enum TransactionType: String, Sendable {
    case expense
    case income
    case transfer
}

/// A financial entry. Amounts are `Money` (integer minor units).
///
/// - `amountBase` and `fxRateMillis` are frozen at creation (Article III) and never recomputed.
/// - `deletedAt` implements soft delete (trash) — there are NO hard deletes (Article III).
/// - `transferGroupId` links the two legs of a transfer (double entry).
public struct Transaction: Equatable, Sendable, Identifiable {
    public let id: String
    public let accountId: String
    public let type: TransactionType
    public let amount: Money
    public let currencyCode: String
    public let amountBase: Money
    public let fxRateMillis: Int
    public let categoryId: String?
    public let merchant: String?
    public let note: String?
    public let occurredAt: Date
    public let transferGroupId: String?
    public let createdAt: Date
    public let updatedAt: Date
    public let deletedAt: Date?

    public init(
        id: String,
        accountId: String,
        type: TransactionType,
        amount: Money,
        currencyCode: String,
        amountBase: Money,
        fxRateMillis: Int,
        categoryId: String? = nil,
        merchant: String? = nil,
        note: String? = nil,
        occurredAt: Date,
        transferGroupId: String? = nil,
        createdAt: Date,
        updatedAt: Date,
        deletedAt: Date? = nil
    ) {
        self.id = id
        self.accountId = accountId
        self.type = type
        self.amount = amount
        self.currencyCode = currencyCode
        self.amountBase = amountBase
        self.fxRateMillis = fxRateMillis
        self.categoryId = categoryId
        self.merchant = merchant
        self.note = note
        self.occurredAt = occurredAt
        self.transferGroupId = transferGroupId
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.deletedAt = deletedAt
    }

    public var isDeleted: Bool { deletedAt != nil }

    /// Soft delete (trash) — returns a copy with `deletedAt` set. Never a hard delete.
    public func markDeleted(at date: Date) -> Transaction {
        Transaction(
            id: id, accountId: accountId, type: type, amount: amount, currencyCode: currencyCode,
            amountBase: amountBase, fxRateMillis: fxRateMillis, categoryId: categoryId, merchant: merchant,
            note: note, occurredAt: occurredAt, transferGroupId: transferGroupId, createdAt: createdAt,
            updatedAt: date, deletedAt: date
        )
    }

    /// Restore from trash — returns a copy with `deletedAt` cleared.
    public func restored() -> Transaction {
        Transaction(
            id: id, accountId: accountId, type: type, amount: amount, currencyCode: currencyCode,
            amountBase: amountBase, fxRateMillis: fxRateMillis, categoryId: categoryId, merchant: merchant,
            note: note, occurredAt: occurredAt, transferGroupId: transferGroupId, createdAt: createdAt,
            updatedAt: updatedAt, deletedAt: nil
        )
    }
}
