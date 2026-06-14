import Foundation
import BolsilloDomain

@MainActor
@Observable
public final class RecordModel {
    public private(set) var state = RecordState()

    private let suggestCategoryAndAccount: SuggestCategoryAndAccount
    private let recordTransaction: RecordTransaction
    private let recordTransfer: RecordTransfer
    private let undoLastRecord: UndoLastRecord
    private let observeAccountBalances: ObserveAccountBalances
    private let editTransaction: EditTransaction
    private let softDeleteTransaction: SoftDeleteTransaction
    private let restoreTransaction: RestoreTransaction

    // Available accounts and categories for pickers
    public private(set) var accounts: [Account] = []
    public private(set) var categories: [BolsilloDomain.Category] = []

    private var balanceObservationTask: Task<Void, Never>? = nil

    public init(
        suggestCategoryAndAccount: SuggestCategoryAndAccount,
        recordTransaction: RecordTransaction,
        recordTransfer: RecordTransfer,
        undoLastRecord: UndoLastRecord,
        observeAccountBalances: ObserveAccountBalances,
        editTransaction: EditTransaction,
        softDeleteTransaction: SoftDeleteTransaction,
        restoreTransaction: RestoreTransaction
    ) {
        self.suggestCategoryAndAccount = suggestCategoryAndAccount
        self.recordTransaction = recordTransaction
        self.recordTransfer = recordTransfer
        self.undoLastRecord = undoLastRecord
        self.observeAccountBalances = observeAccountBalances
        self.editTransaction = editTransaction
        self.softDeleteTransaction = softDeleteTransaction
        self.restoreTransaction = restoreTransaction
    }

    public func onAppear() {
        Task { await prefill() }
        startObservingBalances()
    }

    public func onDisappear() {
        balanceObservationTask?.cancel()
    }

    // MARK: Amount input

    public func digitTapped(_ digit: String) {
        guard state.digits.count < 10 else { return }
        if digit == "000" {
            guard !state.digits.isEmpty else { return }
            state.digits += "000"
        } else {
            state.digits += digit
        }
        state.transientEvent = nil
    }

    public func backspaceTapped() {
        guard !state.digits.isEmpty else { return }
        state.digits.removeLast()
        state.transientEvent = nil
    }

    // MARK: Type selection

    public func selectType(_ type: RecordEntryType) {
        state.type = type
        state.sameAccountError = false
        state.transientEvent = nil
    }

    // MARK: Category / account selection

    public func selectCategory(_ id: String?) {
        state.categoryId = id
    }

    public func selectAccount(_ id: String) {
        state.accountId = id
        checkSameAccount()
    }

    public func selectDestinationAccount(_ id: String) {
        state.destAccountId = id
        checkSameAccount()
    }

    // MARK: Save

    public func save(currency: Currency, occurredAt: Date = Date()) {
        guard state.canSave else { return }
        state.isSaving = true
        let digits = state.digits
        let type = state.type
        let accountId = state.accountId
        let destAccountId = state.destAccountId
        let categoryId = state.categoryId

        Task {
            defer { state.isSaving = false }
            let parser = MoneyParser()
            let amount = parser.parse(digits: digits, currency: currency)

            switch type {
            case .expense, .income:
                let txType: TransactionType = type == .expense ? .expense : .income
                let draft = TransactionDraft(
                    type: txType,
                    accountId: accountId,
                    amount: amount,
                    currencyCode: currency.code,
                    categoryId: categoryId,
                    occurredAt: occurredAt
                )
                let tx = await recordTransaction(draft)
                state.lastSaved = .single(id: tx.id)
                state.digits = ""
                state.transientEvent = .saved

            case .transfer:
                do {
                    let pair = try await recordTransfer(
                        sourceAccountId: accountId,
                        destAccountId: destAccountId,
                        amount: amount,
                        occurredAt: occurredAt
                    )
                    if let groupId = pair.legSource.transferGroupId {
                        state.lastSaved = .group(transferGroupId: groupId)
                    }
                    state.digits = ""
                    state.transientEvent = .saved
                } catch TransferError.sameAccount {
                    state.sameAccountError = true
                } catch {
                    state.transientEvent = .validationError(error.localizedDescription)
                }
            }
        }
    }

    // MARK: Undo

    public func undo(now: Date = Date()) {
        guard let ref = state.lastSaved else { return }
        let domainRef: SavedRef
        switch ref {
        case .single(let id): domainRef = .single(id: id)
        case .group(let gid): domainRef = .group(transferGroupId: gid)
        }
        Task {
            await undoLastRecord(domainRef, now: now)
            state.lastSaved = nil
            state.transientEvent = .undone
        }
    }

    public func dismissUndo() {
        state.transientEvent = nil
        state.lastSaved = nil
    }

    // MARK: Edit / Delete / Restore

    public func edit(_ transaction: Transaction) {
        Task { await editTransaction(transaction) }
    }

    public func softDelete(id: String, now: Date = Date()) {
        Task { await softDeleteTransaction(id: id, now: now) }
    }

    public func restore(id: String) {
        Task { await restoreTransaction(id: id) }
    }

    // MARK: Private helpers

    private func prefill() async {
        let input = ClassificationInput(
            text: "",
            amount: .zero,
            currencyCode: "USD",
            occurredAt: Date(),
            accountType: "cash"
        )
        let suggestion = await suggestCategoryAndAccount(input)
        state.categoryId = suggestion.categoryId
        state.accountId = suggestion.accountId
        state.confidence = suggestion.confidence
    }

    private func startObservingBalances() {
        balanceObservationTask = Task {
            for await balances in observeAccountBalances() {
                state.balances = balances.mapValues { $0.minorUnits }
            }
        }
    }

    private func checkSameAccount() {
        state.sameAccountError = (
            state.type == .transfer &&
            !state.accountId.isEmpty &&
            state.accountId == state.destAccountId
        )
    }
}
