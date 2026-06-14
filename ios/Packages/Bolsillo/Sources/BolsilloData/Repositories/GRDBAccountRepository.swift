import Foundation
import GRDB
import BolsilloDomain

public final class GRDBAccountRepository: AccountRepository, @unchecked Sendable {
    private let db: BolsilloDatabase
    private let currencies: [Currency]

    public init(db: BolsilloDatabase, currencies: [Currency] = CurrencySeed.essentials) {
        self.db = db
        self.currencies = currencies
    }

    public func observeCurrencies() -> AsyncStream<[Currency]> {
        let currencies = self.currencies
        return AsyncStream { cont in
            cont.yield(currencies)
            cont.finish()
        }
    }

    public func observeAccounts() -> AsyncStream<[Account]> {
        let observation = ValueObservation.tracking { dbq in
            try AccountRecord
                .filter(Column("archived") == false)
                .fetchAll(dbq)
                .map { $0.toDomain() }
        }
        return AsyncStream { cont in
            let cancellable = observation.start(
                in: db.queue,
                scheduling: .async(onQueue: .main),
                onError: { _ in cont.finish() },
                onChange: { cont.yield($0) }
            )
            cont.onTermination = { _ in cancellable.cancel() }
        }
    }

    public func getById(_ id: String) async -> Account? {
        try? await db.queue.read { dbq in
            try AccountRecord.fetchOne(dbq, key: id)?.toDomain()
        }
    }

    public func observeBalance(accountId: String) -> AsyncStream<Money> {
        let observation = ValueObservation.tracking { dbq -> Money in
            let initial = try AccountRecord.fetchOne(dbq, key: accountId)
                .map { Money(minorUnits: $0.initialBalanceMinor) } ?? .zero
            let sum = try Int.fetchOne(
                dbq,
                sql: "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE accountId = ? AND deletedAt IS NULL",
                arguments: [accountId]
            ) ?? 0
            return initial + Money(minorUnits: sum)
        }
        return AsyncStream { cont in
            let cancellable = observation.start(
                in: db.queue,
                scheduling: .async(onQueue: .main),
                onError: { _ in cont.finish() },
                onChange: { cont.yield($0) }
            )
            cont.onTermination = { _ in cancellable.cancel() }
        }
    }

    public func observeBalances() -> AsyncStream<[String: Money]> {
        let observation = ValueObservation.tracking { dbq -> [String: Money] in
            let accounts = try AccountRecord.fetchAll(dbq).filter { !$0.archived }
            var result: [String: Money] = [:]
            for account in accounts {
                let initial = Money(minorUnits: account.initialBalanceMinor)
                let sum = try Int.fetchOne(
                    dbq,
                    sql: "SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE accountId = ? AND deletedAt IS NULL",
                    arguments: [account.id]
                ) ?? 0
                result[account.id] = initial + Money(minorUnits: sum)
            }
            return result
        }
        return AsyncStream { cont in
            let cancellable = observation.start(
                in: db.queue,
                scheduling: .async(onQueue: .main),
                onError: { _ in cont.finish() },
                onChange: { cont.yield($0) }
            )
            cont.onTermination = { _ in cancellable.cancel() }
        }
    }
}
