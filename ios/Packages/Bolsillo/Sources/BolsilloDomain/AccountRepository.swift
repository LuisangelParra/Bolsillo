import Foundation

/// Domain port for accounts and their derived balances. Implemented in the data layer;
/// the domain depends only on this protocol (Constitution Article VIII).
///
/// Balances are derived by the data layer from `initialBalance + SUM(transactions)`
/// (Constitution Article III — Invariant 2). The domain never computes them directly.
public protocol AccountRepository: Sendable {
    /// Emits the full list of enabled currencies whenever it changes.
    func observeCurrencies() -> AsyncStream<[Currency]>

    /// Emits the full list of accounts (including archived) whenever any account changes.
    func observeAccounts() -> AsyncStream<[Account]>

    /// Single-shot fetch of one account by id. Returns `nil` if not found.
    func getById(_ id: String) async -> Account?

    /// Emits the running balance of a single account whenever its transactions change.
    func observeBalance(accountId: String) -> AsyncStream<Money>

    /// Emits a map of accountId → balance for all accounts whenever any balance changes.
    func observeBalances() -> AsyncStream<[String: Money]>
}
