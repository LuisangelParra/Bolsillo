import Testing
import Foundation
import BolsilloDomain
import BolsilloData
@testable import FeatureRecord

// MARK: - Helpers

actor MockTxRepo: TransactionRepository {
    var store: [String: Transaction] = [:]
    func observeAll() async -> [Transaction] { store.values.filter { !$0.isDeleted } }
    func getById(_ id: String) async -> Transaction? { store[id] }
    func upsert(_ tx: Transaction) async { store[tx.id] = tx }
    func softDelete(id: String, deletedAt: Date) async {
        guard let tx = store[id] else { return }
        store[id] = tx.markDeleted(at: deletedAt)
    }
    func restore(id: String) async {
        guard let tx = store[id] else { return }
        store[id] = tx.restored()
    }
    func lastUsed() async -> Transaction? {
        store.values.filter { !$0.isDeleted }.sorted { $0.createdAt > $1.createdAt }.first
    }
    func upsertTransfer(legSource: Transaction, legDest: Transaction) async {
        store[legSource.id] = legSource; store[legDest.id] = legDest
    }
    func softDeleteGroup(transferGroupId: String, deletedAt: Date) async {
        for (k, tx) in store where tx.transferGroupId == transferGroupId {
            store[k] = tx.markDeleted(at: deletedAt)
        }
    }
    func restoreGroup(transferGroupId: String) async {
        for (k, tx) in store where tx.transferGroupId == transferGroupId {
            store[k] = tx.restored()
        }
    }
}

final class MockAccRepo: AccountRepository, @unchecked Sendable {
    let account = Account(id: "acc1", name: "Cash", type: .cash, currencyCode: "USD",
                          initialBalance: .zero, icon: "banknote", color: 0,
                          archived: false, createdAt: Date(), updatedAt: Date())
    func observeCurrencies() -> AsyncStream<[Currency]> {
        AsyncStream { cont in cont.yield([CurrencySeed.usd]); cont.finish() }
    }
    func observeAccounts() -> AsyncStream<[Account]> {
        AsyncStream { [self] cont in cont.yield([account]); cont.finish() }
    }
    func getById(_ id: String) async -> Account? { id == "acc1" ? account : nil }
    func observeBalance(accountId: String) -> AsyncStream<Money> {
        AsyncStream { cont in cont.yield(.zero); cont.finish() }
    }
    func observeBalances() -> AsyncStream<[String: Money]> {
        AsyncStream { cont in cont.yield(["acc1": .zero]); cont.finish() }
    }
}

struct AlwaysEmptyClassifier: ExpenseClassifier {
    static let defaultThreshold: Double = 0.75
    func suggest(_ input: ClassificationInput) async -> ClassificationResult {
        ClassificationResult(topCategoryId: nil, confidence: 0.0, alternatives: [])
    }
    func learn(_ input: ClassificationInput, chosenCategoryId: String) async {}
}

@MainActor
func makeModel(txRepo: MockTxRepo) -> RecordModel {
    let accRepo = MockAccRepo()
    let classifier = AlwaysEmptyClassifier()
    return RecordModel(
        suggestCategoryAndAccount: SuggestCategoryAndAccount(
            classifier: classifier, transactions: txRepo, accounts: accRepo),
        recordTransaction: RecordTransaction(transactions: txRepo),
        recordTransfer: RecordTransfer(transactions: txRepo, accounts: accRepo),
        undoLastRecord: UndoLastRecord(transactions: txRepo),
        observeAccountBalances: ObserveAccountBalances(accounts: accRepo),
        editTransaction: EditTransaction(transactions: txRepo),
        softDeleteTransaction: SoftDeleteTransaction(transactions: txRepo),
        restoreTransaction: RestoreTransaction(transactions: txRepo)
    )
}

// MARK: - Tests

@Suite struct RecordModelTests {
    let usd = Currency(code: "USD", symbol: "$", decimalDigits: 2, isEnabled: true, isEssential: true)

    @Test @MainActor func saveEnabledWhileClassifierWaiting() async {
        // Article V: Save must never be gated on classifier state (confidence = 0 = "waiting")
        let model = makeModel(txRepo: MockTxRepo())
        model.onAppear()
        try? await Task.sleep(nanoseconds: 100_000_000)   // let prefill complete
        model.digitTapped("5")
        model.digitTapped("0")
        model.digitTapped("0")
        #expect(model.state.canSave, "Save must be enabled when amount > 0 regardless of AI (Article V)")
    }

    @Test @MainActor func saveDisabledWhenAmountZero() {
        let model = makeModel(txRepo: MockTxRepo())
        #expect(!model.state.canSave, "Save must be disabled when no digits entered")
    }

    @Test @MainActor func digitTapAccumulates() {
        let model = makeModel(txRepo: MockTxRepo())
        model.digitTapped("1")
        model.digitTapped("2")
        model.digitTapped("3")
        #expect(model.state.digits == "123")
    }

    @Test @MainActor func backspaceTrimLastDigit() {
        let model = makeModel(txRepo: MockTxRepo())
        model.digitTapped("1")
        model.digitTapped("2")
        model.backspaceTapped()
        #expect(model.state.digits == "1")
    }

    @Test @MainActor func backspaceOnEmptyDoesNothing() {
        let model = makeModel(txRepo: MockTxRepo())
        model.backspaceTapped()
        #expect(model.state.digits == "")
    }

    @Test @MainActor func selectTypeChangesState() {
        let model = makeModel(txRepo: MockTxRepo())
        model.selectType(.income)
        #expect(model.state.type == .income)
    }

    @Test @MainActor func saveExpenseSetsTransientEvent() async {
        let repo = MockTxRepo()
        let model = makeModel(txRepo: repo)
        model.selectAccount("acc1")
        model.digitTapped("5")
        model.digitTapped("0")
        model.digitTapped("0")
        model.save(currency: usd)
        try? await Task.sleep(nanoseconds: 100_000_000)
        if case .saved = model.state.transientEvent {
            #expect(true)
        } else {
            #expect(Bool(false), "TransientEvent.saved not set after save")
        }
    }

    @Test @MainActor func undoAfterSaveRevertsTransaction() async {
        let repo = MockTxRepo()
        let model = makeModel(txRepo: repo)
        model.selectAccount("acc1")
        model.digitTapped("1")
        model.digitTapped("0")
        model.digitTapped("0")
        model.save(currency: usd)
        try? await Task.sleep(nanoseconds: 150_000_000)
        model.undo()
        try? await Task.sleep(nanoseconds: 100_000_000)
        let all = await repo.observeAll()
        #expect(all.isEmpty, "Undo must soft-delete saved transaction")
    }
}
