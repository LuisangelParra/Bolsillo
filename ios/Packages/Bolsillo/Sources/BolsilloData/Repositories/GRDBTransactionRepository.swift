import Foundation
import GRDB
import BolsilloDomain

public final class GRDBTransactionRepository: TransactionRepository, @unchecked Sendable {
    private let db: BolsilloDatabase

    public init(db: BolsilloDatabase) { self.db = db }

    public func observeAll() async -> [Transaction] {
        (try? await db.queue.read { dbq in
            try TransactionRecord
                .filter(Column("deletedAt") == nil)
                .fetchAll(dbq)
                .map { $0.toDomain() }
        }) ?? []
    }

    public func getById(_ id: String) async -> Transaction? {
        try? await db.queue.read { dbq in
            try TransactionRecord.fetchOne(dbq, key: id)?.toDomain()
        }
    }

    public func upsert(_ transaction: Transaction) async {
        try? await db.queue.write { dbq in
            try TransactionRecord.from(transaction).save(dbq)
        }
    }

    public func softDelete(id: String, deletedAt: Date) async {
        try? await db.queue.write { dbq in
            try dbq.execute(
                sql: "UPDATE transactions SET deletedAt = ?, updatedAt = ? WHERE id = ?",
                arguments: [deletedAt.timeIntervalSince1970, Date().timeIntervalSince1970, id]
            )
        }
    }

    public func restore(id: String) async {
        try? await db.queue.write { dbq in
            try dbq.execute(
                sql: "UPDATE transactions SET deletedAt = NULL, updatedAt = ? WHERE id = ?",
                arguments: [Date().timeIntervalSince1970, id]
            )
        }
    }

    public func lastUsed() async -> Transaction? {
        try? await db.queue.read { dbq in
            try TransactionRecord
                .filter(Column("deletedAt") == nil)
                .order(Column("createdAt").desc)
                .fetchOne(dbq)?
                .toDomain()
        }
    }

    public func upsertTransfer(legSource: Transaction, legDest: Transaction) async {
        try? await db.queue.write { dbq in
            try TransactionRecord.from(legSource).save(dbq)
            try TransactionRecord.from(legDest).save(dbq)
        }
    }

    public func softDeleteGroup(transferGroupId: String, deletedAt: Date) async {
        try? await db.queue.write { dbq in
            try dbq.execute(
                sql: "UPDATE transactions SET deletedAt = ?, updatedAt = ? WHERE transferGroupId = ?",
                arguments: [deletedAt.timeIntervalSince1970, Date().timeIntervalSince1970, transferGroupId]
            )
        }
    }

    public func restoreGroup(transferGroupId: String) async {
        try? await db.queue.write { dbq in
            try dbq.execute(
                sql: "UPDATE transactions SET deletedAt = NULL, updatedAt = ? WHERE transferGroupId = ?",
                arguments: [Date().timeIntervalSince1970, transferGroupId]
            )
        }
    }
}
