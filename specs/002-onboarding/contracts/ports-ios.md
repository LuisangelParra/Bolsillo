# Domain ports — Onboarding (002, iOS)

> All ports live in `BolsilloDomain` (pure Swift, no platform deps). Implementations live in `BolsilloData`. UI MUST NOT touch implementations directly (Article VIII). Parity with [ports-android.md](./ports-android.md) — same semantics, idiomatic Swift signatures.

## `SettingsRepository` (NEW)

Persists app-wide settings as key-value rows in the `app_settings` table (schema v2, see [data-model.md](../data-model.md)). The `completeOnboarding(...)` method is the **atomic** entry point used by onboarding.

```swift
import Foundation

public protocol SettingsRepository: Sendable {
    /// Returns the persisted base currency ISO 4217 code, or nil if not set.
    func getBaseCurrency() async -> String?

    /// Hot observation of the base currency. Emits the current value, then on every change.
    func observeBaseCurrency() -> AsyncStream<String?>

    /// Returns the persisted UI language (BCP-47 tag), or nil if not set.
    func getLanguage() async -> String?

    /// Returns true iff app_settings[AppSettingKeys.onboardingCompletedAt] is present.
    func isOnboardingComplete() async -> Bool

    /// Hot observation of the onboarding-complete flag.
    func observeOnboardingComplete() -> AsyncStream<Bool>

    /// ATOMIC. Writes — in one GRDB `write {}` block — the base currency,
    /// language, an optional currency upsert (if `enableCurrencyIfMissing != nil`),
    /// the first account row, and finally the onboarding-complete marker.
    ///
    /// Either all writes land or none do. A throw anywhere rolls back the entire
    /// transaction; the next `isOnboardingComplete()` will return false and
    /// onboarding will be shown again. Caller MUST NOT split this into multiple calls.
    ///
    /// - Throws: `OnboardingError.invalidBaseCurrency`, `.currencyMismatch`,
    ///   `.negativeBalance`, `.unsupportedLanguage`, or any DB error (rolled back).
    func completeOnboarding(
        baseCurrency: String,
        language: String,
        firstAccount: Account,
        enableCurrencyIfMissing: Currency?
    ) async throws
}

public enum OnboardingError: Error {
    case invalidBaseCurrency
    case currencyMismatch
    case negativeBalance
    case unsupportedLanguage
}
```

## `LocaleProvider` (NEW)

Pure abstraction over the device locale so `ResolveDefaultBaseCurrency` stays testable without touching `Foundation.Locale` directly in the domain.

```swift
public protocol LocaleProvider: Sendable {
    /// ISO 3166-1 alpha-2 region code from the active device locale, or nil.
    func regionCode() -> String?
}
```

Implementation `LocaleProviderImpl` lives in `BolsilloData` and wraps `Locale.current.region?.identifier`.

## Existing ports — extensions

### `CurrencyRepository` (already present in 001)

Add one method (no breaking change):

```swift
public protocol CurrencyRepository: Sendable {
    func observeEnabled() -> AsyncStream<[Currency]>        // existing
    func upsert(_ currency: Currency) async                  // existing
    func getByCode(_ code: String) async -> Currency?        // existing

    /// Returns the static ISO 4217 catalog for the "choose another currency" picker.
    /// Excludes USD and COP (already in the seeded enabled list). Loaded from the
    /// bundled `Iso4217Catalog`; never hits the network (Article I).
    func getIso4217Candidates() -> [Currency]                // NEW
}
```

### `AccountRepository` (already present in 001)

**No new methods.** Onboarding writes the first account through the existing `upsert(_:)` — but the call happens **inside** `SettingsRepository.completeOnboarding(...)`'s transaction, not as a separate call.

## Constants — `AppSettingKeys` (NEW, lives in `BolsilloDomain`)

```swift
public enum AppSettingKeys {
    public static let baseCurrency = "base_currency"
    public static let language = "language"
    public static let onboardingCompletedAt = "onboarding_completed_at"
}
```

`BolsilloData` references these constants for the GRDB writes; `FeatureOnboarding` does not (it only sees the `SettingsRepository` API).

## Contract invariants (testable)

1. `completeOnboarding(...)` is **atomic**: a forced throw at any step results in `isOnboardingComplete() == false` AND no `accounts` row added AND no `app_settings` row added.
2. `completeOnboarding(baseCurrency: "EUR", enableCurrencyIfMissing: Currency(code: "EUR", ...))` results in `currencyRepository.observeEnabled()` emitting a list that contains EUR with `isEnabled = true, isEssential = false`, USD and COP still present and essential, USD at index 0, COP at index 1 (after applying `GetEnabledCurrencies` ordering).
3. `completeOnboarding(...)` with `firstAccount.currencyCode != baseCurrency` throws `OnboardingError.currencyMismatch` before any DB write.
4. `observeOnboardingComplete()` emits `false` initially and `true` exactly once after a successful `completeOnboarding(...)`.
