# Implementation Plan (Android): Fast expense recording

**Feature**: `001-fast-expense-recording` | **Platform**: Android | **Date**: 2026-06-13
**Spec**: [spec.md](./spec.md) · **Constitution**: `.specify/memory/constitution.md`
**Companion artifacts**: [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/](./contracts/) · [quickstart.md](./quickstart.md)

## Summary

Deliver US-2.1 / US-2.2 / US-2.7 natively on Android: open straight into a focused amount keypad, pre-fill category + account from the `ExpenseClassifier` port (stub returns last-used until spec 003), and save an expense in ≤ 3 taps / ≤ 5 s fully offline. Income and same-currency transfers (linked double entry, excluded from totals), one-tap undo, edit with balance recompute, and soft-delete + restore. Money is integer minor units (`Money` value class, `Long`); **account balance is always derived** (initial + signed-sum of non-deleted legs) so reconciliation holds by construction. Stack: Kotlin + Jetpack Compose, Clean Architecture + MVI, modules `:domain` / `:data` / `:feature-record` + new presentation-only `:designsystem` (+ existing `:app` / `:core`), Room over SQLite with SQLCipher, Hilt, Coroutines/Flow. The UI is built faithfully to `docs/design/Bolsillo.dc.html` by mapping `shared-assets/design/tokens.json` into a Compose M3 theme (see **Design mapping**); no literal colors/sizes in composables.

## Technical Context

**Language/Version**: Kotlin 2.0.21 (JDK 17 toolchain; CI on JDK 21)
**Primary Dependencies**: Jetpack Compose (BOM 2024.12.01, Material 3), Hilt, Room 2.6.1, SQLCipher (android-database-sqlcipher) 4.6.1, kotlinx-coroutines 1.9.0 (Flow)
**Storage**: SQLite via Room, encrypted at rest with SQLCipher; passphrase wrapped by Android Keystore
**Design source**: `shared-assets/design/tokens.json` + `shared-assets/design/design-system.md`; canonical pixels in `docs/design/Bolsillo.dc.html`. UI maps tokens into a Compose M3 theme — no literal colors/sizes in composables (see **Design mapping** below)
**Testing**: JUnit + kotlinx-coroutines-test + Turbine (domain/data JVM unit), Room in-memory/JVM SQLite for DAO + migration tests, Compose UI test (`androidTest`) for the 3-tap flow and es/en rendering
**Target Platform**: Android `minSdk` 26 / `compileSdk` 35 / `targetSdk` 35
**Project Type**: Native mobile app (single module-per-layer Gradle project under `android/`)
**Performance Goals**: save ≤ 100 ms; cold start ≤ 1.5 s to usable keypad; suggestion pre-fill ≤ 200 ms (stub is instant); no loading screen during recording
**Constraints**: 100% offline; integer minor units only (no float/double for money); soft delete only; transactional + reconciling writes; es default + en, no hard-coded strings
**Scale/Scope**: Single-user on-device DB; thousands of transactions per account expected at MVP; one recording screen + supporting use cases

**Unknowns resolved in [research.md](./research.md)**: SQLCipher key management; derived-vs-cached balance; float-free amount parsing; signed-amount ledger convention; JVM testing of Room; undo mechanics. No `NEEDS CLARIFICATION` remain (spec clarifications closed 2026-06-13).

## Constitution Check

*GATE: must pass before Phase 0 and re-checked after Phase 1. All nine articles below (governance requires an explicit per-article statement).*

