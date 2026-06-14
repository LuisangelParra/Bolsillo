import Foundation
import BolsilloDomain

// MARK: - MockTransactionRepository

/// Thread-safe in-memory stub for domain unit tests.
/// Mirrors InMemoryTransactionRepository (BolsilloData) but lives here so
/// BolsilloDomainTests never imports BolsilloData.
actor MockTransactionRepository: TransactionRepository {
    var byId: [String: Transaction] = [:]

    func observeAll() async -> [Transaction] {
        byId.values.filter { !$0.isDeleted }
    }

    func getById(_ id: String) async -> Transaction? {
        byId[id]
    }

    func upsert(_ transaction: Transaction) async {
        byId[transaction.id] = transaction
    }

    func softDelete(id: String, deletedAt: Date) async {
        guard let existing = byId[id] else { return }
        byId[id] = existing.markDeleted(at: deletedAt)
    }

    func restore(id: String) async {
        guard let existing = byId[id] else { return }
        byId[id] = existing.restored()
    }

    func lastUsed() async -> Transaction? {
        byId.values
            .filter { !$0.isDeleted }
            .sorted { $0.createdAt > $1.createdAt }
            .first
    }

    func upsertTransfer(legSource: Transaction, legDest: Transaction) async {
        byId[legSource.id] = legSource
        byId[legDest.id] = legDest
    }

    func softDeleteGroup(transferGroupId: String, deletedAt: Date) async {
        for (key, tx) in byId where tx.transferGroupId == transferGroupId {
            byId[key] = tx.markDeleted(at: deletedAt)
        }
    }

    func restoreGroup(transferGroupId: String) async {
        for (key, tx) in byId where tx.transferGroupId == transferGroupId {
            byId[key] = tx.restored()
        }
    }
}

// MARK: - MockAccountRepository

final class MockAccountRepository: AccountRepository, @unchecked Sendable {
    let accounts: [Account]

    init(accounts: [Account] = [
        Account(
            id: "acc1",
            name: "Cash",
            type: .cash,
            currencyCode: "USD",
            initialBalance: .zero,
            icon: "banknote",
            color: 0,
            archived: false,
            createdAt: Date(),
            updatedAt: Date()
        )
    ]) {
        self.accounts = accounts
    }

    func observeCurrencies() -> AsyncStream<[Currency]> {
        AsyncStream { cont in
            cont.yield([Currency(code: "USD", symbol: "$", decimalDigits: 2, isEnabled: true, isEssential: true)])
            cont.finish()
        }
    }

    func observeAccounts() -> AsyncStream<[Account]> {
        let accounts = self.accounts
        return AsyncStream { cont in
            cont.yield(accounts)
            cont.finish()
        }
    }

    func getById(_ id: String) async -> Account? {
        accounts.first { $0.id == id }
    }

    func observeBalance(accountId: String) -> AsyncStream<Money> {
        AsyncStream { cont in
            cont.yield(.zero)
            cont.finish()
        }
    }

    func observeBalances() -> AsyncStream<[String: Money]> {
        let result = Dictionary(uniqueKeysWithValues: accounts.map { ($0.id, Money.zero) })
        return AsyncStream { cont in
            cont.yield(result)
            cont.finish()
        }
    }
}

// MARK: - AlwaysEmptyClassifier

/// Classifier stub that always returns no suggestion (confidence = 0).
struct AlwaysEmptyClassifier: ExpenseClassifier {
    static let defaultThreshold: Double = 0.75

    func suggest(_ input: ClassificationInput) async -> ClassificationResult {
        ClassificationResult(topCategoryId: nil, confidence: 0.0, alternatives: [])
    }

    func learn(_ input: ClassificationInput, chosenCategoryId: String) async {}
}
