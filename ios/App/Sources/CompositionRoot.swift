import Foundation
import BolsilloDomain
import BolsilloData

@MainActor
public final class CompositionRoot {
    public let db: BolsilloDatabase
    public let transactions: GRDBTransactionRepository
    public let accounts: GRDBAccountRepository
    public let classifier: LastUsedExpenseClassifier
    public let suggestCategoryAndAccount: SuggestCategoryAndAccount
    public let recordTransaction: RecordTransaction
    public let recordTransfer: RecordTransfer
    public let undoLastRecord: UndoLastRecord
    public let observeAccountBalances: ObserveAccountBalances
    public let editTransaction: EditTransaction
    public let softDeleteTransaction: SoftDeleteTransaction
    public let restoreTransaction: RestoreTransaction

    public init() throws {
        let dbPath = Self.databasePath()
        db = try BolsilloDatabase(path: dbPath)
        transactions = GRDBTransactionRepository(db: db)
        accounts = GRDBAccountRepository(db: db)
        classifier = LastUsedExpenseClassifier(transactions: transactions)
        suggestCategoryAndAccount = SuggestCategoryAndAccount(
            classifier: classifier,
            transactions: transactions,
            accounts: accounts
        )
        recordTransaction = RecordTransaction(transactions: transactions)
        recordTransfer = RecordTransfer(transactions: transactions, accounts: accounts)
        undoLastRecord = UndoLastRecord(transactions: transactions)
        observeAccountBalances = ObserveAccountBalances(accounts: accounts)
        editTransaction = EditTransaction(transactions: transactions)
        softDeleteTransaction = SoftDeleteTransaction(transactions: transactions)
        restoreTransaction = RestoreTransaction(transactions: transactions)

        // Seed on first launch
        try AccountSeed.seed(db)
        try CategorySeed.seed(db)
    }

    private static func databasePath() -> String {
        let urls = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return urls[0].appendingPathComponent("bolsillo.sqlite").path
    }
}
