# Tasks (Android): Fast expense recording

**Feature**: `001-fast-expense-recording` | **Platform**: Android | **Input**: design docs in `specs/001-fast-expense-recording/`
**Prerequisites**: [plan-android.md](./plan-android.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)
**Tests**: INCLUDED — Article IX (constitution) mandates tests for every feature (money math, transfer integrity, reconciliation, migration-with-sample-data, 3-tap flow, es+en rendering).

## Format: `[ID] [P?] [Story] Description with exact path`
- **[P]** = parallelizable (different files, no incomplete-task dependency).
- **[Story]** = US1 (US-2.1), US2 (US-2.2), US3 (US-2.7). Setup/Foundational/Polish carry no story label.
- Module paths follow [plan-android.md](./plan-android.md): `android/{domain,data,designsystem,feature-record,app,core}/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the new `:designsystem` module and wire dependencies/tooling.

- [ ] T001 [P] Add Room 2.6.1, SQLCipher 4.6.1, coroutines-test, Turbine, and Compose-test versions to `android/gradle/libs.versions.toml`
- [ ] T002 Create `:designsystem` module build file (Compose + Material 3, JVM + androidTest) at `android/designsystem/build.gradle.kts`
- [ ] T003 Register the new module in `android/settings.gradle.kts` (`include(":designsystem")`) — depends on T002
- [ ] T004 [P] Add `:designsystem` as a dependency of `:feature-record` and `:app` in `android/feature-record/build.gradle.kts` and `android/app/build.gradle.kts`
- [ ] T005 [P] Add Plus Jakarta Sans font files (weights 400/500/600/700/800) to `android/designsystem/src/main/res/font/`
- [ ] T005a [P] Add an i18n generation step (Gradle task/script) that derives `android/feature-record/src/main/res/values/strings.xml` (es) + `values-en/strings.xml` (en) from `shared-assets/i18n/{es,en}.json`, mapping dotted keys → underscore resource names (Article VI/VII — shared i18n is the source, not hand-authored)

**Checkpoint**: modules resolve; `./gradlew :designsystem:assembleDebug` builds an empty module.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Theme, domain core, persistence, DI, and app shell that EVERY user story depends on.

**⚠️ CRITICAL**: No user story work begins until this phase is complete.

### Design system theme (`android/designsystem/src/main/kotlin/com/bolsillo/designsystem/`)
- [ ] T006 [P] `theme/Color.kt` — M3 `ColorScheme` slot mapping + `BolsilloColors` (light/dark) from `shared-assets/design/tokens.json → color`/`gradient`
- [ ] T007 [P] `theme/Type.kt` — `Typography` + `BolsilloTypography` (Plus Jakarta Sans; money roles set `fontFeatureSettings = "tnum"` for tabular figures)
- [ ] T008 [P] `theme/Shape.kt` — M3 `Shapes` + `BolsilloShapes` from `tokens.json → radius`
- [ ] T009 [P] `theme/Spacing.kt` + `theme/Elevation.kt` — `BolsilloSpacing` + `BolsilloElevation` from `tokens.json → spacing`/`elevation`
- [ ] T010 [P] `theme/CategoryColors.kt` + `theme/Confidence.kt` — `categoryColor(token)` keyed by palette token id (13 ids) + `confidenceVisual(conf, threshold)` keyed off `ExpenseClassifier.DEFAULT_THRESHOLD`
- [ ] T011 `theme/BolsilloTheme.kt` — wraps `MaterialTheme` + provides `CompositionLocal`s (colors/typography/shapes/spacing/elevation), light/dark by `isSystemInDarkTheme()` — depends on T006–T010
- [ ] T012 [P] `component/MoneyText.kt` — single money renderer taking **primitive inputs** (minor-unit `Long` + `decimalDigits` + symbol + locale + sign; no domain `Money` import, keeps `:designsystem` dependency-free), tabular, sign + `amountPositive`/`amountNegative` — depends on T011
- [ ] T013 [P] `component/` shared primitives — `CategoryIconTile.kt`, `Pill.kt` (badge), `SegmentedControl.kt` — depends on T011
- [ ] T013a [P] `android/feature-record/.../ui/CategoryColorResolver.kt` (or `:data`) — resolve `categoryId` (taxonomy id) → `colorToken` (from `shared-assets/taxonomy/category-taxonomy.json`) → `designsystem.categoryColor(token)`; the UI uses this, never keys the palette off a taxonomy id (Article VI)

### Domain core (`android/domain/src/main/kotlin/com/bolsillo/domain/`)
- [ ] T014 [P] `model/Account.kt` + `model/AccountType.kt` (CASH/DEBIT/CREDIT/BANK/SAVINGS/WALLET) per [data-model.md](./data-model.md)
- [ ] T015 [P] `model/Category.kt` (nameKey) + `model/AmountInput.kt`
- [ ] T016 [P] Apply the signed-amount convention to `model/Transaction.kt` (expense<0, income>0, transfer source<0/dest>0; magnitude helper) — Invariant 1
- [ ] T017 Extend `port/TransactionRepository.kt` — add `lastUsed()`, `upsertTransfer(legSource, legDest)`, `softDeleteGroup(transferGroupId, deletedAt)`, `restoreGroup(transferGroupId)` per [contracts/ports.md](./contracts/ports.md)
- [ ] T018 [P] Extend `port/AccountRepository.kt` (**exists** with `observeCurrencies()` per [contracts/ports.md](./contracts/ports.md)) — add `observeAccounts()`, `getById`, `observeBalance(accountId)`, `observeBalances()` (derived balance contract)
- [ ] T019 [P] `usecase/MoneyParser.kt` — keypad digits → `Money` with pure integer math honoring `Currency.decimalDigits` (FR 14)
- [ ] T019a [P] `domain/.../model/MoneyRounding.kt` — banker's half-up rounding policy, defined once (Article III); used by FX/base conversion. In 001 same-currency it is a no-op identity, but the policy + its test harness exist from day one (Article IX)

### Data core (`android/data/src/main/kotlin/com/bolsillo/data/`)
- [ ] T020 `db/entity/` — `AccountEntity.kt`, `TransactionEntity.kt`, `CategoryEntity.kt` (`*_minor` as `Long`/INTEGER)
- [ ] T021 `db/Converters.kt` — enum↔String TypeConverters — depends on T020
- [ ] T022 `db/dao/` — `AccountDao.kt`, `TransactionDao.kt` (balance `SUM` query + `deletedAt` filter + `transferGroupId` lookup), `CategoryDao.kt`; indices `(accountId, deletedAt)`, `(transferGroupId)`, `(occurredAt)` — depends on T020
- [ ] T023 [P] `db/Migrations.kt` — v1 baseline + versioned migration harness (Article IX)
- [ ] T024 [P] `db/SqlCipherKeyProvider.kt` — random 256-bit passphrase wrapped by Android Keystore (research R1)
- [ ] T025 `db/BolsilloDatabase.kt` — Room DB opened via SQLCipher `SupportFactory` — depends on T020–T024
- [ ] T026 [P] `db/Mappers.kt` — entity↔domain mappers (keep Room types out of `:domain`)
- [ ] T027 `repository/RoomTransactionRepository.kt` — implements extended port; single-row `upsert`/`softDelete`/`restore` + `lastUsed` (transfer methods land in US2) — depends on T022, T026, T017
- [ ] T028 `repository/RoomAccountRepository.kt` — derived balance as `Flow<Money>` from the SUM query (no stored column) — depends on T022, T026, T018
- [ ] T029 [P] `ai/LastUsedExpenseClassifier.kt` — stub: `suggest()`→last-used categoryId, `confidence=0.0`, no alternatives; `learn()` no-op (research R7)
- [ ] T030 [P] `seed/AccountSeed.kt` + `seed/CategorySeed.kt` — one default Cash account + seed categories **from `shared-assets/taxonomy/category-taxonomy.json`** (id + `nameKey` (i18n key) + `colorToken`), defaulting new transactions to `other` (Article VI shared taxonomy)
- [ ] T031 `di/DataModule.kt` (Hilt) — provide `BolsilloDatabase`, DAOs; bind `RoomTransactionRepository`/`RoomAccountRepository`/`LastUsedExpenseClassifier`; run seeds — depends on T025, T027, T028, T029, T030

### App shell (`android/app/src/main/kotlin/com/bolsillo/app/`)
- [ ] T032 `BolsilloApp.kt` (`@HiltAndroidApp`) + `MainActivity.kt` content wrapped in `BolsilloTheme` — depends on T011, T031

**Checkpoint**: app launches into a themed empty surface; DB opens encrypted; ports injectable.

---

## Phase 3: User Story 1 — Record an expense quickly (Priority: P1) 🎯 MVP

**Goal**: Open straight into a focused keypad; AI/last-used pre-fills category+account; save an expense in ≤ 3 taps / ≤ 5 s fully offline; immediate balance update + undo.

**Independent Test**: In airplane mode, launch → keypad focused, no spinner → type amount (category+account pre-filled) → Save → balance drops, undo snackbar appears within ~5 s; tapping undo reverts (spec AC: Happy path, Undo, Offline & AI fallback).

### Domain use cases (`android/domain/src/main/kotlin/com/bolsillo/domain/usecase/`)
- [ ] T033 [P] [US1] `SuggestCategoryAndAccount.kt` — classifier + last-used fallback; never throws (FR 3,4)
- [ ] T034 [P] [US1] `RecordTransaction.kt` — expense/income signed single transactional write; **freezes `fxRateMillis=1000` and `amountBase=amount` at creation** (Invariant 8, same-currency 001); overdraft allowed (FR 5,8,14,16,17,19)
- [ ] T035 [P] [US1] `UndoLastRecord.kt` — soft-delete the just-created id/group within the undo window (FR 6,7)
- [ ] T036 [P] [US1] `ObserveAccountBalances.kt` — derived balances `Flow` for the UI (FR 6,15)

### Tests (`android/domain/src/test/...`, `android/data/src/test/...`)
- [ ] T037 [P] [US1] `domain/.../MoneyParserTest.kt` — integer parsing per `decimalDigits`, no float path (FR 14)
- [ ] T038 [P] [US1] `domain/.../RecordTransactionTest.kt` — balance effect + overdraft-allowed (FR 19) + **frozen FX assertion** (`fxRateMillis==1000`, `amountBase==amount`, not recomputed — Invariant 8, Article IX FX coverage)
- [ ] T038a [P] [US1] `domain/.../MoneyRoundingTest.kt` — banker's half-up boundaries (.5 rounds to even); identity on same-currency (Article III/IX)
- [ ] T039 [P] [US1] `domain/.../SuggestAndUndoTest.kt` — last-used fallback + `UndoLastRecord` revert
- [ ] T040 [P] [US1] `data/.../TransactionDaoTest.kt` — insert/query with `deletedAt` filter + balance `SUM` correctness (JVM/Robolectric)
- [ ] T041 [P] [US1] `data/.../MigrationTest.kt` — open v1, seed sample rows, assert no data loss (Article IX)

### Localization (`android/feature-record/src/main/res/`)
- [ ] T042 [P] [US1] `values/strings.xml` (es, default) — **generated by the T005a sync** from `shared-assets/i18n/es.json` (`record_*` keys); do not hand-author values
- [ ] T043 [P] [US1] `values-en/strings.xml` (en) — **generated by the T005a sync** from `shared-assets/i18n/en.json`; verify both render without truncation

### Presentation (`android/feature-record/src/main/kotlin/com/bolsillo/feature/record/`)
- [ ] T044 [US1] `presentation/RecordUiState.kt` + `RecordIntent.kt` + `RecordUiEvent.kt`
- [ ] T045 [US1] `presentation/RecordViewModel.kt` (`@HiltViewModel`) — pre-fill, save, undo, observe balances; **Save always enabled regardless of classifier state** — depends on T033–T036, T044
- [ ] T046 [P] [US1] `ui/AmountKeypad.kt` — custom 3×4 integer keypad (1–9, 000, 0, ⌫), auto-focus, no system IME (§2.1)
- [ ] T047 [P] [US1] `ui/AmountDisplay.kt` — `$` + `displayAmount` tabular + blinking caret + currency caption (§2.1)
- [ ] T048 [P] [US1] `ui/TypeSelector.kt` — Gasto/Ingreso/Transferencia segmented (§3)
- [ ] T049 [P] [US1] `ui/CategoryChip.kt` + `ui/AccountChip.kt` — pre-filled chips with category tile (§2.3/2.4)
- [ ] T050 [P] [US1] `ui/AiConfidenceBadge.kt` — `confidenceVisual()` waiting/to-confirm/confident (§2.3)
- [ ] T051 [P] [US1] `ui/SaveButton.kt` — gradient + glow when amount>0; disabled at 0 (§2.7)
- [ ] T052 [P] [US1] `ui/UndoSnackbar.kt` — `surfaceInverse` pill, 5000 ms or until next action (§3 / motion)
- [ ] T053 [P] [US1] `ui/ModeTabs.kt` — Teclado active; Texto/Recibo inert (visual parity, §3)
- [ ] T054 [US1] `ui/RecordScreen.kt` — assemble TypeSelector→AmountDisplay→chips→badge→ModeTabs→keypad→SaveButton + UndoSnackbar host — depends on T045–T053
- [ ] T055 [US1] `ui/RecordRoute.kt` + app nav — app **opens straight into** the record surface (FR1) — depends on T054, T032

### UI tests (`android/feature-record/src/androidTest/...`)
- [ ] T056 [P] [US1] `RecordFlowTest.kt` — 3-tap save flow (≤ 3 taps per the spec FR5 definition — amount entry = 1 interaction, Save = 1 tap; digit presses excluded), balance updates, undo appears, **no loading screen shown during recording** (FR2); Save enabled while classifier "waiting"
- [ ] T057 [P] [US1] `RecordLocalizationTest.kt` — es and en rendering, no truncation/hard-coded text

**Checkpoint**: MVP — expense recording works end-to-end offline, independently testable.

---

## Phase 4: User Story 2 — Record income and transfers (Priority: P2)

**Goal**: Record income (adds to balance, marked income) and same-currency transfers as a linked double entry excluded from expense/income totals.

**Independent Test**: Income save raises balance with `+`; transfer between two different same-currency accounts creates a negative+positive linked pair (both balances move, excluded from totals); same account on both sides shows `record_transfer_sameAccountError` and blocks save (spec AC: Income, Transfer).

### Domain & data
- [ ] T058 [P] [US2] `domain/.../usecase/RecordTransfer.kt` — build signed linked pair; reject same-account; assert same-currency; **freeze `fxRateMillis=1000`/`amountBase=amount` on both legs at creation** (Invariant 8, FR 17) (FR 9,10; Invariants 4,5,6,8)
- [ ] T059 [US2] Implement transfer methods in `data/.../repository/RoomTransactionRepository.kt` — `upsertTransfer`/`softDeleteGroup`/`restoreGroup` inside one `withTransaction {}` (atomic) — depends on T027

### Tests
- [ ] T060 [P] [US2] `domain/.../RecordTransferTest.kt` — signed pair sums to 0, excluded from expense/income totals, same-account rejected, cross-currency rejected
- [ ] T061 [P] [US2] `domain/.../IncomeTest.kt` — income raises balance and is marked `income`
- [ ] T062 [P] [US2] `data/.../TransferAtomicityTest.kt` — pair write is all-or-nothing

### Presentation
- [ ] T063 [US2] Extend `presentation/RecordViewModel.kt` — income mode + transfer mode (destination selection, same-account validation) — depends on T058, T045
- [ ] T064 [P] [US2] `ui/TransferAccountsRow.kt` — source/dest pickers + inline same-account error (§3)
- [ ] T065 [US2] Wire income/transfer modes into `ui/RecordScreen.kt` (reveal `TransferAccountsRow`, income tinting) — depends on T063, T064

### UI test
- [ ] T066 [P] [US2] `androidTest/TransferIncomeTest.kt` — transfer same-account error blocks save; income save path

**Checkpoint**: US1 + US2 both work independently.

---

## Phase 5: User Story 3 — Correct or delete a transaction (Priority: P3)

**Goal**: Edit a transaction with correct balance recompute (incl. account/type change and transfer legs); soft-delete to trash with restore; no hard deletes.

**Independent Test**: Edit amount/account → affected balances recompute and reconcile; delete → moves to trash, excluded from balance; restore → reappears; deleting one transfer leg removes both, restoring brings both back (spec AC: Edit, Delete & restore, Integrity).

### Domain
- [ ] T067 [P] [US3] `domain/.../usecase/EditTransaction.kt` — recompute via re-derived balances; keep transfer pair consistent; **preserve frozen `fxRateMillis`/`amountBase` (never recompute on edit)** (Invariant 8) (FR 11)
- [ ] T068 [P] [US3] `domain/.../usecase/SoftDeleteTransaction.kt` + `RestoreTransaction.kt` — single or whole transfer group (FR 12,13)

### Tests
- [ ] T069 [P] [US3] `domain/.../EditTransactionTest.kt` — amount change, account change (old restored/new adjusted), transfer-leg edit keeps pair consistent
- [ ] T070 [P] [US3] `domain/.../DeleteRestoreTest.kt` — soft-delete/restore balance effect; transfer group together
- [ ] T071 [US3] `domain/.../ReconciliationTest.kt` — property test over random record/edit/undo/delete/restore sequences (balances always reconcile, FR 15)

### Presentation
- [ ] T072 [US3] Extend `presentation/RecordViewModel.kt` — edit/delete/restore intents — depends on T067, T068, T045
- [ ] T073 [P] [US3] `ui/TransactionRow.kt` — reflect saved result/balance + "Por confirmar" badge (§2.2)
- [ ] T074 [US3] Edit/delete/restore UI affordances + trash restore entry — depends on T072, T073

### UI test
- [ ] T075 [P] [US3] `androidTest/EditDeleteRestoreTest.kt` — edit recompute + delete→trash→restore round trip

**Checkpoint**: all three stories independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T076 [P] `android/designsystem/src/test|androidTest/...` — `BolsilloTheme` light/dark resolution, `MoneyText` tabular/locale/sign, `confidenceVisual()` boundaries (0, just-below/at 0.75), `categoryColor` covers all 13 palette ids, and taxonomy `colorToken` resolution covers every taxonomy id
- [ ] T076a [P] `android/data/src/androidTest/.../SqlCipherSmokeTest.kt` — on device/emulator: encrypted DB opens with the Keystore-wrapped key; the DB file is unreadable without it (Articles I/II)
- [ ] T077 Accessibility pass on `ui/RecordScreen.kt` — screen-reader labels, large text, touch targets, contrast (§10)
- [ ] T078 Performance validation on reference low-end device — save ≤ 100 ms, cold start ≤ 1.5 s, no main-thread DB work (Article IV); record sign-off
- [ ] T079 [P] `./gradlew ktlintCheck` clean across modules
- [ ] T080 Run [quickstart.md](./quickstart.md) manual validation in airplane mode (golden path + edge cases + es↔en)

---

## Dependencies & Execution Order

### Phase dependencies
- **Setup (P1)** → no deps.
- **Foundational (P2)** → after Setup; **BLOCKS all stories**.
- **US1 (P3)** → after Foundational. **US2 (P4)** and **US3 (P5)** → after Foundational; both build on US1's `RecordViewModel`/`RecordScreen` (T045/T054) so in practice sequence P1→P2→P3 unless staffed in parallel on separate files.
- **Polish (P6)** → after the desired stories.

### Critical intra-phase deps
- Theme: T006–T010 → T011 → {T012, T013}.
- Data: T020 → {T021, T022}; T020–T024 → T025; {T022, T026, T017} → T027; {T022, T026, T018} → T028; T025/T027/T028/T029/T030 → T031.
- App shell T032 needs T011 + T031.
- US1: T033–T036 + T044 → T045 → T054 (with T046–T053) → T055.
- US2: T058 → T059; T058 → T063 → T065 (with T064).
- US3: T067/T068 → T072 → T074 (with T073).

### Parallel opportunities
- Setup: T001, T004, T005 in parallel (T002→T003 sequential).
- Foundational: theme T006–T010 all [P]; domain T014–T016, T018, T019 all [P]; data T023, T024, T026, T029, T030 all [P].
- US1: use cases T033–T036 [P]; tests T037–T041 [P]; strings T042/T043 [P]; UI components T046–T053 [P].
- Cross-story: with capacity, US2 and US3 domain+tests (T058/T060/T061, T067–T071) proceed in parallel on separate files once Foundational is done.

---

## Parallel Example: User Story 1

```bash
# Use cases (separate files):
Task: "SuggestCategoryAndAccount.kt"   # T033
Task: "RecordTransaction.kt"           # T034
Task: "UndoLastRecord.kt"              # T035
Task: "ObserveAccountBalances.kt"      # T036

