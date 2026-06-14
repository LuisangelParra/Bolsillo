import Foundation

/// A draft submitted by the UI to the recording use case. `amount` is a magnitude
/// (always positive); the sign is applied by `RecordTransaction` based on `type`
/// (Constitution Article III — Invariant 1).
public struct TransactionDraft: Sendable {
    public let type: TransactionType
    public let accountId: String
    public let amount: Money          // magnitude (sign applied by use case)
    public let currencyCode: String
    public let categoryId: String?
    public let merchant: String?
    public let note: String?
    public let occurredAt: Date

    public init(
        type: TransactionType,
        accountId: String,
        amount: Money,
        currencyCode: String,
        categoryId: String? = nil,
        merchant: String? = nil,
        note: String? = nil,
        occurredAt: Date
    ) {
        self.type = type
        self.accountId = accountId
        self.amount = amount
        self.currencyCode = currencyCode
        self.categoryId = categoryId
        self.merchant = merchant
        self.note = note
        self.occurredAt = occurredAt
    }
}

/// AI suggestion returned by `SuggestCategoryAndAccount`. Never blocks saving
/// (Constitution Article V).
public struct Suggestion: Sendable {
    public let categoryId: String?
    public let accountId: String
    public let confidence: Double

    public init(categoryId: String?, accountId: String, confidence: Double) {
        self.categoryId = categoryId
        self.accountId = accountId
        self.confidence = confidence
    }
}

/// The two linked legs of a recorded transfer (Constitution Article III — Invariant 6).
public struct TransferPair: Sendable {
    public let legSource: Transaction
    public let legDest: Transaction

    public init(legSource: Transaction, legDest: Transaction) {
        self.legSource = legSource
        self.legDest = legDest
    }
}

/// Reference to the last-saved entry — used by `UndoLastRecord`.
public enum SavedRef: Sendable {
    case single(id: String)
    case group(transferGroupId: String)
}

/// Errors that can be thrown by `RecordTransfer`.
public enum TransferError: Error, Sendable {
    case sameAccount
    case crossCurrency
}

/// Raw digit string plus resolved currency, ready for `MoneyParser`.
public struct AmountInput: Sendable {
    public let digits: String
    public let currency: Currency

    public init(digits: String, currency: Currency) {
        self.digits = digits
        self.currency = currency
    }
}
