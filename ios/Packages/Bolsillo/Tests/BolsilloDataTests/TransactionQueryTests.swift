import Testing
import Foundation
import BolsilloDomain
@testable import BolsilloData
import GRDB

@Suite struct TransactionQueryTests {
    let db: BolsilloDatabase
    let repo: GRDBTransactionRepository

    init() throws {
        db = try BolsilloDatabase.inMemory()
        repo = GRDBTransactionRepository(db: db)
    }

    // MARK: Soft-delete visibility

    @Test func softDeletedExcludedFromObserveAll() async throws {
        let tx = makeTransaction(id: "t1", amount: -100)
        await repo.upsert(tx)
        await repo.softDelete(id: "t1", deletedAt: Date())

        let all = await repo.observeAll()
        #expect(!all.contains(where: { $0.id == "t1" }),
                "Soft-deleted transactions must not appear in observeAll")
    }

    @Test func activeTransactionAppearsInObserveAll() async throws {
        let tx = makeTransaction(id: "active-1", amount: -200)
        await repo.upsert(tx)

        let all = await repo.observeAll()
        #expect(all.contains(where: { $0.id == "active-1" }),
                "Active (non-deleted) transaction must appear in observeAll")
    }

    @Test func softDeletedTransactionIsStillFetchableById() async throws {
        let tx = makeTransaction(id: "t-deleted", amount: -50)
        await repo.upsert(tx)
        await repo.softDelete(id: "t-deleted", deletedAt: Date())

        let fetched = await repo.getById("t-deleted")
        #expect(fetched != nil,
                "Soft-deleted record must remain fetchable by id (no hard deletes, Article III)")
        #expect(fetched?.isDeleted == true)
    }

    // MARK: Balance SUM correctness

    @Test func balanceSumCorrect() async throws {
        let accountId = "acc-sum"

        // Insert two expenses and one income
        await repo.upsert(makeTransaction(id: "bs-1", accountId: accountId, amount: -300))
        await repo.upsert(makeTransaction(id: "bs-2", accountId: accountId, amount: -200))
        await repo.upsert(makeTransaction(id: "bs-3", accountId: accountId, amount: 1000))

        let sum = try await db.queue.read { dbq in
            try Int.fetchOne(
                dbq,
                sql: "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE accountId = ? AND deletedAt IS NULL",
                arguments: [accountId]
            ) ?? 0
        }
        #expect(sum == 500, "Balance SUM: -300 + -200 + 1000 = 500")
    }

    @Test func deletedTransactionExcludedFromBalance() async throws {
        let accountId = "acc-del"
        await repo.upsert(makeTransaction(id: "bd-1", accountId: accountId, amount: -500))
        await repo.upsert(makeTransaction(id: "bd-2", accountId: accountId, amount: 200))
        await repo.softDelete(id: "bd-2", deletedAt: Date())

        let sum = try await db.queue.read { dbq in
            try Int.fetchOne(
                dbq,
                sql: "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE accountId = ? AND deletedAt IS NULL",
                arguments: [accountId]
            ) ?? 0
        }
        #expect(sum == -500,
                "Soft-deleted income must be excluded from balance SUM (Constitution Article III)")
    }

    @Test func emptyAccountBalanceIsZero() async throws {
        let sum = try await db.queue.read { dbq in
            try Int.fetchOne(
                dbq,
                sql: "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE accountId = ? AND deletedAt IS NULL",
                arguments: ["acc-empty"]
            ) ?? 0
        }
        #expect(sum == 0, "Account with no transactions must have zero balance")
    }

    // MARK: lastUsed

    @Test func lastUsedReturnsNewestNonDeleted() async throws {
        let earlier = Date(timeIntervalSinceNow: -60)
        let later   = Date()

        await repo.upsert(makeTransaction(id: "lu-old", accountId: "a1", amount: -100, createdAt: earlier))
        await repo.upsert(makeTransaction(id: "lu-new", accountId: "a1", amount: -200, createdAt: later))

        let last = await repo.lastUsed()
        #expect(last?.id == "lu-new", "lastUsed must return the most recently created non-deleted transaction")
    }

    // MARK: Restore

    @Test func restoreUndoesSoftDelete() async throws {
        let tx = makeTransaction(id: "restore-1", amount: -300)
        await repo.upsert(tx)
        await repo.softDelete(id: "restore-1", deletedAt: Date())
        await repo.restore(id: "restore-1")

        let fetched = await repo.getById("restore-1")
        #expect(fetched?.isDeleted == false,
                "Restored transaction must have isDeleted == false")
    }

    // MARK: Transfer group soft-delete

    @Test func softDeleteGroupRemovesBothLegs() async throws {
        let groupId = "grp-1"
        let src = makeTransactionWithGroup(id: "leg-src", accountId: "a1", amount: -500, groupId: groupId)
        let dst = makeTransactionWithGroup(id: "leg-dst", accountId: "a2", amount:  500, groupId: groupId)

        await repo.upsertTransfer(legSource: src, legDest: dst)
        await repo.softDeleteGroup(transferGroupId: groupId, deletedAt: Date())

        let srcFetched = await repo.getById("leg-src")
        let dstFetched = await repo.getById("leg-dst")
        #expect(srcFetched?.isDeleted == true, "Source leg must be soft-deleted by group delete")
        #expect(dstFetched?.isDeleted == true, "Destination leg must be soft-deleted by group delete")
    }

    // MARK: Helpers

    private func makeTransaction(
        id: String,
        accountId: String = "acc1",
        amount: Int,
        createdAt: Date = Date()
    ) -> Transaction {
        Transaction(
            id: id,
            accountId: accountId,
            type: amount < 0 ? .expense : .income,
            amount: Money(minorUnits: amount),
            currencyCode: "USD",
            amountBase: Money(minorUnits: amount),
            fxRateMillis: 1000,
            categoryId: nil,
            merchant: nil,
            note: nil,
            occurredAt: createdAt,
            transferGroupId: nil,
            createdAt: createdAt,
            updatedAt: createdAt
        )
    }

    private func makeTransactionWithGroup(
        id: String,
        accountId: String,
        amount: Int,
        groupId: String
    ) -> Transaction {
        let now = Date()
        return Transaction(
            id: id,
            accountId: accountId,
            type: .transfer,
            amount: Money(minorUnits: amount),
            currencyCode: "USD",
            amountBase: Money(minorUnits: amount),
            fxRateMillis: 1000,
            categoryId: "transfer",
            merchant: nil,
            note: nil,
            occurredAt: now,
            transferGroupId: groupId,
            createdAt: now,
            updatedAt: now
        )
    }
}
