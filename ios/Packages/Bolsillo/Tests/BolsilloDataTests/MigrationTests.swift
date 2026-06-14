import Testing
import Foundation
import BolsilloDomain
@testable import BolsilloData
import GRDB

@Suite struct MigrationTests {

    // MARK: Table existence

    @Test func v1TablesCreated() async throws {
        let db = try BolsilloDatabase.inMemory()
        let tables = try await db.queue.read { dbq in
            try String.fetchAll(
                dbq,
                sql: "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
            )
        }
        #expect(tables.contains("accounts"),     "accounts table must exist after v1 migration")
        #expect(tables.contains("transactions"), "transactions table must exist after v1 migration")
        #expect(tables.contains("categories"),   "categories table must exist after v1 migration")
    }

    // MARK: Index existence

    @Test func v1CompositeIndexCreated() async throws {
        let db = try BolsilloDatabase.inMemory()
        let indices = try await db.queue.read { dbq in
            try String.fetchAll(
                dbq,
                sql: "SELECT name FROM sqlite_master WHERE type='index' ORDER BY name"
            )
        }
        // Composite index on (accountId, deletedAt) for balance SUM performance
        #expect(
            indices.contains(where: { $0.contains("account") }),
            "Composite index for (accountId, deletedAt) must exist after v1 migration"
        )
    }

    // MARK: Re-running migrator is idempotent (uses @testable to access internal DatabaseMigratorV1)

    @Test func seedDataSurvivesRerunningMigrator() async throws {
        let db = try BolsilloDatabase.inMemory()

        // Seed data before re-migration
        try AccountSeed.seed(db)
        try CategorySeed.seed(db)

        // Re-running migrator must skip already-applied migrations (idempotent)
        var migrator = DatabaseMigrator()
        DatabaseMigratorV1.register(in: &migrator)
        try migrator.migrate(db.queue)

        let accountCount = try await db.queue.read { dbq in
            try Int.fetchOne(dbq, sql: "SELECT COUNT(*) FROM accounts") ?? 0
        }
        let categoryCount = try await db.queue.read { dbq in
            try Int.fetchOne(dbq, sql: "SELECT COUNT(*) FROM categories") ?? 0
        }

        #expect(accountCount >= 1,
                "Seed account must survive re-migration (Article IX — data loss blocks release)")
        #expect(categoryCount == 20,
                "All 20 seed categories must survive re-migration")
    }

    // MARK: Money stored as integer (Article III)

    @Test func moneyStoredAsInteger() async throws {
        let db = try BolsilloDatabase.inMemory()
        let repo = GRDBTransactionRepository(db: db)
        let now = Date()

        let tx = Transaction(
            id: "money-int-test",
            accountId: "acc",
            type: .expense,
            amount: Money(minorUnits: -1234),
            currencyCode: "USD",
            amountBase: Money(minorUnits: -1234),
            fxRateMillis: 1000,
            categoryId: nil,
            merchant: nil,
            note: nil,
            occurredAt: now,
            transferGroupId: nil,
            createdAt: now,
            updatedAt: now
        )
        await repo.upsert(tx)

        let stored = try await db.queue.read { dbq in
            try Int.fetchOne(
                dbq,
                sql: "SELECT amountMinor FROM transactions WHERE id = ?",
                arguments: ["money-int-test"]
            ) ?? 0
        }
        #expect(stored == -1234,
                "Money must be stored as integer minor units — never Double (Constitution Article III)")
    }

    // MARK: Column presence

    @Test func transactionsColumnsExist() async throws {
        let db = try BolsilloDatabase.inMemory()
        let columns = try await db.queue.read { dbq in
            try Row.fetchAll(dbq, sql: "PRAGMA table_info(transactions)")
                .map { $0["name"] as String? ?? "" }
        }
        #expect(columns.contains("amountMinor"),     "amountMinor column must exist")
        #expect(columns.contains("deletedAt"),       "deletedAt column must exist (soft-delete, Article III)")
        #expect(columns.contains("transferGroupId"), "transferGroupId must exist (double-entry, Invariant 6)")
        #expect(columns.contains("fxRateMillis"),    "fxRateMillis must exist (frozen FX, Invariant 8)")
    }

    @Test func accountsColumnsExist() async throws {
        let db = try BolsilloDatabase.inMemory()
        let columns = try await db.queue.read { dbq in
            try Row.fetchAll(dbq, sql: "PRAGMA table_info(accounts)")
                .map { $0["name"] as String? ?? "" }
        }
        #expect(columns.contains("initialBalanceMinor"), "initialBalanceMinor must be integer (Article III)")
        #expect(columns.contains("archived"),            "archived column must exist for soft-archive")
        #expect(columns.contains("currencyCode"),        "currencyCode column must exist")
    }

    // MARK: Seed counts

    @Test func categorySeedCreates20Categories() async throws {
        let db = try BolsilloDatabase.inMemory()
        try CategorySeed.seed(db)

        let count = try await db.queue.read { dbq in
            try Int.fetchOne(dbq, sql: "SELECT COUNT(*) FROM categories") ?? 0
        }
        #expect(count == 20, "CategorySeed must insert exactly 20 categories")
    }

    @Test func accountSeedCreatesDefaultCashAccount() async throws {
        let db = try BolsilloDatabase.inMemory()
        try AccountSeed.seed(db)

        let count = try await db.queue.read { dbq in
            try Int.fetchOne(dbq, sql: "SELECT COUNT(*) FROM accounts WHERE id = 'default-cash'") ?? 0
        }
        #expect(count == 1, "AccountSeed must create exactly one 'default-cash' account")
    }

    @Test func accountSeedIsIdempotent() async throws {
        let db = try BolsilloDatabase.inMemory()
        try AccountSeed.seed(db)
        try AccountSeed.seed(db) // second call must be a no-op

        let count = try await db.queue.read { dbq in
            try Int.fetchOne(dbq, sql: "SELECT COUNT(*) FROM accounts") ?? 0
        }
        #expect(count == 1, "AccountSeed must be idempotent — seeding twice must not duplicate rows")
    }
}
