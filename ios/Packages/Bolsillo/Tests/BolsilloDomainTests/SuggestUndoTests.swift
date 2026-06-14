import Testing
import Foundation
import BolsilloDomain

@Suite struct SuggestUndoTests {

    // MARK: SuggestCategoryAndAccount — last-used fallback

    @Test func lastUsedCategoryFallback() async {
        let repo = MockTransactionRepository()

        // Seed a prior transaction with a known category
        let now = Date()
        await repo.upsert(Transaction(
            id: "prev1",
            accountId: "acc1",
            type: .expense,
            amount: Money(minorUnits: -500),
            currencyCode: "USD",
            amountBase: Money(minorUnits: -500),
            fxRateMillis: 1000,
            categoryId: "food.coffee",
            merchant: nil,
            note: nil,
            occurredAt: now,
            transferGroupId: nil,
            createdAt: now,
            updatedAt: now
        ))

        let useCase = SuggestCategoryAndAccount(
            classifier: AlwaysEmptyClassifier(),
            transactions: repo,
            accounts: MockAccountRepository()
        )

        let input = ClassificationInput(
            text: "",
            amount: .zero,
            currencyCode: "USD",
            occurredAt: Date(),
            accountType: "cash"
        )
        let suggestion = await useCase(input)
        #expect(suggestion.categoryId == "food.coffee",
                "Should fall back to last-used category when classifier returns nil (Constitution Article V)")
    }

    @Test func classifierConfidenceIsPreferred() async {
        let repo = MockTransactionRepository()

        // Seed a prior transaction with a different category
        let now = Date()
        await repo.upsert(Transaction(
            id: "prev1",
            accountId: "acc1",
            type: .expense,
            amount: Money(minorUnits: -1000),
            currencyCode: "USD",
            amountBase: Money(minorUnits: -1000),
            fxRateMillis: 1000,
            categoryId: "transport",
            merchant: nil,
            note: nil,
            occurredAt: now,
            transferGroupId: nil,
            createdAt: now,
            updatedAt: now
        ))

        // Classifier that returns a different (higher-priority) category
        struct SpecificClassifier: ExpenseClassifier {
            static let defaultThreshold: Double = 0.75
            func suggest(_ input: ClassificationInput) async -> ClassificationResult {
                ClassificationResult(topCategoryId: "food.restaurants", confidence: 0.9, alternatives: [])
            }
            func learn(_ input: ClassificationInput, chosenCategoryId: String) async {}
        }

        let useCase = SuggestCategoryAndAccount(
            classifier: SpecificClassifier(),
            transactions: repo,
            accounts: MockAccountRepository()
        )

        let input = ClassificationInput(
            text: "McDonald's",
            amount: Money(minorUnits: 800),
            currencyCode: "USD",
            occurredAt: Date(),
            accountType: "cash"
        )
        let suggestion = await useCase(input)
        #expect(suggestion.categoryId == "food.restaurants",
                "Classifier result must take priority over last-used fallback (Constitution Article V)")
        #expect(suggestion.confidence == 0.9)
    }

    @Test func noHistoryNilCategoryIsOk() async {
        let repo = MockTransactionRepository()
        // Empty repo — no prior transactions

        let useCase = SuggestCategoryAndAccount(
            classifier: AlwaysEmptyClassifier(),
            transactions: repo,
            accounts: MockAccountRepository()
        )

        let input = ClassificationInput(
            text: "",
            amount: .zero,
            currencyCode: "USD",
            occurredAt: Date(),
            accountType: "cash"
        )
        let suggestion = await useCase(input)
        // nil is valid — UI must handle it, saving is never blocked
        #expect(suggestion.categoryId == nil,
                "nil categoryId is acceptable when classifier and history are both empty (Article V)")
    }

    // MARK: UndoLastRecord — soft-delete, no hard-delete

    @Test func undoSingleTransactionSoftDeletes() async {
        let repo = MockTransactionRepository()
        let record = RecordTransaction(transactions: repo)
        let undo = UndoLastRecord(transactions: repo)

        let draft = TransactionDraft(
            type: .expense,
            accountId: "a1",
            amount: Money(minorUnits: 200),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await record(draft)

        await undo(.single(id: tx.id), now: Date())

        let fetched = await repo.getById(tx.id)
        #expect(fetched?.isDeleted == true,
                "Undo must soft-delete, not hard-delete (Constitution Article III)")
        #expect(fetched?.deletedAt != nil)
    }

    @Test func undoDoesNotRemoveRecordFromStorage() async {
        let repo = MockTransactionRepository()
        let record = RecordTransaction(transactions: repo)
        let undo = UndoLastRecord(transactions: repo)

        let draft = TransactionDraft(
            type: .income,
            accountId: "a1",
            amount: Money(minorUnits: 750),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await record(draft)

        await undo(.single(id: tx.id), now: Date())

        // Record still exists in storage (soft delete only)
        let fetched = await repo.getById(tx.id)
        #expect(fetched != nil, "Soft-deleted record must remain in storage (no hard-delete, Article III)")
    }

    @Test func undoGroupSoftDeletesBothTransferLegs() async {
        let txRepo = MockTransactionRepository()
        let accRepo = MockAccountRepository(accounts: [
            Account(id: "src", name: "Cash",    type: .cash,    currencyCode: "USD",
                    initialBalance: .zero, icon: "", color: 0, archived: false,
                    createdAt: Date(), updatedAt: Date()),
            Account(id: "dst", name: "Savings", type: .savings, currencyCode: "USD",
                    initialBalance: .zero, icon: "", color: 0, archived: false,
                    createdAt: Date(), updatedAt: Date()),
        ])

        let recordTransfer = RecordTransfer(transactions: txRepo, accounts: accRepo)
        let undo = UndoLastRecord(transactions: txRepo)

        let pair = try! await recordTransfer(
            sourceAccountId: "src",
            destAccountId: "dst",
            amount: Money(minorUnits: 1000),
            occurredAt: Date()
        )

        let groupId = pair.legSource.transferGroupId!
        await undo(.group(transferGroupId: groupId), now: Date())

        let src = await txRepo.getById(pair.legSource.id)
        let dst = await txRepo.getById(pair.legDest.id)
        #expect(src?.isDeleted == true, "Source transfer leg must be soft-deleted on group undo")
        #expect(dst?.isDeleted == true, "Destination transfer leg must be soft-deleted on group undo")
    }
}
