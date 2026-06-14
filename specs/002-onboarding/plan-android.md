# Implementation Plan (Android): Onboarding and initial setup

**Feature**: `002-onboarding` | **Platform**: Android | **Date**: 2026-06-13
**Spec**: [spec.md](./spec.md) · **Constitution**: `.specify/memory/constitution.md`
**Companion artifacts**: [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/) · [quickstart.md](./quickstart.md)

## Summary

Deliver US-1.1 / US-1.2 / US-1.3 natively on Android: a new user, with no account / no email / no network, reaches the existing record surface (feature 001) in ≤ 60 s. Onboarding picks the **base currency** (USD/COP pinned at top + "choose another" ISO 4217), creates **exactly one** first account (type `cash`, name key `account.defaultName.cash`, currency = base, initial balance default `0`), silently persists `language = es`, and writes an **atomic** onboarding-complete marker so subsequent launches go straight to recording. Reuses 001's `:domain` (`Account`, `Currency`, `AccountRepository`) and `:data` (Room schema v1, `CurrencySeed` with USD+COP essential). New `:feature-onboarding` Compose module orchestrated by an MVI `OnboardingViewModel`. A new `SettingsRepository` port lives in `:domain` and is backed by a Room `AppSetting` key-value table — so the four onboarding writes (`base_currency`, `language`, first account, `onboarding_completed_at`) land in **one** Room `withTransaction { }` block, satisfying Article III atomicity. UI maps directly to the canonical design (`docs/design/Bolsillo.dc.html`) via the existing `:designsystem` (`shared-assets/design/tokens.json` + `design-system.md`); no literal colors/sizes in composables.

## Technical Context

**Language/Version**: Kotlin 2.0.21 (JDK 17 toolchain; CI on JDK 21)
**Primary Dependencies**: Jetpack Compose (BOM 2024.12.01, Material 3) via `:designsystem`, Hilt, Room 2.6.1 (+SQLCipher 4.6.1 from 001), kotlinx-coroutines 1.9.0 (Flow), Navigation-Compose for the onboarding ↔ record route switch
**Storage**: Reuses 001's encrypted Room DB; adds one new table `app_settings(key TEXT PRIMARY KEY, value TEXT)` via a v2 migration. No DataStore Preferences (would split atomicity across two stores — see research.md)
**Design source**: `shared-assets/design/tokens.json` + `shared-assets/design/design-system.md`; canonical pixels in `docs/design/Bolsillo.dc.html`. All onboarding composables consume `BolsilloTheme` from `:designsystem` — no literal colors/sizes (see **Design mapping** below)
**Testing**: JUnit + kotlinx-coroutines-test + Turbine (domain JVM unit); Room in-memory for `AppSettingDao` + migration v1→v2 tests; Compose UI test (`androidTest`) for the defaults path, currency order, USD/COP non-removable, no-language-selector, re-launch routing
**Target Platform**: Android `minSdk` 26 / `compileSdk` 35 / `targetSdk` 35
**Project Type**: Native mobile app (single module-per-layer Gradle project under `android/`)
**Performance Goals**: cold start ≤ 1.5 s into the onboarding entry screen; onboarding-complete atomic write ≤ 100 ms (Article IV); defaults-path wall-clock ≤ 60 s (spec §10); no loading screen mid-flow
**Constraints**: 100% offline; integer minor units only (initial balance is `Money`); soft delete only on any later changes (E3); transactional + reconciling writes; `es` silently persisted, en switchable later (E12); no hard-coded strings
**Scale/Scope**: Single-user device; one new module + one new domain port + one schema migration

**Unknowns resolved in [research.md](./research.md)**: settings-store choice (Room AppSetting vs DataStore), atomic write strategy across settings + account, first-launch routing signal, currency-list-order data shape, locale-derived default base currency. No `NEEDS CLARIFICATION` remain.

## Constitution Check

*GATE: must pass before Phase 0 and re-checked after Phase 1. All nine articles below (governance requires an explicit per-article statement).*

