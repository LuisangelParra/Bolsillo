# Use cases — Onboarding (002, iOS)

> All use cases live in `BolsilloDomain` and are pure orchestration over ports. Each has one responsibility (Article VIII). Parity with [use-cases-android.md](./use-cases-android.md) — same semantics, idiomatic Swift signatures.

## `IsOnboardingComplete`

```swift
public struct IsOnboardingComplete: Sendable {
    private let settings: SettingsRepository
    public init(settings: SettingsRepository) { self.settings = settings }

    public func callAsFunction() -> AsyncStream<Bool> {
        settings.observeOnboardingComplete()
    }
}
```

**Contract**: Emits `false` until the marker is set; emits `true` afterwards. Drives `BolsilloApp`'s root-view decision (D6 in [research-ios.md](../research-ios.md)).

## `GetEnabledCurrencies`

```swift
public struct GetEnabledCurrencies: Sendable {
    private let currencies: CurrencyRepository
    public init(currencies: CurrencyRepository) { self.currencies = currencies }

    public func callAsFunction() -> AsyncStream<[Currency]> {
        AsyncStream { continuation in
            let task = Task {
                for await list in currencies.observeEnabled() {
                    let usd = list.first { $0.code == "USD" }
                    let cop = list.first { $0.code == "COP" }
                    let others = list
                        .filter { $0.code != "USD" && $0.code != "COP" }
                        .sorted { $0.code < $1.code }
                    var ordered: [Currency] = []
                    if let usd { ordered.append(usd) }
                    if let cop { ordered.append(cop) }
                    ordered.append(contentsOf: others)
                    continuation.yield(ordered)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }
}
```

**Contract**: USD at index 0, COP at index 1, the rest sorted alphabetically by `code`. Pinning is invariant regardless of base choice or device locale (spec FR3). Pure stream transformation — no IO beyond the upstream observation.

## `EnableCurrency`

```swift
public struct EnableCurrency: Sendable {
    private let currencies: CurrencyRepository
    public init(currencies: CurrencyRepository) { self.currencies = currencies }

    /// If `code` is already in the catalog, ensures it's enabled and returns it.
    /// Otherwise inserts a new row with isEnabled=true, isEssential=false using the
    /// Iso4217Candidates metadata for symbol + decimalDigits.
    ///
    /// - Throws: `OnboardingError.invalidBaseCurrency` if `code` is not in the candidate catalog.
    @discardableResult
    public func callAsFunction(_ code: String) async throws -> Currency {
        if let existing = await currencies.getByCode(code) {
            if !existing.isEnabled {
                let enabled = Currency(
                    code: existing.code,
                    symbol: existing.symbol,
                    decimalDigits: existing.decimalDigits,
                    isEnabled: true,
                    isEssential: existing.isEssential
                )
                await currencies.upsert(enabled)
                return enabled
            }
            return existing
        }
        guard let candidate = currencies.getIso4217Candidates().first(where: { $0.code == code }) else {
            throw OnboardingError.invalidBaseCurrency
        }
        let resolved = Currency(
            code: candidate.code,
            symbol: candidate.symbol,
            decimalDigits: candidate.decimalDigits,
            isEnabled: true,
            isEssential: false
        )
        await currencies.upsert(resolved)
        return resolved
    }
}
```

**Contract**: Idempotent. NEVER flips `isEssential`. Throws `OnboardingError.invalidBaseCurrency` if `code` is not in the candidate catalog (UI is expected to only offer codes that are).

## `ResolveDefaultBaseCurrency`

```swift
public struct ResolveDefaultBaseCurrency: Sendable {
    private let locale: LocaleProvider
    public init(locale: LocaleProvider) { self.locale = locale }

    public func callAsFunction() -> String {
        if locale.regionCode()?.uppercased() == "CO" { return "COP" }
        return "USD"
    }
}
```

**Contract**: Pure. Returns `"COP"` iff device region is Colombia, otherwise `"USD"`. (D5 in [research-ios.md](../research-ios.md).)

## `CompleteOnboarding`

The model's terminal command — wraps validation + the atomic write.

