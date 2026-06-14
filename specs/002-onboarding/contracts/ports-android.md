# Domain ports — Onboarding (002, Android)

> All ports live in `:domain` (pure Kotlin, no Android deps). Implementations live in `:data`. UI MUST NOT touch ports' implementations directly (Article VIII).

## `SettingsRepository` (NEW)

Persists app-wide settings as key-value rows in the `app_settings` table (schema v2, see [data-model.md](../data-model.md)). The `completeOnboarding(...)` method is the **atomic** entry point used by onboarding.

```kotlin
package com.bolsillo.domain.port

import com.bolsillo.domain.model.Account
import com.bolsillo.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Returns the persisted base currency ISO 4217 code, or null if not set. */
    suspend fun getBaseCurrency(): String?

    /** Hot observation of the base currency. Emits the current value, then on every change. */
    fun observeBaseCurrency(): Flow<String?>

    /** Returns the persisted UI language (BCP-47 tag), or null if not set. */
    suspend fun getLanguage(): String?

    /** Returns true iff app_settings[onboarding_completed_at] is present. */
    suspend fun isOnboardingComplete(): Boolean

    /** Hot observation of the onboarding-complete flag. */
    fun observeOnboardingComplete(): Flow<Boolean>

    /**
     * ATOMIC. Writes — in one Room withTransaction { } block — the base currency,
     * language, an optional currency upsert (if [enableCurrencyIfMissing] != null),
     * the first account row, and finally the onboarding-complete marker.
     *
     * Either all writes land or none do. A failure anywhere rolls back the entire
     * transaction; the next isOnboardingComplete() will return false and onboarding
     * will be shown again. Caller MUST NOT split this into multiple calls.
     *
     * @throws IllegalArgumentException if [baseCurrency] is empty, [firstAccount.currencyCode]
     *   != [baseCurrency], [firstAccount.initialBalance.minorUnits] < 0, or [language] is not "es".
     */
    suspend fun completeOnboarding(
        baseCurrency: String,
        language: String,
        firstAccount: Account,
        enableCurrencyIfMissing: Currency?,
    )
}
```

## `LocaleProvider` (NEW)

Pure abstraction over the device locale so `ResolveDefaultBaseCurrency` stays testable.

```kotlin
package com.bolsillo.domain.port

interface LocaleProvider {
    /** ISO 3166-1 alpha-2 region code from the active device locale, or null. */
    fun regionCode(): String?
}
```

## Existing ports — extensions

### `CurrencyRepository` (already present in 001)

Add one method (no breaking change):

```kotlin
interface CurrencyRepository {
    fun observeEnabled(): Flow<List<Currency>>   // existing
    suspend fun upsert(currency: Currency)        // existing
    suspend fun getByCode(code: String): Currency? // existing

    /**
     * Returns the static ISO 4217 catalog for the "choose another currency" picker.
     * Excludes USD and COP (already in the seeded enabled list). Loaded from the
     * bundled Iso4217Catalog asset; never hits the network (Article I).
     */
    fun getIso4217Candidates(): List<Currency>    // NEW
}
```

### `AccountRepository` (already present in 001)

**No new methods.** Onboarding writes the first account through the existing `upsert(account)` — but the call happens **inside** `SettingsRepository.completeOnboarding(...)`'s transaction, not as a separate call.

## Constants — `AppSettingKeys` (NEW, lives in `:domain`)

```kotlin
package com.bolsillo.domain.model

object AppSettingKeys {
    const val BASE_CURRENCY = "base_currency"
    const val LANGUAGE = "language"
    const val ONBOARDING_COMPLETED_AT = "onboarding_completed_at"
}
```

`:data` references these constants for the Room writes; `:feature-onboarding` does not (it only sees the `SettingsRepository` API).

## Contract invariants (testable)

1. `completeOnboarding(...)` is **atomic**: a forced failure at any step results in `isOnboardingComplete() == false` AND no `accounts` row added AND no `app_settings` row added.
2. `completeOnboarding(baseCurrency = "EUR", enableCurrencyIfMissing = Currency("EUR", ...))` results in `currencyRepository.observeEnabled()` emitting a list that contains EUR with `isEnabled = true, isEssential = false`, USD and COP still present and essential, USD at index 0, COP at index 1 (after applying `GetEnabledCurrencies` ordering).
3. `completeOnboarding(...)` with `firstAccount.currencyCode != baseCurrency` throws `IllegalArgumentException` before any DB write.
4. `observeOnboardingComplete()` emits `false` initially and `true` exactly once after a successful `completeOnboarding(...)`.
