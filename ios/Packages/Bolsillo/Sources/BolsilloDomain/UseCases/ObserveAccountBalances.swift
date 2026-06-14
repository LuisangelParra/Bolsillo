import Foundation

/// Returns a live stream of account balances keyed by account id.
///
/// Balances are derived by the data layer (initialBalance + SUM of non-deleted
/// transactions). This use case is a thin delegation to `AccountRepository` so
/// the presentation layer never takes a direct dependency on the port
/// (Constitution Article VIII — clean architecture).
public struct ObserveAccountBalances: Sendable {
    private let accounts: any AccountRepository

    public init(accounts: some AccountRepository) {
        self.accounts = accounts
    }

    /// Start observing all account balances.
    /// - Returns: An `AsyncStream` that emits a `[accountId: Money]` map on every change.
    public func callAsFunction() -> AsyncStream<[String: Money]> {
        accounts.observeBalances()
    }
}
