# Contracts — Use Cases (iOS)

New `BolsilloDomain` use-case types. Each is a single-responsibility `struct`/`final class` with constructor-injected ports. The UI (`RecordModel`) depends on these, never on repositories/DB directly (Article VIII). Signatures are indicative; each maps to spec FRs and gets unit tests (Article IX). Behavior matches the Android use-case contract ([use-cases.md](./use-cases.md)).

```swift
// FR 3,4,17 — get pre-fill; never throws, never blocks save
public struct SuggestCategoryAndAccount: Sendable {
    let classifier: ExpenseClassifier
    let transactions: TransactionRepository
    let accounts: AccountRepository
    public func callAsFunction(_ input: ClassificationInput) async -> Suggestion   // category + account, last-used fallback
}

// FR 5,8,14,16,19 — record expense or income (signed), single transactional write
public struct RecordTransaction: Sendable {
    let transactions: TransactionRepository
    public func callAsFunction(_ draft: TransactionDraft) async -> Transaction       // sign applied by type; overdraft allowed
}

// FR 9,10 — build + persist the linked pair atomically
public struct RecordTransfer: Sendable {
    let transactions: TransactionRepository
    let accounts: AccountRepository
    public func callAsFunction(
        sourceAccountId: String,
        destAccountId: String,
        amount: Money,            // magnitude; signs applied to each leg
        occurredAt: Date
    ) async throws -> TransferPair
    // throws TransferError.sameAccount if sourceAccountId == destAccountId (FR 10)
    //        TransferError.crossCurrency if currencies differ (Invariant 6, deferred E8)
}

// FR 11 — edit; recompute by re-deriving balances; keeps transfer pair consistent
public struct EditTransaction: Sendable {
    let transactions: TransactionRepository
    public func callAsFunction(_ updated: Transaction) async
}

// FR 12 — soft delete (single or whole transfer group)
public struct SoftDeleteTransaction: Sendable {
    let transactions: TransactionRepository
    public func callAsFunction(id: String, now: Date) async
}

// FR 13 — restore from trash (single or whole transfer group)
public struct RestoreTransaction: Sendable {
    let transactions: TransactionRepository
    public func callAsFunction(id: String) async
}

// FR 6,7 — undo the just-created record/group via soft delete (within undo window)
public struct UndoLastRecord: Sendable {
    let transactions: TransactionRepository
    public func callAsFunction(_ lastSaved: SavedRef, now: Date) async   // SavedRef = .single(id) | .group(transferGroupId)
}

// FR 6, US-3.2 — derived balances for the UI
public struct ObserveAccountBalances: Sendable {
    let accounts: AccountRepository
    public func callAsFunction() -> AsyncStream<[String: Money]>
}

// FR 14 — float-free parse of keypad input → signed-able Money magnitude
public struct MoneyParser: Sendable {
    public func parse(digits: String, currency: Currency) -> Money   // integer math only; honors decimalDigits
}
```

## Supporting value types (`BolsilloDomain`)
```swift
public struct TransactionDraft: Sendable { /* type, accountId, magnitude amount, currencyCode, categoryId?, occurredAt, merchant?, note? */ }
public struct Suggestion: Sendable { public let categoryId: String?; public let accountId: String; public let confidence: Double }
public struct TransferPair: Sendable { public let legSource: Transaction; public let legDest: Transaction }
public enum SavedRef: Sendable { case single(id: String); case group(transferGroupId: String) }
public enum TransferError: Error, Sendable { case sameAccount; case crossCurrency }
```

## Mapping to acceptance criteria
| Use case | Spec FR / AC |
|---|---|
| `SuggestCategoryAndAccount` | FR 3, 4; Offline & AI fallback AC |
| `RecordTransaction` | FR 5, 8, 14, 16, 19; Happy path, Income, Overdraft AC |
| `RecordTransfer` | FR 9, 10; Transfer AC (incl. same-account, same-currency) |
| `EditTransaction` | FR 11; Edit AC (amount, account change, transfer leg) |
| `SoftDeleteTransaction` / `RestoreTransaction` | FR 12, 13; Delete & restore AC |
| `UndoLastRecord` | FR 6, 7; Undo AC |
| `ObserveAccountBalances` | FR 6, 15; Integrity AC |
| `MoneyParser` | FR 14 |