| Article | Status | How this Android design satisfies it |
|---|---|---|
| **I — Local-first / offline** | ✔ | All reads/writes hit the on-device Room DB; no network in any path. `ExpenseClassifier` stub is in-process. Airplane-mode is the default test mode. |
| **II — Privacy** | ✔ | No account, no PII, no network calls. Classifier runs in-process; `learn()` is a no-op stub (no telemetry). DB encrypted at rest (SQLCipher). |
| **III — Financial integrity** | ✔ | `Money(minorUnits: Long)` only — no float anywhere, incl. amount parsing (integer math) and balance SUM. Transfers = linked pair (`transferGroupId`) excluded from expense/income. Soft delete (`deletedAt`) only — `TransactionRepository` has no hard-delete. Writes wrapped in a single Room transaction; balance **derived** so it always reconciles. `fxRateMillis`/`amountBase` frozen at creation (same-currency in 001 ⇒ rate = 1.000). |
| **IV — Speed** | ✔ | App launches directly into the record surface with the amount auto-focused; the **custom `AmountKeypad`** (not the system IME) is up on open per the design; save = one indexed insert + one SUM query (≤ 100 ms); no loading spinner during recording (design has none); suggestion off the UI-blocking path (stub instant, last-used fallback). |
| **V — AI suggests, never blocks** | ✔ | Category/account accessed only via `ExpenseClassifier` port; `RecordViewModel` pre-fills the result but Save is always enabled regardless of classifier state. Threshold/“to confirm” UI deferred to E4; stub returns last-used. |
| **VI — Native per platform, spec is source of truth** | ✔ | This is the Android plan only; behavior derives from the platform-neutral spec. Any divergence from iOS is fixed in `spec.md` first. |
| **VII — Localization & currency** | ✔ | All strings in `res/values/strings.xml` (es) + `res/values-en/strings.xml` (en); zero hard-coded UI text — every label in the design maps to a string key. Money rendered via a shared `MoneyText` using `Currency.decimalDigits` + active locale + **tabular figures** (design rule). USD/COP essential seed already present; totals roll up to base. |
| **VIII — Clean architecture** | ✔ | `presentation (:feature-record, :designsystem) → domain (:domain) ← data (:data)`. New `:designsystem` is **presentation-only** (Compose theme/components) and depends on nothing in `:data`/`:domain`; it imports no DB/AI types. `:domain` stays free of Android/Room/SQLCipher — only ports + use cases. UI never touches the DB; it talks to use cases. |
| **IX — Tests / no data loss** | ✔ | Unit tests for money math, transfer integrity, reconciliation after every op, overdraft-allowed, same-account-rejected; Room migration test with sample data (must not lose data); Compose 3-tap UI test; es+en rendering test. Soft delete guarantees recoverability. |

**Result: PASS** (no violations; Complexity Tracking table omitted — nothing to justify). **Re-verified after adding the Design mapping**: the UI work is presentation-only — it adds one `:designsystem` module and composables that consume shared tokens; it introduces **no** change to `:domain`/`:data`, money handling, persistence, or the AI port, so all nine articles still hold.

## Project Structure

### Documentation (this feature)

```text
specs/001-fast-expense-recording/
├── spec.md                 # Platform-neutral spec (done)
├── plan-android.md         # This file
├── research.md             # Phase 0 — decisions
├── data-model.md           # Phase 1 — entities, invariants, schema v1
├── contracts/              # Phase 1 — domain ports + use-case signatures
│   ├── ports.md
│   └── use-cases.md
├── quickstart.md           # Phase 1 — build/validate guide
├── checklists/requirements.md
└── tasks-android.md        # Phase 2 — produced by /speckit-tasks (NOT here)
```

### Source Code (Android — `android/`, modules already scaffolded)

```text
android/
├── domain/                 # Pure Kotlin (no Android deps)
│   └── src/main/kotlin/com/bolsillo/domain/
│       ├── model/          # Money, Currency, Transaction (exist) + Account, AccountBalance, AmountInput (new)
│       ├── port/           # TransactionRepository (extend), AccountRepository (extend)
│       ├── ai/             # ExpenseClassifier (exists)
│       └── usecase/        # NEW: RecordTransaction, RecordTransfer, EditTransaction,
│                           #      SoftDeleteTransaction, RestoreTransaction, UndoLastRecord,
│                           #      ObserveAccountBalances, SuggestCategoryAndAccount, MoneyParser
├── data/                   # Room + SQLCipher + DI
│   └── src/main/kotlin/com/bolsillo/data/
│       ├── db/             # NEW: BolsilloDatabase, entities, DAOs, Converters, Migrations, SqlCipherKeyProvider
│       ├── repository/     # RoomTransactionRepository, RoomAccountRepository (replace in-memory)
│       ├── ai/             # NEW: LastUsedExpenseClassifier (stub until spec 003)
│       ├── currency/       # CurrencySeed (exists); + AccountSeed (new)
│       └── di/             # DataModule (extend: DB, DAOs, classifier binding)
├── designsystem/           # NEW MODULE (presentation-only): Compose M3 theme from shared tokens
│   └── src/main/kotlin/com/bolsillo/designsystem/
│       ├── theme/          # BolsilloTheme, Color.kt (ColorScheme + BolsilloColors), Type.kt
│       │                   #   (Typography + BolsilloTypography), Shape.kt (Shapes + BolsilloShapes),
│       │                   #   Spacing.kt, Elevation.kt, CategoryColors.kt, Confidence.kt
│       └── component/       # MoneyText, shared primitives (CategoryIconTile, Pill/Badge, SegmentedControl)
├── feature-record/         # Compose UI + MVI (consumes :designsystem)
│   └── src/main/kotlin/com/bolsillo/feature/record/
│       ├── ui/             # RecordRoute, RecordScreen, AmountDisplay, AmountKeypad, TypeSelector,
│       │                   #   CategoryChip, AccountChip, AiConfidenceBadge, TransferAccountsRow,
│       │                   #   SaveButton, UndoSnackbar
│       └── presentation/   # NEW: RecordViewModel, RecordUiState, RecordIntent, RecordUiEvent
├── app/                    # Entry point (MainActivity → RecordRoute wrapped in BolsilloTheme), Hilt app, nav
└── core/                   # DispatcherProvider (exists)
```

