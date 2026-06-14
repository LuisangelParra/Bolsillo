import Foundation

public enum AccountType: String, Sendable, CaseIterable {
    case cash, debit, credit, bank, savings, wallet
}

/// A financial account. Balances are derived at query time from the sum of all
/// non-deleted transactions (Constitution Article III — Invariant 2).
public struct Account: Equatable, Sendable, Identifiable {
    public let id: String
    public let name: String
    public let type: AccountType
    public let currencyCode: String
    public let initialBalance: Money
    public let icon: String
    public let color: Int         // ARGB
    public let archived: Bool
    public let createdAt: Date
    public let updatedAt: Date

    public init(
        id: String,
        name: String,
        type: AccountType,
        currencyCode: String,
        initialBalance: Money,
        icon: String,
        color: Int,
        archived: Bool,
        createdAt: Date,
        updatedAt: Date
    ) {
        self.id = id
        self.name = name
        self.type = type
        self.currencyCode = currencyCode
        self.initialBalance = initialBalance
        self.icon = icon
        self.color = color
        self.archived = archived
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
