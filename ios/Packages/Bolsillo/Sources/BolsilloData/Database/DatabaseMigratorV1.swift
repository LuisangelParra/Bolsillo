import Foundation
import GRDB

enum DatabaseMigratorV1 {
    static func register(in migrator: inout DatabaseMigrator) {
        migrator.registerMigration("v1") { db in
            try db.create(table: "accounts") { t in
                t.primaryKey("id", .text)
                t.column("name", .text).notNull()
                t.column("type", .text).notNull()
                t.column("currencyCode", .text).notNull()
                t.column("initialBalanceMinor", .integer).notNull().defaults(to: 0)
                t.column("icon", .text).notNull().defaults(to: "")
                t.column("color", .integer).notNull().defaults(to: 0)
                t.column("archived", .boolean).notNull().defaults(to: false)
                t.column("createdAt", .double).notNull()
                t.column("updatedAt", .double).notNull()
            }

            try db.create(table: "transactions") { t in
                t.primaryKey("id", .text)
                t.column("accountId", .text).notNull().indexed()
                t.column("type", .text).notNull()
                t.column("amountMinor", .integer).notNull()
                t.column("currencyCode", .text).notNull()
                t.column("amountBaseMinor", .integer).notNull()
                t.column("fxRateMillis", .integer).notNull().defaults(to: 1000)
                t.column("categoryId", .text)
                t.column("merchant", .text)
                t.column("note", .text)
                t.column("occurredAt", .double).notNull().indexed()
                t.column("transferGroupId", .text).indexed()
                t.column("createdAt", .double).notNull()
                t.column("updatedAt", .double).notNull()
                t.column("deletedAt", .double)
            }
            // Composite index for balance SUM (accountId, deletedAt)
            try db.create(index: "transactions_account_active",
                          on: "transactions", columns: ["accountId", "deletedAt"])

            try db.create(table: "categories") { t in
                t.primaryKey("id", .text)
                t.column("nameKey", .text).notNull()
                t.column("icon", .text).notNull()
                t.column("colorToken", .text).notNull()
            }
        }
    }
}