**Structure Decision**: Keep the layer-per-module layout and add **one** new presentation module, `:designsystem`, so both `:app` and `:feature-record` (and future feature modules) share the same theme without putting Compose into `:core` (which stays pure utilities). This is the option the design system explicitly allows (`:core` or `:designsystem`); `:designsystem` is chosen for reuse and to keep `:core` Compose-free. Must be added to `settings.gradle.kts` (`include(":designsystem")`). The Compose theme currently in `app/ui/theme/` moves into `:designsystem`. All other new code lands inside existing modules; the in-memory placeholders (`InMemoryTransactionRepository`, `SeedAccountRepository`) are replaced by Room-backed implementations behind the unchanged port interfaces.

## Approach by layer

### Domain (`:domain`)
- **New `Account`** model: `id`, `name` (string key or user text), `type` (CASH/DEBIT/CREDIT/BANK/SAVINGS/WALLET), `currencyCode`, `initialBalance: Money`, `icon`, `color`, `archived`, timestamps. (Only the subset needed for recording; full account management is E3.)
- **Signed-amount ledger convention** (new invariant, see data-model): `Transaction.amount` (and `amountBase`) are **signed** — expense < 0, income > 0, transfer source leg < 0, destination leg > 0. Display uses magnitude. This makes balance a pure sum.
- **Balance is derived, never stored**: `balance(account) = initialBalance + Σ(amount of non-deleted legs for that account)`. No cached balance ⇒ edit/undo/delete/restore reconcile automatically.
- **Extend ports**: `AccountRepository` gains `observeAccounts()`, `getById`, and `observeBalances()` (or `observeBalance(accountId)`); `TransactionRepository` gains `lastUsed()` (most recent non-deleted) and an atomic `upsertTransfer(legA, legB)` / `softDeleteGroup(transferGroupId)` for transfer pairs. (Full signatures in [contracts/ports.md](./contracts/ports.md).)
- **Use cases** (one responsibility each, constructor-injected ports): `RecordTransaction` (expense/income), `RecordTransfer` (builds the linked pair, rejects same-account, asserts same currency), `EditTransaction`, `SoftDeleteTransaction`, `RestoreTransaction`, `UndoLastRecord` (hard-undo of the just-created id within the undo window — removes the in-flight record; uses soft delete so nothing is lost), `ObserveAccountBalances`, `SuggestCategoryAndAccount` (calls classifier; falls back to last-used; never throws to the caller). `MoneyParser` converts keypad input → `Money` using `Currency.decimalDigits` with pure integer math.

