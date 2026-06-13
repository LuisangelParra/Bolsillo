# Contracts — Domain Ports (iOS)

`BolsilloDomain` exposes only these protocols; `BolsilloData` implements them; `FeatureRecord` consumes use cases (never ports directly — Article VIII). Signatures are the contract both the implementation and tests honor. Behavior matches the Android port contract ([ports.md](./ports.md)); only the Swift surface differs. **exists** = in the scaffold; **extend** = add methods; **new** = to create. All ports are `Sendable` (Swift Concurrency).

## ExpenseClassifier (exists — `BolsilloDomain`)
```swift
public protocol ExpenseClassifier: Sendable {
    func suggest(_ input: ClassificationInput) async -> ClassificationResult
    func learn(_ input: ClassificationInput, chosenCategoryId: String) async   // no-op stub in 001
    static var defaultThreshold: Double { get }                                // default 0.75
}
```
- 001 stub `LastUsedExpenseClassifier`: `suggest` → last-used `categoryId`, `confidence = 0.0`, no alternatives. MUST NOT throw, MUST return promptly (≤ 200 ms). `learn` is a no-op (personalization is E4).

## TransactionRepository (extend — `BolsilloDomain`)
```swift
public protocol TransactionRepository: Sendable {
    func observeAll() async -> [Transaction]            // exists — active only (deletedAt == nil)
    func getById(_ id: String) async -> Transaction?    // exists
    func upsert(_ transaction: Transaction) async       // exists — single-row, transactional
    func softDelete(id: String, deletedAt: Date) async  // exists
    func restore(id: String) async                      // exists

    // --- extend for 001 ---
    func lastUsed() async -> Transaction?                                   // most recent active (fallbacks)
    func upsertTransfer(legSource: Transaction, legDest: Transaction) async // atomic pair write
    func softDeleteGroup(transferGroupId: String, deletedAt: Date) async    // both legs together
    func restoreGroup(transferGroupId: String) async                       // both legs together
}
```
**Contract**: no hard-delete method exists; multi-row methods are atomic (one GRDB `write {}`). `observeAll` currently returns a snapshot; reactive streaming for history is E3 — 001 reads via use cases and observes balances through `AccountRepository`.

## AccountRepository (new — `BolsilloDomain`)
```swift
public protocol AccountRepository: Sendable {
    func observeCurrencies() -> AsyncStream<[Currency]>     // essentials always present (USD, COP)
    func observeAccounts() -> AsyncStream<[Account]>        // active (non-archived) accounts
    func getById(_ id: String) async -> Account?
    func observeBalance(accountId: String) -> AsyncStream<Money>   // DERIVED: initial + SUM(signed, active)
    func observeBalances() -> AsyncStream<[String: Money]>         // all accounts' derived balances
}
```
**Contract**: balance is derived per [data-model.md](./data-model.md) Invariant 2 — implementations MUST NOT return a cached/stored balance. `AsyncStream` is the Swift-Concurrency surface bridged from GRDB `ValueObservation` in the data layer (research-ios R2). (Currency observation lives here in 001 to avoid a second port; it may move to a dedicated `CurrencyRepository` in E7/E8.)
