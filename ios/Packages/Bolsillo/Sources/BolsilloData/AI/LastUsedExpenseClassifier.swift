import Foundation
import BolsilloDomain

public final class LastUsedExpenseClassifier: ExpenseClassifier, @unchecked Sendable {
    private let transactions: any TransactionRepository

    public static let defaultThreshold: Double = 0.75

    public init(transactions: some TransactionRepository) {
        self.transactions = transactions
    }

    public func suggest(_ input: ClassificationInput) async -> ClassificationResult {
        let last = await transactions.lastUsed()
        return ClassificationResult(
            topCategoryId: last?.categoryId,
            confidence: 0.0,      // stub: R8 — real ML is spec 003
            alternatives: []
        )
    }

    public func learn(_ input: ClassificationInput, chosenCategoryId: String) async {
        // no-op: personalization is E4 (spec 003)
    }
}