### Data (`:data`)
- **Room schema v1**: `accounts`, `transactions`, `categories` tables. `transactions` indexed on `(accountId, deletedAt)` and `transferGroupId` for fast balance SUM and pair lookup. `TypeConverters` for enums; `Money` stored as `Long` columns (`amount_minor`, `amount_base_minor`, `initial_balance_minor`).
- **SQLCipher**: open the Room DB with a SupportFactory using a 256-bit passphrase generated once and stored wrapped by an Android Keystore key (see research.md). DB file never readable off-device.
- **Repositories**: `RoomTransactionRepository`, `RoomAccountRepository` implement the (extended) ports; multi-row writes (transfer pair, edit that touches two accounts) run inside `withTransaction { }` for atomicity (Article III). Balance exposed via a `SUM` DAO query returning `Long`, mapped to `Money`, as a `Flow`.
- **Mappers** entity ↔ domain (keep Room types out of `:domain`).
- **Stub classifier** `LastUsedExpenseClassifier` implements `ExpenseClassifier`: `suggest()` returns the last-used category id (via repo) with `confidence = 0.0`, empty alternatives; `learn()` is a no-op (personalization is E4). Account fallback handled by `SuggestCategoryAndAccount`.
- **Seeds**: `CurrencySeed` (exists, USD+COP essential). Add a minimal `AccountSeed` (a single default Cash account) so first launch has an account to record into; categories seed is minimal/optional in 001 (a default “Uncategorized”).
- **DI**: extend `DataModule` to provide `BolsilloDatabase`, DAOs, and bind `RoomTransactionRepository`/`RoomAccountRepository`/`LastUsedExpenseClassifier`.

### Presentation (`:feature-record`)
- **MVI**: `RecordViewModel` (`@HiltViewModel`) exposes `StateFlow<RecordUiState>`; consumes `RecordIntent` (AmountChanged, TypeSelected[Expense/Income/Transfer], CategorySelected, AccountSelected, DestinationSelected, Save, Undo, DismissUndo); emits one-shot `RecordUiEvent` (Saved, Undone, ValidationError) via a `Channel`/`SharedFlow`.
- **Screen**: `RecordScreen` rebuilt — `AmountKeypad` (custom, integer entry, auto-focus on open, no system loading), type segmented control, category + account chips (pre-filled), Save button always enabled, `UndoSnackbar` shown ~5 s or until next action (Article-IV friendly). Transfer mode reveals source + destination pickers with same-account inline error (`record_transfer_sameAccountError`).
- **3-tap path**: launch → (amount typed) → Save. Category/account already pre-filled ⇒ taps = digits + Save. Undo is a 1-tap affordance, not on the critical path.
- **Strings**: `res/values/strings.xml` (es) + `res/values-en/strings.xml` (en) are **generated from the shared i18n source** `shared-assets/i18n/{es,en}.json` (Article VI/VII — the keys are defined there, not hand-authored per platform). The dotted shared keys map to Android resource names by replacing `.` with `_` (`record.save`→`record_save`, `record.transfer.sameAccountError`→`record_transfer_sameAccountError`, `record.ai.waiting`→`record_ai_waiting`, …). A small generation/sync step keeps the XML in lockstep with the shared JSON; document the dotted↔underscore mapping so parity audits don't flag it.

## Design mapping

Faithful to `docs/design/Bolsillo.dc.html` via `shared-assets/design/{tokens.json,design-system.md}`. **No new visual decisions** — every value below is a token reference. `§N` = section in `design-system.md`. All literals (hex/sp/dp) live only in the `:designsystem` theme generated from `tokens.json`; composables read them through `BolsilloTheme`.

