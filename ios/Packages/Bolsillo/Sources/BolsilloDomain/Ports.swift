import Foundation

/// Domain port for transactions. Implemented in the data layer; the domain depends
/// only on this protocol (Constitution Article VIII).
///
/// Note: there is intentionally NO hard-delete method. Removal is `softDelete` only
/// (Constitution Article III — no hard deletes).
public protocol TransactionRepository: Sendable {
    func observeAll() async -> [Transaction]
    func getById(_ id: String) async -> Transaction?
    func upsert(_ transaction: Transaction) async
    func softDelete(id: String, deletedAt: Date) async
    func restore(id: String) async
}

/// Input features for on-device categorization (Constitution Article V).
public struct ClassificationInput: Sendable {
    public let text: String
    public let amount: Money
    public let currencyCode: String
    public let occurredAt: Date
    public let accountType: String

    public init(text: String, amount: Money, currencyCode: String, occurredAt: Date, accountType: String) {
        self.text = text
        self.amount = amount
        self.currencyCode = currencyCode
        self.occurredAt = occurredAt
        self.accountType = accountType
    }
}

/// Suggested category with calibrated confidence and up to top-3 alternatives.
public struct ClassificationResult: Sendable {
    public let topCategoryId: String?
    public let confidence: Double
    public let alternatives: [String]

    public init(topCategoryId: String?, confidence: Double, alternatives: [String]) {
        self.topCategoryId = topCategoryId
        self.confidence = confidence
        self.alternatives = alternatives
    }
}

/// Domain port for the AI categorization cascade (user rules → merchant dictionary →
/// on-device ML). The AI is accessed ONLY through this protocol and is replaceable
/// without touching the domain (Constitution Articles V & VIII). It MUST never block saving.
public protocol ExpenseClassifier: Sendable {
    func suggest(_ input: ClassificationInput) async -> ClassificationResult
    func learn(_ input: ClassificationInput, chosenCategoryId: String) async

    /// Default confidence threshold for auto-apply (Constitution Article V).
    static var defaultThreshold: Double { get }
}

public extension ExpenseClassifier {
    static var defaultThreshold: Double { 0.75 }
}
