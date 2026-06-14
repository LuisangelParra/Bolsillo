import Testing
import Foundation
import BolsilloDomain
import BolsilloData
import GRDB

// NOTE: TransferError already conforms to Equatable in BolsilloDomain (Values.swift).

@Suite struct TransferAtomicityTests {

    // MARK: Same-account rejection

    @Test func sameAccountTransferThrows() async throws {
        let db = try BolsilloDatabase.inMemory()
        let txRepo = GRDBTransactionRepository(db: db)
        let accRepo = GRDBAccountRepository(db: db)
        try AccountSeed.seed(db)

        let useCase = RecordTransfer(transactions: txRepo, accounts: accRepo)

        var caughtError: TransferError? = nil
        do {
            _ = try await useCase(
                sourceAccountId: "default-cash",
                destAccountId:   "default-cash",
                amount: Money(minorUnits: 100),
                occurredAt: Date()
            )
        } catch let e as TransferError {
            caughtError = e
        }
        #expect(caughtError == .sameAccount,
                "Same-account transfer must throw TransferError.sameAccount (RecordTransfer guard)")
    }

    // MARK: Transfer pair sums to zero (Invariant 4)

    @Test func transferPairSumsToZero() async throws {
        let db = try BolsilloDatabase.inMemory()
        let txRepo = GRDBTransactionRepository(db: db)
        let accRepo = GRDBAccountRepository(db: db)
        try AccountSeed.seed(db)

        // Create a second account via raw SQL (AccountRecord is internal to BolsilloData)
        try await db.queue.write { dbq in
            try dbq.execute(
                sql: """
                    INSERT INTO accounts
                        (id, name, type, currencyCode, initialBalanceMinor, icon, color, archived, createdAt, updatedAt)
                    VALUES
                        ('acc-savings', 'Savings', 'savings', 'USD', 0, 'banknote', 0, 0, ?, ?)
                    """,
                arguments: [Date().timeIntervalSince1970, Date().timeIntervalSince1970]
            )
        }

        let useCase = RecordTransfer(transactions: txRepo, accounts: accRepo)
        let pair = try await useCase(
            sourceAccountId: "default-cash",
            destAccountId:   "acc-savings",
            amount: Money(minorUnits: 5000),
            occurredAt: Date()
        )

        #expect(pair.legSource.amount.minorUnits + pair.legDest.amount.minorUnits == 0,
                "Transfer pair must sum to zero (Constitution Article III — Invariant 4)")
        #expect(pair.legSource.amount.minorUnits < 0, "Source leg must be negative (debited)")
        #expect(pair.legDest.amount.minorUnits   > 0, "Destination leg must be positive (credited)")
    }

    // MARK: Both legs persisted atomically

    @Test func bothLegsArePersistedAfterTransfer() async throws {
        let db = try BolsilloDatabase.inMemory()
        let txRepo = GRDBTransactionRepository(db: db)
        let accRepo = GRDBAccountRepository(db: db)
        try AccountSeed.seed(db)

        try await db.queue.write { dbq in
            try dbq.execute(
                sql: """
                    INSERT INTO accounts
                        (id, name, type, currencyCode, initialBalanceMinor, icon, color, archived, createdAt, updatedAt)
                    VALUES
                        ('acc-wallet', 'Wallet', 'wallet', 'USD', 0, 'wallet', 0, 0, ?, ?)
                    """,
                arguments: [Date().timeIntervalSince1970, Date().timeIntervalSince1970]
            )
        }

        let useCase = RecordTransfer(transactions: txRepo, accounts: accRepo)
        let pair = try await useCase(
            sourceAccountId: "default-cash",
            destAccountId:   "acc-wallet",
            amount: Money(minorUnits: 2500),
            occurredAt: Date()
        )

        let src = await txRepo.getById(pair.legSource.id)
        let dst = await txRepo.getById(pair.legDest.id)
        #expect(src != nil, "Source leg must be persisted after transfer")
        #expect(dst != nil, "Destination leg must be persisted after transfer")
    }

    // MARK: Both legs share the same transferGroupId (Invariant 6)

    @Test func legsShareTransferGroupId() async throws {
        let db = try BolsilloDatabase.inMemory()
        let txRepo = GRDBTransactionRepository(db: db)
        let accRepo = GRDBAccountRepository(db: db)
        try AccountSeed.seed(db)

        try await db.queue.write { dbq in
            try dbq.execute(
                sql: """
                    INSERT INTO accounts
                        (id, name, type, currencyCode, initialBalanceMinor, icon, color, archived, createdAt, updatedAt)
                    VALUES
                        ('acc-bank', 'Bank', 'bank', 'USD', 0, 'building.columns', 0, 0, ?, ?)
                    """,
                arguments: [Date().timeIntervalSince1970, Date().timeIntervalSince1970]
            )
        }

        let useCase = RecordTransfer(transactions: txRepo, accounts: accRepo)
        let pair = try await useCase(
            sourceAccountId: "default-cash",
            destAccountId:   "acc-bank",
            amount: Money(minorUnits: 10_000),
            occurredAt: Date()
        )

        #expect(pair.legSource.transferGroupId != nil, "Source leg must have a transferGroupId")
        #expect(pair.legDest.transferGroupId   != nil, "Destination leg must have a transferGroupId")
        #expect(pair.legSource.transferGroupId == pair.legDest.transferGroupId,
                "Both legs must share the same transferGroupId (Invariant 6 — double-entry linking)")
    }

    // MARK: Cross-currency rejection

    @Test func crossCurrencyTransferThrows() async throws {
        let db = try BolsilloDatabase.inMemory()
        let txRepo = GRDBTransactionRepository(db: db)
        let accRepo = GRDBAccountRepository(db: db)
        try AccountSeed.seed(db) // default-cash is USD

        // Insert a COP account
        try await db.queue.write { dbq in
            try dbq.execute(
                sql: """
                    INSERT INTO accounts
                        (id, name, type, currencyCode, initialBalanceMinor, icon, color, archived, createdAt, updatedAt)
                    VALUES
                        ('acc-cop', 'COP Cash', 'cash', 'COP', 0, 'banknote', 0, 0, ?, ?)
                    """,
                arguments: [Date().timeIntervalSince1970, Date().timeIntervalSince1970]
            )
        }

        let useCase = RecordTransfer(transactions: txRepo, accounts: accRepo)

        var caughtError: TransferError? = nil
        do {
            _ = try await useCase(
                sourceAccountId: "default-cash",
                destAccountId:   "acc-cop",
                amount: Money(minorUnits: 1000),
                occurredAt: Date()
            )
        } catch let e as TransferError {
            caughtError = e
        }
        #expect(caughtError == .crossCurrency,
                "Cross-currency transfer must throw TransferError.crossCurrency (feature 001 defers E8)")
    }
}
