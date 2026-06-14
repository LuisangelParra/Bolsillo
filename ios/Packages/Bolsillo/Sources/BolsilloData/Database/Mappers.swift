import Foundation
import GRDB
import BolsilloDomain

extension AccountRecord {
    func toDomain() -> Account {
        Account(
            id: id, name: name,
            type: AccountType(rawValue: type) ?? .cash,
            currencyCode: currencyCode,
            initialBalance: Money(minorUnits: initialBalanceMinor),
            icon: icon, color: color, archived: archived,
            createdAt: Date(timeIntervalSince1970: createdAt),
            updatedAt: Date(timeIntervalSince1970: updatedAt)
        )
    }

    static func from(_ account: Account) -> AccountRecord {
        AccountRecord(
            id: account.id, name: account.name, type: account.type.rawValue,
            currencyCode: account.currencyCode,
            initialBalanceMinor: account.initialBalance.minorUnits,
            icon: account.icon, color: account.color, archived: account.archived,
            createdAt: account.createdAt.timeIntervalSince1970,
            updatedAt: account.updatedAt.timeIntervalSince1970
        )
    }
}

extension TransactionRecord {
    func toDomain() -> Transaction {
        Transaction(
            id: id, accountId: accountId,
            type: TransactionType(rawValue: type) ?? .expense,
            amount: Money(minorUnits: amountMinor),
            currencyCode: currencyCode,
            amountBase: Money(minorUnits: amountBaseMinor),
            fxRateMillis: fxRateMillis,
            categoryId: categoryId, merchant: merchant, note: note,
            occurredAt: Date(timeIntervalSince1970: occurredAt),
            transferGroupId: transferGroupId,
            createdAt: Date(timeIntervalSince1970: createdAt),
            updatedAt: Date(timeIntervalSince1970: updatedAt),
            deletedAt: deletedAt.map { Date(timeIntervalSince1970: $0) }
        )
    }

    static func from(_ tx: Transaction) -> TransactionRecord {
        TransactionRecord(
            id: tx.id, accountId: tx.accountId, type: tx.type.rawValue,
            amountMinor: tx.amount.minorUnits, currencyCode: tx.currencyCode,
            amountBaseMinor: tx.amountBase.minorUnits, fxRateMillis: tx.fxRateMillis,
            categoryId: tx.categoryId, merchant: tx.merchant, note: tx.note,
            occurredAt: tx.occurredAt.timeIntervalSince1970,
            transferGroupId: tx.transferGroupId,
            createdAt: tx.createdAt.timeIntervalSince1970,
            updatedAt: tx.updatedAt.timeIntervalSince1970,
            deletedAt: tx.deletedAt?.timeIntervalSince1970
        )
    }
}

extension CategoryRecord {
    func toDomain() -> BolsilloDomain.Category {
        BolsilloDomain.Category(id: id, nameKey: nameKey, icon: icon, colorToken: colorToken)
    }

    static func from(_ cat: BolsilloDomain.Category) -> CategoryRecord {
        CategoryRecord(id: cat.id, nameKey: cat.nameKey, icon: cat.icon, colorToken: cat.colorToken)
    }
}
