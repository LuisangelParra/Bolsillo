import Testing
import Foundation
import BolsilloDomain

@Suite struct RecordTransactionTests {

    // MARK: Signing invariants (Constitution Article III — Invariant 1)

    @Test func expenseIsSignedNegative() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .expense,
            accountId: "a1",
            amount: Money(minorUnits: 1000),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.amount.minorUnits == -1000, "Expense amount must be negative (Invariant 1)")
    }

    @Test func incomeIsSignedPositive() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .income,
            accountId: "a1",
            amount: Money(minorUnits: 500),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.amount.minorUnits == 500, "Income amount must be positive (Invariant 1)")
    }

    @Test func transferSingleLegIsSignedNegative() async {
        // Single-leg transfer (source) is negative (Invariant 1); atomic pairs use RecordTransfer
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .transfer,
            accountId: "a1",
            amount: Money(minorUnits: 300),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.amount.minorUnits == -300, "Transfer single-leg must be negative (Invariant 1)")
    }

    // MARK: FX rate frozen at creation (Constitution Article III — Invariant 8)

    @Test func fxRateIsFrozenAtCreation() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .expense,
            accountId: "a1",
            amount: Money(minorUnits: 2000),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.fxRateMillis == 1000,
                "Same-currency fxRateMillis must be frozen at 1000 (Invariant 8)")
        #expect(tx.amountBase.minorUnits == tx.amount.minorUnits,
                "amountBase must equal amount for same-currency (Invariant 8)")
    }

    // MARK: Overdraft allowed (FR 19)

    @Test func overdraftIsAllowed() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .expense,
            accountId: "a1",
            amount: Money(minorUnits: 99_999_999),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.amount.minorUnits == -99_999_999, "Overdraft must be allowed — no balance check (FR 19)")
    }

    // MARK: Persistence

    @Test func transactionIsPersistedAfterRecord() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .expense,
            accountId: "a1",
            amount: Money(minorUnits: 100),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        let fetched = await repo.getById(tx.id)
        #expect(fetched != nil, "Transaction must be persisted after RecordTransaction")
        #expect(fetched?.id == tx.id)
    }

    @Test func transactionIsNotSoftDeletedOnCreation() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .income,
            accountId: "a1",
            amount: Money(minorUnits: 50),
            currencyCode: "USD",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.isDeleted == false, "New transaction must not be soft-deleted")
    }

    @Test func categoryAndMerchantArePreserved() async {
        let repo = MockTransactionRepository()
        let useCase = RecordTransaction(transactions: repo)
        let draft = TransactionDraft(
            type: .expense,
            accountId: "a1",
            amount: Money(minorUnits: 450),
            currencyCode: "USD",
            categoryId: "food.coffee",
            merchant: "Starbucks",
            occurredAt: Date()
        )
        let tx = await useCase(draft)
        #expect(tx.categoryId == "food.coffee")
        #expect(tx.merchant == "Starbucks")
    }
}
