import Foundation
import GRDB
import BolsilloDomain

public enum AccountSeed {
    public static func seed(_ db: BolsilloDatabase, currency: Currency = CurrencySeed.usd) throws {
        try db.queue.write { dbq in
            // Only seed if no accounts exist
            guard try AccountRecord.fetchCount(dbq) == 0 else { return }
            let now = Date().timeIntervalSince1970
            try AccountRecord(
                id: "default-cash",
                name: "Efectivo",
                type: AccountType.cash.rawValue,
                currencyCode: currency.code,
                initialBalanceMinor: 0,
                icon: "banknote",
                color: 0xFF16B364,
                archived: false,
                createdAt: now,
                updatedAt: now
            ).insert(dbq)
        }
    }
}
