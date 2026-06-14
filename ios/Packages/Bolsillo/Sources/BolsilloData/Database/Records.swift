import Foundation
import GRDB

struct AccountRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "accounts"
    var id: String
    var name: String
    var type: String              // AccountType.rawValue
    var currencyCode: String
    var initialBalanceMinor: Int
    var icon: String
    var color: Int
    var archived: Bool
    var createdAt: Double         // epoch seconds
    var updatedAt: Double
}

struct TransactionRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "transactions"
    var id: String
    var accountId: String
    var type: String              // TransactionType.rawValue
    var amountMinor: Int          // signed
    var currencyCode: String
    var amountBaseMinor: Int      // signed, frozen
    var fxRateMillis: Int
    var categoryId: String?
    var merchant: String?
    var note: String?
    var occurredAt: Double
    var transferGroupId: String?
    var createdAt: Double
    var updatedAt: Double
    var deletedAt: Double?
}

struct CategoryRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "categories"
    var id: String
    var nameKey: String
    var icon: String
    var colorToken: String
}
