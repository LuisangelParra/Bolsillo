import BolsilloDomain
import Foundation

/// Placeholder repository (in-memory) until GRDB/SQLCipher persistence is implemented.
/// Demonstrates the soft-delete contract: `softDelete` sets deletedAt; nothing is ever
/// physically removed (Constitution Article III). An `actor` provides thread-safe access.
public actor InMemoryTransactionRepository: TransactionRepository {
    private var byId: [String: Transaction] = [:]

    public init() {}

    public func observeAll() async -> [Transaction] {
        byId.values.filter { !$0.isDeleted }
    }

    public func getById(_ id: String) async -> Transaction? {
        byId[id]
    }

    public func upsert(_ transaction: Transaction) async {
        byId[transaction.id] = transaction
    }

    public func softDelete(id: String, deletedAt: Date) async {
        guard let existing = byId[id] else { return }
        byId[id] = existing.markDeleted(at: deletedAt)
    }

    public func restore(id: String) async {
        guard let existing = byId[id] else { return }
        byId[id] = existing.restored()
    }

    // MARK: - Extended for feature 001

    public func lastUsed() async -> Transaction? {
        byId.values
            .filter { !$0.isDeleted }
            .sorted { $0.createdAt > $1.createdAt }
            .first
    }

    public func upsertTransfer(legSource: Transaction, legDest: Transaction) async {
        byId[legSource.id] = legSource
        byId[legDest.id] = legDest
    }

    public func softDeleteGroup(transferGroupId: String, deletedAt: Date) async {
        for (key, tx) in byId where tx.transferGroupId == transferGroupId {
            byId[key] = tx.markDeleted(at: deletedAt)
        }
    }

    public func restoreGroup(transferGroupId: String) async {
        for (key, tx) in byId where tx.transferGroupId == transferGroupId {
            byId[key] = tx.restored()
        }
    }
}