| Article | Status | How this Android design satisfies it |
|---|---|---|
| **I — Local-first / offline** | ✔ | All reads/writes hit the existing on-device Room DB; no network in any onboarding path (no FX fetch, no remote currency list). Airplane-mode is the default test mode. ISO 4217 "choose another" picker reads from a bundled in-app list (`shared-assets/i18n` + a static `Iso4217Catalog`), not a remote service. |
| **II — Privacy** | ✔ | No sign-up, email, phone, social login, or account creation. Zero PII collection. No telemetry of entered values (currency choice, balance, name). All writes stay on the device-encrypted Room DB (SQLCipher key from 001). |
| **III — Financial integrity** | ✔ | Initial balance handled as `Money(minorUnits: Long)` (no float, including the keypad parser — reuses 001's `MoneyParser`). The four onboarding writes (`base_currency`, `language`, first account row, `onboarding_completed_at`) execute inside **one** Room `withTransaction { }` block ⇒ either all four land or none, so a crash mid-onboarding leaves zero partial state. Soft-delete rules carry over to any later edits in E3. |
| **IV — Speed** | ✔ | Cold start ≤ 1.5 s into the welcome screen (single composition, no IO on the main thread, no spinner). The defaults-path completion write is a single transaction ≤ 100 ms. No loading screen anywhere in the flow. Navigation to the record surface is immediate (route swap, not re-launch). |
| **V — AI suggests, never blocks** | N/A | Onboarding does not touch the `ExpenseClassifier` port. Recording's AI behavior is unchanged. |
| **VI — Native per platform, spec is source of truth** | ✔ | This is the Android plan only; behavior derives from the platform-neutral spec. UI is built from the shared design system (`tokens.json`, `design-system.md`) and the canonical mock (`docs/design/Bolsillo.dc.html`); divergence from iOS is fixed in `spec.md` first. |
| **VII — Localization & currency** | ✔ | `language = es` silently persisted; no language UI control anywhere in onboarding (spec FR7). USD + COP pinned at top of the picker, badged essential, non-removable (spec FR3). Any user-added ISO 4217 currency appears below them and is enabled but not essential. Initial balance honors `Currency.decimalDigits` (USD/COP = 2). All onboarding strings live in `res/values/strings.xml` (es) + `res/values-en/strings.xml` (en), generated from `shared-assets/i18n/{es,en}.json` (the keys defined there, not hand-authored per platform). Zero hard-coded UI strings. |
| **VIII — Clean architecture** | ✔ | `presentation (:feature-onboarding, :designsystem) → domain (:domain) ← data (:data)`. New `SettingsRepository` port lives in `:domain`; its Room-backed implementation lives in `:data` (the UI never touches the DB). `:domain` stays Android-free. `:feature-onboarding` depends on `:domain` + `:designsystem` only — never on `:data`. |
| **IX — Tests / no data loss** | ✔ | Unit tests for atomic onboarding completion (crash mid-write → zero partial state); migration v1→v2 test that adds `app_settings` table without losing existing transactions/accounts (release blocked on data loss); Compose UI tests for defaults path, currency order, USD/COP non-removable, no-language-selector, re-launch routing; es+en rendering test. |

**Result: PASS** (no violations; Complexity Tracking table omitted — nothing to justify).

## Project Structure

### Documentation (this feature)

```text
specs/002-onboarding/
├── spec.md                 # Platform-neutral spec (done, clarified 2026-06-13)
├── plan-android.md         # This file
├── research.md             # Phase 0 — decisions
├── data-model.md           # Phase 1 — entities, invariants, schema v2 delta
├── contracts/              # Phase 1 — domain ports + use-case signatures
│   ├── ports-android.md
│   └── use-cases-android.md
├── quickstart.md           # Phase 1 — build/validate guide
├── checklists/requirements.md
└── tasks-android.md        # Phase 2 — produced by /speckit-tasks (NOT here)
```

### Source Code (Android — `android/`, modules already scaffolded from 001)

```text
android/
├── domain/                 # Pure Kotlin (no Android deps)
│   └── src/main/kotlin/com/bolsillo/domain/
│       ├── model/          # Money, Currency, Account (exist from 001) + AppSetting keys (new const file)
│       ├── port/           # AccountRepository (exist), CurrencyRepository (exist or extracted) + NEW SettingsRepository
│       └── usecase/        # NEW: CompleteOnboarding, IsOnboardingComplete, GetEnabledCurrencies,
│                           #      EnableCurrency, ResolveDefaultBaseCurrency
├── data/                   # Room + SQLCipher + DI (exists)
│   └── src/main/kotlin/com/bolsillo/data/
│       ├── db/             # +entity/AppSettingEntity.kt, +dao/AppSettingDao.kt,
│       │                   #   +BolsilloMigrations.MIGRATION_1_2 (add `app_settings` table)
│       ├── repository/     # +RoomSettingsRepository (impl of SettingsRepository)
│       ├── currency/       # Iso4217Catalog (NEW: static bundled list for the "choose another" picker;
│       │                   #   reads `shared-assets/currency/iso4217.json` or a generated Kt file)
│       ├── seed/           # AccountSeed STAYS for tests/fallback but is NO LONGER called by AppSeeder
│       │                   #   on first launch (onboarding now creates the first account). CurrencySeed
│       │                   #   remains the source of truth for USD/COP essential.
│       └── di/             # DataModule extension: bind SettingsRepository; provide AppSettingDao
├── designsystem/           # Unchanged — onboarding consumes existing tokens
├── feature-record/         # Unchanged
├── feature-onboarding/     # NEW MODULE (Compose + MVI)
│   └── src/main/kotlin/com/bolsillo/feature/onboarding/
│       ├── ui/             # OnboardingRoute, WelcomeScreen, CurrencyPickerScreen,
│       │                   #   FirstAccountScreen, CurrencyRow, IsoCurrencyPickerSheet
│       └── presentation/   # OnboardingViewModel (MVI), OnboardingUiState, OnboardingIntent,
│                           #   OnboardingUiEvent (FinishedToRecord)
├── app/                    # Entry: MainActivity observes IsOnboardingComplete; routes to
│                           # OnboardingRoute or RecordRoute (NavHost or conditional setContent)
└── core/                   # Unchanged
```

**Structure Decision**: Add **one** new presentation module, `:feature-onboarding`, mirroring `:feature-record`'s shape (one MVI ViewModel, one route, screens under `ui/`). Settings persistence lives in **Room** (new `app_settings` table) rather than DataStore Preferences so the four onboarding writes share a single transaction — see research.md decision D1. New domain `SettingsRepository` port keeps `:feature-onboarding` from depending on `:data`. Existing `AccountSeed.defaultCash(...)` is **kept as a constructor** (callable for tests / future fallback) but **`AppSeeder` no longer creates the default Cash account on first launch** — onboarding owns first-account creation now. `CurrencySeed` is unchanged: USD + COP remain essential and pre-loaded. Must add `include(":feature-onboarding")` to `settings.gradle.kts`.

## Approach by layer

### Domain (`:domain`)
- **No new entity types**: reuses `Account`, `Currency`, `Money`, `AccountType` from 001.
- **New port `SettingsRepository`** (full signature in [contracts/ports-android.md](./contracts/ports-android.md)):
  - `suspend fun getBaseCurrency(): String?`
  - `fun observeBaseCurrency(): Flow<String?>`
  - `suspend fun getLanguage(): String?`
  - `suspend fun isOnboardingComplete(): Boolean`
  - `fun observeOnboardingComplete(): Flow<Boolean>`
  - `suspend fun completeOnboarding(baseCurrency: String, language: String, firstAccount: Account, enableCurrencyIfMissing: Currency?)` — **atomic**, all-or-nothing. Implementation wraps every write in a single Room `withTransaction { }`.
- **New use cases** (one responsibility each, constructor-injected ports):
  - `IsOnboardingComplete(settings)` → `Flow<Boolean>` for app routing.
  - `GetEnabledCurrencies(currencies)` → returns the list with **USD pinned at index 0, COP at index 1**, then remaining enabled currencies sorted by code. Pinning logic centralized here so the UI never has to know.
  - `EnableCurrency(currencies, code)` → if the picked ISO 4217 code is not already in the catalog, insert it `enabled = true, essential = false`; if present, mark `enabled = true`. Returns the resolved `Currency`. Idempotent.
  - `ResolveDefaultBaseCurrency(localeProvider)` → returns `"COP"` if the device region is `CO`, else `"USD"` (spec §12 assumption). Pure function over a `LocaleProvider` port (single method `regionCode(): String?`).
  - `CompleteOnboarding(settings, accounts, currencies, clock)` → orchestrates: validates inputs (currency code is enabled / will-be-enabled; account currency == base; initial balance ≥ 0 minor units; language is `es` for now), then calls `settings.completeOnboarding(...)`. Never throws on a partial state — that is the repository's atomicity contract.

### Data (`:data`)
- **Schema v2 migration** (`MIGRATION_1_2`):
  ```sql
  CREATE TABLE IF NOT EXISTS app_settings (
      key TEXT PRIMARY KEY NOT NULL,
      value TEXT NOT NULL
  );
  ```
  Additive only — no data loss. Tested with sample rows in existing tables (Article IX).
- **`AppSettingEntity`** (`key: String`, `value: String`) + **`AppSettingDao`** (`get(key)`, `observe(key)`, `upsert(key, value)`).
- **`RoomSettingsRepository`** implements `SettingsRepository`. `completeOnboarding(...)` runs inside `db.withTransaction { }`:
  1. `appSettingDao.upsert(BASE_CURRENCY_KEY, baseCurrency)`
  2. `appSettingDao.upsert(LANGUAGE_KEY, language)`
  3. `enableCurrencyIfMissing?.let { currencyDao.upsert(it.toEntity()) }`
  4. `accountDao.upsert(firstAccount.toEntity())`
  5. `appSettingDao.upsert(ONBOARDING_COMPLETED_AT_KEY, clock.now().toString())`
  Any failure rolls back all five writes ⇒ next launch sees no marker ⇒ onboarding re-shown clean. Setting key constants live in `:domain` so both layers reference the same names.
- **`Iso4217Catalog`** (NEW static bundle): list of `(code, displayNameKey, decimalDigits)` for ISO 4217 currencies the user can pick via "choose another". USD/COP excluded from this list (they live in `CurrencySeed`). Display names resolve through localized keys (`currency.name.<code>`) — never hard-coded.
- **`AppSeeder` change**: keep `CurrencySeed.essentials` insert on first DB open (so USD/COP are always present). **Remove** the unconditional `AccountSeed.defaultCash(...)` insert — onboarding now creates the first account. Keep `CategorySeed.ALL` insert (categories are needed for recording, independent of onboarding). Document this change in the migration.

### Presentation (`:feature-onboarding`)
- **MVI**: `OnboardingViewModel` (`@HiltViewModel`) exposes `StateFlow<OnboardingUiState>`; consumes `OnboardingIntent` (NextFromWelcome, UseDefaults, CurrencySelected(code), OpenIsoPicker, IsoCurrencySelected(code), CloseIsoPicker, NameNotEditable (informational), InitialBalanceChanged(digits), Confirm, Back); emits one-shot `OnboardingUiEvent.FinishedToRecord` via `Channel`/`SharedFlow`. State machine:
  - `Step.Welcome` → `Step.Currency` → `Step.FirstAccount` → (atomic write) → `FinishedToRecord`.
  - `UseDefaults` from Welcome skips Currency + FirstAccount: resolves default base via `ResolveDefaultBaseCurrency`, builds a Cash account with `initialBalance = Money(0, baseDecimalDigits)`, calls `CompleteOnboarding` directly.
- **Screens**:
  - `WelcomeScreen` — hero brand, "Continuar" + "Usar valores predeterminados" CTAs (mapped to `onboarding.cta.continue` / `onboarding.cta.useDefaults`). No language picker (spec FR7).
  - `CurrencyPickerScreen` — vertical list. **USD row at index 0, COP row at index 1**, both with essential badge (`onboarding.currency.essential.badge`). "Elegir otra moneda" row opens an `IsoCurrencyPickerSheet` (modal bottom sheet) showing `Iso4217Catalog`. Selecting an ISO row enables that currency and returns to the picker with it selected.
  - `FirstAccountScreen` — fixed-type "Efectivo / Cash" header (resolved from `account.defaultName.cash`); no name field on the defaults path (spec FR5 clarification). Amount entry uses the **same `AmountKeypad` composable from `:designsystem`** (currently lives in `:feature-record` — extracted in T010 to `:designsystem` if not already there; see tasks). Currency caption shows the selected base. Confirm CTA writes.
- **Strings**: `res/values/strings.xml` (es) + `res/values-en/strings.xml` (en) generated from `shared-assets/i18n/{es,en}.json`. New keys to add to the shared JSON (dotted): `onboarding.welcome.title`, `onboarding.welcome.subtitle`, `onboarding.cta.continue`, `onboarding.cta.useDefaults`, `onboarding.currency.title`, `onboarding.currency.essential.badge`, `onboarding.currency.chooseAnother`, `onboarding.account.title`, `onboarding.account.initialBalance.label`, `onboarding.account.confirm`, `account.defaultName.cash` (es: "Efectivo", en: "Cash"), `currency.name.USD`, `currency.name.COP`, plus `currency.name.<code>` for the supported ISO 4217 codes. Mapping rule: dotted → underscored (`onboarding.cta.useDefaults` → `onboarding_cta_useDefaults`).

### App (`:app`) — routing
- `MainActivity` collects `IsOnboardingComplete()` once at startup (suspend `getBaseCurrency() != null && isOnboardingComplete()` ⇒ true; see spec §12 first-launch assumption). Sets a `BolsilloNavHost` with two destinations: `OnboardingRoute` and `RecordRoute`.
- Start destination = `if (onboardingComplete) RecordRoute else OnboardingRoute`. After `OnboardingUiEvent.FinishedToRecord`, the nav controller pops onboarding and navigates to `RecordRoute` (single-task, no re-entry). On subsequent cold starts the saved marker routes straight to `RecordRoute` ⇒ onboarding is never shown again.
- Locale: `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))` is called once when the marker is first written (atomic with the DB transaction commit), so the entire app renders `es` from that point. No language UI in onboarding.

## Design mapping

Faithful to `docs/design/Bolsillo.dc.html` via `shared-assets/design/{tokens.json,design-system.md}`. **No new visual decisions** — every value below is a token reference; literals (hex/sp/dp) live only in `:designsystem`. `§N` = section in `design-system.md`. Composables read tokens through `BolsilloTheme`.

### Composables in scope of feature 002 (`:feature-onboarding`)

| Composable | Maps to | Tokens (role) | Spec |
|---|---|---|---|
| `WelcomeScreen` | §3 onboarding hero (mock's brand/intro panel) | `gradient.primary` background, `BolsilloTypography.displayBalance` for brand mark, `bodyLarge`/`textMuted` for subtitle, primary CTA `gradient.primary` + `buttonPrimary` shadow, secondary CTA `surface`/`textPrimary` outlined | FR1, US-1.3 |
| `CurrencyPickerScreen` | §3 list screen | `background`; rows use `surface` + `card` radius + `e1`; selected row `primaryContainer` ring; essential badge `Pill` (`surfaceInverse`/`onPrimary`, `badge`); "Elegir otra moneda" trailing chevron `textMuted` | FR3, US-1.2 |
| `CurrencyRow(code, name, isEssential, isSelected)` | row component | leading `iconTileSm` with currency glyph; title `bodyStrong`; subtitle `label`/`textMuted` (full currency name via `currency.name.<code>`); essential badge `badge`/`surfaceInverse`; trailing radio (check) `primaryAccent` when selected | FR3 |
| `IsoCurrencyPickerSheet` | §3 bottom-sheet picker | `sheet` radius (top corners), `surface`, `e3`; search field `surfaceVariant`/`outline`; list rows same as `CurrencyRow` (no essential badge) | FR3 |
| `FirstAccountScreen` | §3 account creation step | `background`; header: `iconTile` (cash glyph) + title `titleM`; amount block reuses `AmountDisplay` + `AmountKeypad` from `:designsystem`; currency caption `label`/`textMuted`; Confirm CTA `gradient.primary` + `buttonPrimary` shadow (`onboarding.account.confirm`) | FR5, US-1.2 |
| `OnboardingStepDots(index, total)` | §3 progress dots | filled dot `primaryAccent`, empty dot `textDisabled`; spacing `sm` | UX |

### Theme reuse
- All colors/typography/shapes/spacing/elevation/gradients consumed via `BolsilloTheme.colors / typography / shapes / spacing / elevation` (already wired in `:designsystem` for feature 001). No new token roles needed.
- **Tabular figures** apply to every amount rendered (`AmountDisplay`, `MoneyText`) — already enforced by `:designsystem`.
- Dark-mode parity: each token has `light`/`dark` variants resolved by `BolsilloTheme` from `isSystemInDarkTheme()`.

### Strings → design copy
Every visible label resolves through `LocalContext.current.getString(R.string.<key>)`. Mock copy (`"Configura tu moneda base"`, `"Elegir otra moneda"`, `"Tu primera cuenta"`, etc.) lives in the shared JSON; the XML is generated. No literal strings in composables.

## Testing strategy (Article IX)

- **`:domain` (JVM unit)**:
  - `GetEnabledCurrencies` always returns USD at 0, COP at 1 regardless of input order, with the rest sorted alphabetically after.
  - `EnableCurrency` is idempotent and never re-essential-flags a non-essential currency.
  - `ResolveDefaultBaseCurrency` returns COP for `regionCode == "CO"` and USD otherwise (incl. null).
  - `CompleteOnboarding` rejects invalid inputs (currency code empty, account currency != base, balance negative).
- **`:data` (JVM unit, Robolectric for SQLCipher-free Room)**:
  - `AppSettingDao` round-trip (get/observe/upsert).
  - **Atomicity test**: `RoomSettingsRepository.completeOnboarding(...)` — force a failure at step 4 (account insert) and assert that base_currency, language, currency-upsert, and the completion marker were **all** rolled back; the next `isOnboardingComplete()` returns `false`.
  - **Migration v1→v2 test**: seed a v1 DB with sample accounts/transactions/categories, run `MIGRATION_1_2`, assert `app_settings` exists, all prior rows intact, balances still reconcile (Article IX).
- **`:feature-onboarding` (`androidTest`)**:
  - Defaults path: launch → tap "Usar valores predeterminados" → land on record surface; assert (a) marker is set, (b) base currency = USD or COP per device region, (c) exactly one account exists with type `cash`, balance `0`, name key `account.defaultName.cash`, currency = base.
  - Currency order: assert USD row at index 0, COP at index 1, both with essential badge, neither removable. Add EUR via "choose another" → EUR appears at index 2 (or after USD/COP).
  - Pick COP path: select COP → confirm → assert `base_currency == "COP"` and first account currency == COP.
  - Pick another ISO path: open ISO sheet → select EUR → confirm → assert EUR is now `enabled = true, essential = false`, base = EUR, first account currency = EUR, and USD + COP still present + essential.
  - No language selector: enumerate every node on every onboarding screen, assert none has the `Tag.languagePicker` semantics tag.
  - Re-launch: after a successful run, kill + relaunch the app → assert `RecordRoute` is the start destination and onboarding is never shown.
  - Crash-mid-flow recovery: stub a failure during `completeOnboarding(...)` → assert no partial DB state and onboarding restarts cleanly.
  - es + en rendering: switch device language → assert all onboarding strings localize (es: "Efectivo", en: "Cash"; etc.); no truncation; no hard-coded text via Layout Inspector / `getStringOrNull` assertion.
- **Performance checks**:
  - Cold-start to welcome screen ≤ 1.5 s on the reference low-end device (no DB read on the main thread; the `isOnboardingComplete` check runs on `Dispatchers.IO`, the UI shows the welcome composition unconditionally and routes after the suspend completes).
  - `completeOnboarding(...)` transactional write ≤ 100 ms (benchmark in `androidTest`).

## Phase summary

- **Phase 0 — research.md**: decisions recorded (D1 settings store, D2 atomic write, D3 first-launch signal, D4 currency-order data shape, D5 default base currency derivation, D6 routing/locale apply). ✔
- **Phase 1 — data-model.md / contracts/ / quickstart.md**: schema v2 delta + invariants; port + use-case contracts; build/validate guide. ✔
- **Phase 2 — tasks-android.md**: produced by `/speckit-tasks` (NOT in this command).

## Complexity Tracking

No constitution violations — table intentionally omitted.
