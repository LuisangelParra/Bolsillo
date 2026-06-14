# Use cases — Onboarding (002, Android)

> All use cases live in `:domain` and are pure orchestration over ports. Each has one responsibility (Article VIII).

## `IsOnboardingComplete`

```kotlin
class IsOnboardingComplete(
    private val settings: SettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = settings.observeOnboardingComplete()
}
```

**Contract**: Emits `false` until the marker is set; emits `true` afterwards. Drives `MainActivity`'s start-destination decision (D6 in [research.md](../research.md)).

## `GetEnabledCurrencies`

```kotlin
class GetEnabledCurrencies(
    private val currencies: CurrencyRepository,
) {
    operator fun invoke(): Flow<List<Currency>> =
        currencies.observeEnabled().map { list ->
            val (essential, other) = list.partition { it.code == "USD" || it.code == "COP" }
            val usd = essential.firstOrNull { it.code == "USD" }
            val cop = essential.firstOrNull { it.code == "COP" }
            buildList {
                if (usd != null) add(usd)
                if (cop != null) add(cop)
                addAll(other.sortedBy { it.code })
            }
        }
}
```

**Contract**: USD at index 0, COP at index 1, the rest sorted alphabetically by `code`. Pinning is invariant regardless of base choice or device locale (spec FR3). Pure stream transformation — no IO.

## `EnableCurrency`

```kotlin
class EnableCurrency(
    private val currencies: CurrencyRepository,
) {
    /**
     * If [code] is already in the catalog, ensures it's enabled and returns it.
     * Otherwise inserts a new row with isEnabled=true, isEssential=false using the
     * Iso4217Candidates metadata for symbol + decimalDigits.
     */
    suspend operator fun invoke(code: String): Currency {
        val existing = currencies.getByCode(code)
        if (existing != null) {
            if (!existing.isEnabled) currencies.upsert(existing.copy(isEnabled = true))
            return existing.copy(isEnabled = true)
        }
        val candidate = currencies.getIso4217Candidates().first { it.code == code }
        val resolved = candidate.copy(isEnabled = true, isEssential = false)
        currencies.upsert(resolved)
        return resolved
    }
}
```

**Contract**: Idempotent. NEVER flips `isEssential`. Throws `NoSuchElementException` if `code` is not in the candidate catalog (UI is expected to only offer codes that are).

## `ResolveDefaultBaseCurrency`

```kotlin
class ResolveDefaultBaseCurrency(
    private val locale: LocaleProvider,
) {
    operator fun invoke(): String =
        if (locale.regionCode()?.equals("CO", ignoreCase = true) == true) "COP" else "USD"
}
```

**Contract**: Pure. Returns `"COP"` iff device region is Colombia, otherwise `"USD"`. (D5 in [research.md](../research.md).)

## `CompleteOnboarding`

The MVI's terminal command — wraps validation + the atomic write.

```kotlin
class CompleteOnboarding(
    private val settings: SettingsRepository,
    private val currencies: CurrencyRepository,
    private val clock: Clock,
) {
    /**
     * @param baseCurrencyCode ISO 4217 (e.g. "USD", "COP", "EUR").
     * @param initialBalance Must be non-negative; minor units honor [baseCurrencyCode].decimalDigits.
     * @param accountName Localization key, e.g. "account.defaultName.cash".
     * @return The persisted Account id.
     */
    suspend operator fun invoke(
        baseCurrencyCode: String,
        initialBalance: Money,
        accountName: String = "account.defaultName.cash",
    ): String {
        require(baseCurrencyCode.isNotBlank()) { "base currency required" }
        require(initialBalance.minorUnits >= 0) { "initial balance must be >= 0" }

        // Resolve the currency (enables it in the catalog if it's a new ISO pick).
        val baseCurrency = currencies.getByCode(baseCurrencyCode)
            ?: currencies.getIso4217Candidates().first { it.code == baseCurrencyCode }
        val enableIfMissing = if (currencies.getByCode(baseCurrencyCode) == null) {
            baseCurrency.copy(isEnabled = true, isEssential = false)
        } else null

        val account = Account(
            id = UUID.randomUUID().toString(),
            name = accountName,
            type = AccountType.CASH,
            currencyCode = baseCurrencyCode,
            initialBalance = initialBalance,
            icon = "wallet",
            color = 0xFF7C5CF0L,
            archived = false,
            createdAt = clock.now(),
            updatedAt = clock.now(),
        )

        settings.completeOnboarding(
            baseCurrency = baseCurrencyCode,
            language = "es",
            firstAccount = account,
            enableCurrencyIfMissing = enableIfMissing,
        )
        return account.id
    }
}
```

**Contract**:
- Validates inputs before touching persistence.
- Delegates atomicity to `SettingsRepository.completeOnboarding(...)` — any DB failure rolls back the entire onboarding write set.
- Always persists `language = "es"` in 002 (E12 handles future switching).
- Always creates the account with `type = CASH` and the localized default name key (spec FR5 clarification).

## Sequence — defaults path

```text
WelcomeScreen
    │ user taps "Usar valores predeterminados"
    ▼
OnboardingViewModel.onUseDefaults()
    │
    ├─ ResolveDefaultBaseCurrency()                  → "USD" or "COP"
    ├─ Money(0, currency.decimalDigits)              → initialBalance = 0
    └─ CompleteOnboarding(code, Money(0,...))
            │
            └─ SettingsRepository.completeOnboarding(...)
                    │  withTransaction {
                    │    upsert base_currency
                    │    upsert language = "es"
                    │    (no currency upsert — already seeded)
                    │    upsert first Account row
                    │    upsert onboarding_completed_at = clock.now()
                    │  }
                    ▼
            emit OnboardingUiEvent.FinishedToRecord
                    ▼
        MainActivity nav: popUpTo(OnboardingRoute, inclusive=true) → navigate(RecordRoute)
        AppCompatDelegate.setApplicationLocales("es")
```

## Sequence — custom currency (e.g., EUR)

```text
CurrencyPickerScreen → IsoCurrencyPickerSheet
    │ user picks EUR
    ▼
EnableCurrency("EUR")                                 → EUR row inserted (enabled, non-essential)
    ▼
back to CurrencyPickerScreen with EUR selected (USD@0, COP@1, EUR@2)
    │ user taps Continuar → FirstAccountScreen
    │ user enters 100,00 → Confirm
    ▼
CompleteOnboarding("EUR", Money(10000, 2))
    │ resolves EUR from catalog (already enabled by EnableCurrency above; enableIfMissing = null)
    ▼
SettingsRepository.completeOnboarding(
    baseCurrency = "EUR",
    language = "es",
    firstAccount = Account(currencyCode = "EUR", initialBalance = Money(10000,2), ...),
    enableCurrencyIfMissing = null,
)
```