### Theme (`:designsystem`) — tokens → Compose M3
- **`BolsilloTheme(darkTheme = isSystemInDarkTheme(), content)`** wraps `MaterialTheme(colorScheme, typography, shapes)` and provides custom `CompositionLocal`s for roles M3 doesn't model. Selects the `light`/`dark` variant of every token per `darkTheme`.
- **Color** (`tokens.json → color`, §1.1). M3 `ColorScheme` slot mapping: `primary→primary`, `onPrimary→onPrimary`, `primaryContainer→primaryContainer`, `onPrimaryContainer→onPrimaryContainer`, `background→background`, `onBackground→textPrimary`, `surface→surface`, `onSurface→textPrimary`, `surfaceVariant→track`, `onSurfaceVariant→textSecondary`, `outline→outline`, `error→danger`, `onError→onPrimary`, `errorContainer→dangerContainer`. Roles with no M3 slot (`textMuted`, `textDisabled`, `surfaceInverse`, `success(+Container)`, `warning(+Container/Border)`, `info(+Container)`, `amountPositive/Negative`, `divider`, `fill`, `caret`, `notificationDot`, `primaryAccent`) go in an immutable **`BolsilloColors`** exposed via `LocalBolsilloColors`, read as `BolsilloTheme.colors.*`. Brand gradients (`tokens.json → gradient`, hero/primary/fab/avatar) as `Brush` factories.
- **Typography** (`tokens.json → typography`, §1.3). `FontFamily` = Plus Jakarta Sans (bundled or downloadable) with weights 400/500/600/700/800. M3 `Typography` filled where roles fit (`headlineLarge≈displayBalance`, `titleLarge≈titleXL`, `titleMedium≈titleM`, `labelLarge≈button`, `bodyLarge≈body`, `bodyMedium≈label`, `labelSmall≈badge`). App-specific roles (`displayAmount`, `keypadDigit`, `amountRow`, `moneyXL/L`, `navLabel`, `overline`, `bodyStrong`) in **`BolsilloTypography`** via `LocalBolsilloTypography`. Money roles set `TextStyle(fontFeatureSettings = "tnum")` for **tabular figures** (§1.3 rule); `letterSpacing`/`lineHeight` from the role.
- **Shapes** (`tokens.json → radius`, §1.5). M3 `Shapes(small = control 16.dp, medium = card 18.dp, large = cardLarge 20.dp)`; extras (`iconTileSm 11`, `iconTile 13`, `iconTileLg 17`, `chip 16`, `cardXL 22`, `nav 26`, `sheet 30`, `full = CircleShape`) in **`BolsilloShapes`** via `LocalBolsilloShapes`.
- **Spacing** (`tokens.json → spacing`, §1.4): `BolsilloSpacing` `Dp` scale (`xxs 2 … 6xl 30`) via `LocalBolsilloSpacing`; screen padding 18.dp, sheet 20.dp.
- **Elevation/shadow** (`tokens.json → elevation`, §1.6): `BolsilloElevation` token→`Modifier.shadow(...)` presets (`e1…e4`, `nav`, `fab`, `buttonPrimary`, `segmentedThumb`, `toast`); ambient color `rgba(28,20,60,a)` light / `rgba(0,0,0,a)` dark.
- **Category** (`tokens.json → category`, §1.2): `categoryColor(token): {fg, container}` map (light/dark container), keyed by the **palette token id**. A transaction's `categoryId` is a **taxonomy** id, so resolve color as `taxonomy[categoryId].colorToken → categoryColor(token)` (`shared-assets/taxonomy/category-taxonomy.json` carries `colorToken`); never key the palette directly off a taxonomy id. **Confidence** (`tokens.json → confidence`, §2.3): `confidenceVisual(conf, threshold = ExpenseClassifier.DEFAULT_THRESHOLD)` → `{icon, fgRole, containerRole, labelKey, chipBorderRole?}` for `waiting | toConfirm | confident`.
- **`MoneyText`** (`:designsystem/component`): renders an amount from **primitive inputs** (minor-unit `Long` + `decimalDigits` + currency symbol + locale + sign), tabular, sign + color (`amountPositive`/`amountNegative`) per §1.1 — the single money renderer used everywhere. It takes primitives (not the domain `Money` type) so `:designsystem` stays dependency-free (Article VIII, parity with iOS); `:feature-record` adapts `Money` → primitives at the call site.

### Composables in scope of feature 001 (`:feature-record`)
Layout/spacing/state per the **record sheet** in the mock and `design-system.md` §2. Each lists its design source and spec FR.

