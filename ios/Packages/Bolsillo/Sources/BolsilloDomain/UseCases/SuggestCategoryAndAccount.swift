import Foundation

/// Invokes the AI categorization cascade and resolves a best-guess account,
/// falling back gracefully so that saving is never blocked
/// (Constitution Article V — AI must never block saving).
///
/// Confidence cascade:
/// 1. Classifier top result (if any)
/// 2. Last-used category from most recent transaction
/// 3. `nil` (user picks manually)
///
/// Account cascade:
/// 1. Account of most recent transaction
/// 2. First active account from `AccountRepository`
/// 3. Empty string (UI must handle)
public struct SuggestCategoryAndAccount: Sendable {
    private let classifier: any ExpenseClassifier
    private let transactions: any TransactionRepository
    private let accounts: any AccountRepository

    public init(
        classifier: some ExpenseClassifier,
        transactions: some TransactionRepository,
        accounts: some AccountRepository
    ) {
        self.classifier = classifier
        self.transactions = transactions
        self.accounts = accounts
    }

    public func callAsFunction(_ input: ClassificationInput) async -> Suggestion {
        // Fire classifier and last-used fetch concurrently
        async let classResult = classifier.suggest(input)
        async let lastTx = transactions.lastUsed()

        let (result, last) = await (classResult, lastTx)

        // Category cascade
        let categoryId = result.topCategoryId ?? last?.categoryId

        // Account cascade
        let accountId: String
        if let lastId = last?.accountId {
            accountId = lastId
        } else {
            accountId = await firstAccountId()
        }

        return Suggestion(categoryId: categoryId, accountId: accountId, confidence: result.confidence)
    }

    /// Pulls the first emission from the accounts stream and returns the first account's id.
    private func firstAccountId() async -> String {
        for await list in accounts.observeAccounts() {
            return list.first?.id ?? ""
        }
        return ""
    }
}