# UI components (separate files):
Task: "AmountKeypad.kt"  Task: "AmountDisplay.kt"  Task: "TypeSelector.kt"
Task: "CategoryChip.kt+AccountChip.kt"  Task: "AiConfidenceBadge.kt"
Task: "SaveButton.kt"  Task: "UndoSnackbar.kt"  Task: "ModeTabs.kt"   # T046–T053
```

---

## Implementation Strategy

### MVP first (US1 only)
1. Phase 1 Setup → 2. Phase 2 Foundational (CRITICAL) → 3. Phase 3 US1 → **STOP & validate** the 3-tap offline flow → demo.

### Incremental delivery
Foundation ready → US1 (MVP, expense) → US2 (income + transfers) → US3 (edit/delete/restore) → Polish. Each story is an independently testable increment.

### Parallel team strategy
After Foundational: Dev A → US1; Dev B → US2 domain/data + `TransferAccountsRow`; Dev C → US3 domain/data + `TransactionRow`. UI integration tasks (T065, T074) merge onto US1's `RecordScreen` last.

---

## Notes
- `[P]` = different files, no incomplete-task dependency.
- Every money path uses `Money` (`Long` minor units) — no `float`/`double` anywhere (Article III).
- Balance is **derived** (SUM of signed non-deleted legs) — never a stored column (Invariant 2).
- Soft delete only; transfers move/restore as a group; writes are transactional (Articles III).
- Release blocked if a migration loses data, balances fail to reconcile, es/en UI coverage is incomplete, or Article IV budgets fail (Article IX).
- `RecordViewModel.kt` is created in US1 (T045) then extended in US2 (T063) and US3 (T072) — these touch the **same file**, so those story tasks are sequential, not parallel; split per-mode handling into separate files if parallelizing across developers.
- String keys are dotted in `shared-assets/i18n` and the spec (`record.save`); Android resource names use underscores (`record_save`) via the T005a mapping — a platform constraint, not a divergence.
- Frozen FX (T034/T038) and banker's rounding (T019a/T038a) are trivial in same-currency 001 but their harness exists from day one (Articles III/IX); they carry the load when E8 adds cross-currency.
- Run `/speckit-analyze` before `/speckit-implement` to cross-check spec ↔ plan ↔ tasks against the constitution.