| Composable | Maps to | Tokens (role) | Spec |
|---|---|---|---|
| `RecordScreen(state, onIntent)` | record sheet body (§3 "Record sheet") — vertical stack: TypeSelector → AmountDisplay → Category/Account chips → AiConfidenceBadge → ModeTabs(keypad active) → AmountKeypad → SaveButton | `background`, sheet padding 20, gaps per §1.4 | FR1,2,5 |
| `TypeSelector` (Gasto/Ingreso/Transferencia) | §3 "Segmented control" | track `outline`, thumb `surface` + `segmentedThumb`, text `textPrimary`/`textMuted`, radius `control` | FR8,9 |
| `AmountDisplay` | §2.1 amount row | `$` `moneyL` + value `displayAmount` tabular; color `textPrimary`/`amountPositive`(income)/`textDisabled`(empty); blinking `caret` (motion `caretBlink`); currency caption `label`/`textMuted` | FR1,14 |
| `AmountKeypad` (3×4: 1–9, `000`, 0, ⌫) | §2.1 keypad + violet hint strip | keys `surface`/`keypadDigit`/radius `control`/elevation `key`; hint strip `primaryContainer`/`onPrimaryContainer` (`record_keypad_hint`) | FR1,5,14 |
| `CategoryChip` / `AccountChip` | §2.3 / §2.4 chip | `surface`, 1.5dp border (`warningBorder` when low else `surface`), tile `iconTileSm` w/ `categoryColor`, labels `caption`/`bodyValue` | FR3,4 |
| `AiConfidenceBadge` | §2.3 confidence states | `confidenceVisual()` → spark/info/check icon + `textMuted`/`warning`/`success` on `fill`/`warningContainer`/`successContainer`, `badge` type | FR3,4 (V) |
| `TransferAccountsRow` (source/dest + same-account error) | §3 record sheet (transfer) | two `AccountChip`s; inline error `danger` text (`record_transfer_sameAccountError`) | FR9,10 |
| `SaveButton` | §2.7 (reuses FAB gradient) / "save" | enabled: `gradient.primary` + `buttonPrimary` shadow, `onPrimary` `button` text, arrow icon; disabled (amount 0): `track`/`textDisabled`. **Always tappable when amount>0 regardless of AI** | FR5,19 (IV,V) |
| `UndoSnackbar` | §3 "Undo toast" | `surfaceInverse` pill (radius `full`, `toast` shadow), green check, message `body`, "Deshacer" action `primaryAccent`; visible **5000 ms** (motion `undoToastTimeout`) | FR6,7 |
| `TransactionRow` *(to reflect saved result/balance; home feed is E3)* | §2.2 | `surface`/`card`/`e2`, category tile `iconTile`, title `bodyStrong`, `MoneyText` `amountRow`, "Por confirmar" badge `warning`/`warningContainer` | FR6 |

**ModeTabs** (Teclado/Texto/Recibo, §3): render all three for visual fidelity, but only **Teclado** is interactive in 001 — Texto/Recibo are deferred (specs 007); they are disabled/inert, not removed, so layout matches the design.

**Launch-shell parity note (Article VI):** the mock reaches recording via Home → FAB → bottom sheet; spec FR1 says the app **opens straight into amount entry**. This is **decided in `spec.md`** (Clarifications, post-analysis 2026-06-13): the record surface is the launch destination, **presented full-screen**, built from the same record composables as the mock's sheet — and **both platforms MUST match this**. Home + FAB + bottom-sheet presentation belong to later nav/E3 work. No open decision remains here.

## Testing strategy (Article IX)
- **`:domain` (JVM unit)**: `Money` arithmetic; `MoneyParser` integer parsing per `decimalDigits`; `RecordTransaction` balance effect; `RecordTransfer` builds signed linked pair, excluded from expense/income totals, rejects same account, rejects cross-currency; `EditTransaction` recompute incl. account change + transfer leg; `SoftDelete`/`Restore` balance effect; `UndoLastRecord`; **reconciliation property test** over random op sequences; overdraft allowed.
- **`:data` (JVM unit)**: DAO insert/query with `deletedAt` filtering; balance `SUM` query correctness; transactional transfer write atomicity; **migration test with sample data** (v1 baseline; framework in place for future migrations — release blocked on data loss).
- **`:designsystem`**: `BolsilloTheme` resolves light/dark token sets; `MoneyText` renders tabular, locale-correct, sign-colored output; `confidenceVisual()` returns the right state at boundaries (0, just-below/at `DEFAULT_THRESHOLD` 0.75); category map covers all 13 ids.
- **`:feature-record` (`androidTest`)**: Compose UI test for the 3-tap save flow (assert ≤ 3 taps, balance updates, undo appears); `AiConfidenceBadge`/`CategoryChip` render the waiting/to-confirm/confident states (incl. low-confidence chip border); Save stays enabled while the classifier is "waiting" (Art. V); transfer same-account error; es and en rendering (no truncation, no hard-coded). ViewModel state tests with Turbine on JVM where possible.
- **Performance checks**: assert save path does no main-thread DB work; manual/benchmark validation of ≤ 100 ms save and ≤ 1.5 s cold start on a reference low-end device before release.

## Phase summary
- **Phase 0 — research.md**: decisions recorded (key mgmt, derived balance, float-free parsing, signed ledger, JVM Room testing, undo). ✔
- **Phase 1 — data-model.md / contracts/ / quickstart.md**: entities + invariants + schema v1; port + use-case contracts; build/validate guide. ✔
- **Phase 2 — tasks-android.md**: produced by `/speckit-tasks` (NOT in this command).

## Complexity Tracking
No constitution violations — table intentionally omitted.