```swift
public struct CompleteOnboarding: Sendable {
    private let settings: SettingsRepository
    private let currencies: CurrencyRepository
    private let clock: () -> Date

    public init(
        settings: SettingsRepository,
        currencies: CurrencyRepository,
        clock: @escaping () -> Date = Date.init
    ) {
        self.settings = settings
        self.currencies = currencies
        self.clock = clock
    }

    /// - Parameters:
    ///   - baseCurrencyCode: ISO 4217 (e.g. "USD", "COP", "EUR").
    ///   - initialBalance: Must be non-negative; minor units honor `baseCurrencyCode.decimalDigits`.
    ///   - accountName: Localization key, default `"account.defaultName.cash"`.
    /// - Returns: The persisted `Account` id.
    @discardableResult
    public func callAsFunction(
        baseCurrencyCode: String,
        initialBalance: Money,
        accountName: String = "account.defaultName.cash"
    ) async throws -> String {
        guard !baseCurrencyCode.isEmpty else { throw OnboardingError.invalidBaseCurrency }
        guard initialBalance.minorUnits >= 0 else { throw OnboardingError.negativeBalance }

        // Resolve the currency (enables it in the catalog if it's a new ISO pick).
        let existing = await currencies.getByCode(baseCurrencyCode)
        let baseCurrency: Currency
        let enableIfMissing: Currency?
        if let existing {
            baseCurrency = existing
            enableIfMissing = nil
        } else {
            guard let candidate = currencies.getIso4217Candidates().first(where: { $0.code == baseCurrencyCode }) else {
                throw OnboardingError.invalidBaseCurrency
            }
            baseCurrency = Currency(
                code: candidate.code,
                symbol: candidate.symbol,
                decimalDigits: candidate.decimalDigits,
                isEnabled: true,
                isEssential: false
            )
            enableIfMissing = baseCurrency
        }

        let now = clock().timeIntervalSince1970
        let account = Account(
            id: UUID().uuidString,
            name: accountName,
            type: .cash,
            currencyCode: baseCurrencyCode,
            initialBalance: initialBalance,
            icon: "banknote",
            color: 0xFF7C5CF0,
            archived: false,
            createdAt: now,
            updatedAt: now
        )

        try await settings.completeOnboarding(
            baseCurrency: baseCurrencyCode,
            language: "es",
            firstAccount: account,
            enableCurrencyIfMissing: enableIfMissing
        )
        return account.id
    }
}
```

**Contract**:
- Validates inputs before touching persistence.
- Delegates atomicity to `SettingsRepository.completeOnboarding(...)` — any DB failure rolls back the entire onboarding write set.
- Always persists `language = "es"` in 002 (E12 handles future switching).
- Always creates the account with `type = .cash` and the localized default name key (spec FR5 clarification).

## Sequence — defaults path

```text
WelcomeView
    │ user taps "Usar valores predeterminados"
    ▼
OnboardingModel.onUseDefaults()
    │
    ├─ ResolveDefaultBaseCurrency()                  → "USD" or "COP"
    ├─ Money(minorUnits: 0, decimalDigits: digits)   → initialBalance = 0
    └─ CompleteOnboarding(baseCurrencyCode, initialBalance)
            │
            └─ SettingsRepository.completeOnboarding(...)
                    │  db.queue.write { dbq in
                    │    upsert app_settings[base_currency]
                    │    upsert app_settings[language] = "es"
                    │    (no currency upsert — already seeded)
                    │    insert AccountRecord(first account)
                    │    upsert app_settings[onboarding_completed_at] = now ISO 8601
                    │  }
                    ▼
            yields OnboardingEvent.finishedToRecord
                    ▼
        BolsilloApp swaps root view: OnboardingView → RecordView
        environment(\.locale, Locale(identifier: "es"))
```

## Sequence — custom currency (e.g., EUR)

```text
CurrencyPickerView → IsoCurrencyPickerSheet
    │ user picks EUR
    ▼
EnableCurrency("EUR")                                  → EUR row inserted (enabled, non-essential)
    ▼
back to CurrencyPickerView with EUR selected (USD@0, COP@1, EUR@2)
    │ user taps Continuar → FirstAccountView
    │ user enters 100,00 → Confirm
    ▼
CompleteOnboarding(baseCurrencyCode: "EUR", initialBalance: Money(minorUnits: 10000, decimalDigits: 2))
    │ resolves EUR from catalog (already enabled by EnableCurrency above; enableIfMissing = nil)
    ▼
SettingsRepository.completeOnboarding(
    baseCurrency: "EUR",
    language: "es",
    firstAccount: Account(currencyCode: "EUR", initialBalance: Money(10000, 2), ...),
    enableCurrencyIfMissing: nil
)
```
