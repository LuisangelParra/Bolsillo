# Tasks (iOS): Fast expense recording

**Feature**: `001-fast-expense-recording` | **Platform**: iOS | **Input**: design docs in `specs/001-fast-expense-recording/`
**Prerequisites**: [plan-ios.md](./plan-ios.md), [spec.md](./spec.md), [research-ios.md](./research-ios.md), [data-model.md](./data-model.md) + [data-model-ios.md](./data-model-ios.md), [contracts/ports-ios.md](./contracts/ports-ios.md) + [contracts/use-cases-ios.md](./contracts/use-cases-ios.md)
**Tests**: INCLUDED — Article IX mandates tests. Unit tests use **Swift Testing** (`import Testing`); UI tests use **XCUITest**. Fast loop: `swift test --package-path Packages/Bolsillo`.

## Format: `[ID] [P?] [Story] Description with exact path`
- **[P]** = parallelizable (different files, no incomplete-task dependency).
- **[Story]** = US1 (US-2.1), US2 (US-2.2), US3 (US-2.7). Setup/Foundational/Polish carry no story label.
- Paths follow [plan-ios.md](./plan-ios.md): package `ios/Packages/Bolsillo/`, app `ios/App/`, Tuist `ios/Project.swift`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the `BolsilloDesignSystem` package, new test targets, and the GRDB+SQLCipher build.

- [ ] T001 Add `BolsilloDesignSystem` library product + target, new test targets (`BolsilloDataTests`, `BolsilloDesignSystemTests`, `FeatureRecordTests`), point `FeatureRecord` deps at `BolsilloDesignSystem`, and declare `FeatureRecord` + `BolsilloDesignSystem` resources in `ios/Packages/Bolsillo/Package.swift`
- [ ] T002 Integrate **GRDB + SQLCipher** (research-ios R1): wire SQLCipher so GRDB compiles with `SQLITE_HAS_CODEC` (SwiftPM SQLCipher module/xcframework) in `ios/Packages/Bolsillo/Package.swift` — **validate first**; same file as T001 (sequential)
- [ ] T003 [P] Add `BolsilloDesignSystem` to the app target deps and add a `BolsilloUITests` target (`product: .uiTests`, `sources: ["App/UITests/**"]`) in `ios/Project.swift`
- [ ] T004 [P] Add Plus Jakarta Sans font files (400/500/600/700/800) to `ios/Packages/Bolsillo/Sources/BolsilloDesignSystem/Resources/` and declare them `.process`
- [ ] T004a [P] Add an i18n generation step (script/SPM plugin) that derives `ios/Packages/Bolsillo/Sources/FeatureRecord/Resources/Localizable.xcstrings` from `shared-assets/i18n/{es,en}.json` (dotted keys map verbatim — no renaming; Article VI/VII — shared i18n is the source, not hand-authored)
- [ ] T005 Run `tuist generate --no-open` from `ios/` and confirm all targets build — depends on T001–T004a

**Checkpoint**: `swift build --package-path ios/Packages/Bolsillo` and `xcodebuild build` succeed with the new package + SQLCipher.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Theme, domain core, GRDB persistence, composition root, and app shell that EVERY user story depends on.

**⚠️ CRITICAL**: No user story work begins until this phase is complete.

### Design system theme (`ios/Packages/Bolsillo/Sources/BolsilloDesignSystem/`)
- [ ] T006 [P] `Theme/BolsilloColors.swift` — every semantic role as `Color`, `.light`/`.dark` instances from `shared-assets/design/tokens.json → color`
- [ ] T007 [P] `Theme/BolsilloTypography.swift` — `Font` per role (Plus Jakarta Sans) + font registration; money roles apply `.monospacedDigit()` (tabular)
- [ ] T008 [P] `Theme/BolsilloSpacing.swift` + `Theme/BolsilloRadius.swift` — `CGFloat` scales from `tokens.json → spacing`/`radius` (`full`→`Capsule`)
- [ ] T009 [P] `Theme/BolsilloElevation.swift` (shadow presets, ambient dark-violet) + `Theme/BolsilloGradients.swift` (`LinearGradient` factories from `tokens.json → gradient`)
- [ ] T010 [P] `Theme/CategoryColors.swift` (`categoryColor(_ token:)` keyed by palette token id, 13 ids) + `Theme/Confidence.swift` (`confidence(_:threshold:)` keyed off `ExpenseClassifier.defaultThreshold`)
- [ ] T011 `Theme/BolsilloTheme.swift` + `Theme/Environment+Theme.swift` (`EnvironmentValues.bolsilloTheme`) + `Theme/BolsilloThemeProvider.swift` (picks light/dark by `@Environment(\.colorScheme)`) — depends on T006–T010
- [ ] T012 [P] `Components/MoneyText.swift` — primitive inputs (minorUnits/decimalDigits/symbol/locale/sign), tabular, `amountPositive`/`amountNegative`; **no domain import** — depends on T011
- [ ] T013 [P] `Components/` shared primitives — `CategoryIconTile.swift`, `Pill.swift` (badge), `SegmentedControl.swift` — depends on T011
- [ ] T013a [P] `Sources/FeatureRecord/CategoryColorResolver.swift` — resolve `categoryId` (taxonomy id) → `colorToken` (from `shared-assets/taxonomy/category-taxonomy.json`) → `categoryColor(token)`; the UI uses this, never keys the palette off a taxonomy id (Article VI)

### Domain core (`ios/Packages/Bolsillo/Sources/BolsilloDomain/`)
- [ ] T014 [P] `Account.swift` + `AccountType.swift` (cash/debit/credit/bank/savings/wallet) per [data-model.md](./data-model.md)
- [ ] T015 [P] `Category.swift` + `Values.swift` (`TransactionDraft`, `Suggestion`, `TransferPair`, `SavedRef`, `TransferError`, `AmountInput`) per [contracts/use-cases-ios.md](./contracts/use-cases-ios.md)
- [ ] T016 [P] Signed-amount helpers on `Transaction.swift` (magnitude accessor; sign by type/leg) — Invariant 1
- [ ] T017 Extend `Ports.swift` `TransactionRepository` — add `lastUsed()`, `upsertTransfer(legSource:legDest:)`, `softDeleteGroup(transferGroupId:deletedAt:)`, `restoreGroup(transferGroupId:)` per [contracts/ports-ios.md](./contracts/ports-ios.md)
- [ ] T018 [P] New `AccountRepository.swift` port — `observeAccounts()`/`getById`/`observeBalance(accountId:)`/`observeBalances()` returning `AsyncStream` (derived-balance contract)
- [ ] T019 [P] `UseCases/MoneyParser.swift` — keypad digits → `Money` with pure `Int` math honoring `Currency.decimalDigits` (FR 14)
- [ ] T019a [P] `Sources/BolsilloDomain/MoneyRounding.swift` — banker's half-up rounding policy, defined once (Article III); identity for same-currency 001, but the policy + test harness exist from day one (Article IX), used by FX/base conversion in E8

### Data core (`ios/Packages/Bolsillo/Sources/BolsilloData/`)
- [ ] T020 `Database/Records.swift` — `AccountRecord`, `TransactionRecord`, `CategoryRecord` (`Codable`, `FetchableRecord`, `PersistableRecord`); money as `INTEGER`
- [ ] T021 `Database/DatabaseMigratorV1.swift` — schema v1 tables + indices `(accountId, deletedAt)`/`(transferGroupId)`/`(occurredAt)` via `DatabaseMigrator` — depends on T020
- [ ] T022 [P] `Database/KeychainKeyProvider.swift` — `SecRandomCopyBytes` 256-bit key, Keychain `AfterFirstUnlockThisDeviceOnly`, device-only (research-ios R1)
- [ ] T023 `Database/BolsilloDatabase.swift` — `DatabaseQueue` with `Configuration.prepareDatabase { try $0.usePassphrase(key) }`, WAL — depends on T020–T022, T002
- [ ] T024 [P] `Database/Mappers.swift` — record↔domain (keep GRDB out of `BolsilloDomain`)
- [ ] T025 `Repositories/GRDBTransactionRepository.swift` — implements extended port; single-row `upsert`/`softDelete`/`restore` + `lastUsed` (transfer methods in US2); writes in one `write {}` — depends on T021, T024, T017
- [ ] T026 `Repositories/GRDBAccountRepository.swift` — derived balance via `ValueObservation` → `AsyncStream<Money>` (no stored column) — depends on T021, T024, T018
- [ ] T027 [P] `AI/LastUsedExpenseClassifier.swift` — stub: `suggest()`→last-used categoryId, `confidence=0.0`; `learn()` no-op (research-ios R8)
- [ ] T028 [P] `Seeds/AccountSeed.swift` + `Seeds/CategorySeed.swift` — one default Cash account + seed categories **from `shared-assets/taxonomy/category-taxonomy.json`** (id + `nameKey` (i18n key) + `colorToken`), defaulting new transactions to `other` (Article VI shared taxonomy)

### Composition root + app shell (`ios/App/Sources/`)
- [ ] T029 `App/Sources/CompositionRoot.swift` — build `BolsilloDatabase` (GRDB+SQLCipher), GRDB repositories, stub classifier, and the use cases; run seeds — depends on T023, T025, T026, T027, T028
- [ ] T030 `App/Sources/BolsilloApp.swift` — wrap root in `BolsilloThemeProvider`, present the record surface as launch destination; replace placeholder `ContentView.swift` — depends on T011, T029

**Checkpoint**: app launches into a themed surface; encrypted DB opens; use cases injectable.

---

## Phase 3: User Story 1 — Record an expense quickly (Priority: P1) 🎯 MVP

**Goal**: Open straight into a focused keypad; AI/last-used pre-fills category+account; save an expense in ≤ 3 taps / ≤ 5 s fully offline; immediate balance update + undo.

**Independent Test**: In airplane mode, launch → keypad on screen (no system keyboard, no spinner) → type amount (category+account pre-filled) → Save → balance drops, undo toast appears within ~5 s; undo reverts (spec AC: Happy path, Undo, Offline & AI fallback).

### Domain use cases (`ios/Packages/Bolsillo/Sources/BolsilloDomain/UseCases/`)
- [ ] T031 [P] [US1] `SuggestCategoryAndAccount.swift` — classifier + last-used fallback; never throws (FR 3,4)
- [ ] T032 [P] [US1] `RecordTransaction.swift` — expense/income signed single transactional write; **freezes `fxRateMillis=1000` and `amountBase=amount` at creation** (Invariant 8, same-currency 001); overdraft allowed (FR 5,8,14,16,17,19)
- [ ] T033 [P] [US1] `UndoLastRecord.swift` — soft-delete the just-created id/group within the undo window (FR 6,7)
- [ ] T034 [P] [US1] `ObserveAccountBalances.swift` — derived balances `AsyncStream` for the UI (FR 6,15)

### Tests (`ios/Packages/Bolsillo/Tests/...`)
- [ ] T035 [P] [US1] `Tests/BolsilloDomainTests/MoneyParserTests.swift` — integer parse per `decimalDigits`, no float path (FR 14)
- [ ] T036 [P] [US1] `Tests/BolsilloDomainTests/RecordTransactionTests.swift` — balance effect + overdraft-allowed (FR 19) + **frozen FX assertion** (`fxRateMillis==1000`, `amountBase==amount`, not recomputed — Invariant 8, Article IX FX coverage)
- [ ] T036a [P] [US1] `Tests/BolsilloDomainTests/MoneyRoundingTests.swift` — banker's half-up boundaries (.5 → even); identity on same-currency (Article III/IX)
- [ ] T037 [P] [US1] `Tests/BolsilloDomainTests/SuggestUndoTests.swift` — last-used fallback + `UndoLastRecord` revert
- [ ] T038 [P] [US1] `Tests/BolsilloDataTests/TransactionQueryTests.swift` — `deletedAt` filtering + balance `SUM` correctness (GRDB in-memory)
- [ ] T039 [P] [US1] `Tests/BolsilloDataTests/MigrationTests.swift` — run `DatabaseMigrator` v1 over seeded sample rows, assert no data loss (Article IX)

### Localization (`ios/Packages/Bolsillo/Sources/FeatureRecord/Resources/`)
- [ ] T040 [P] [US1] `Localizable.xcstrings` — **generated by the T004a sync** from `shared-assets/i18n/{es,en}.json` (dotted `record.*` keys verbatim), resolved via `Bundle.module`; do not hand-author values

### Presentation (`ios/Packages/Bolsillo/Sources/FeatureRecord/Presentation/`)
- [ ] T041 [US1] `Presentation/RecordState.swift` — state + transient `event` enum (saved/undone/validationError)
- [ ] T042 [US1] `Presentation/RecordModel.swift` (`@MainActor @Observable`) — pre-fill, save, undo, observe balances; **Save always enabled regardless of classifier state** — depends on T031–T034, T041

### UI views (`ios/Packages/Bolsillo/Sources/FeatureRecord/Views/`)
- [ ] T043 [P] [US1] `Views/AmountKeypad.swift` — custom 3×4 integer keypad (1–9, 000, 0, ⌫), live on appear, no system keyboard (§2.1)
- [ ] T044 [P] [US1] `Views/AmountDisplay.swift` — `$` + `displayAmount` tabular + blinking caret + currency caption (§2.1)
- [ ] T045 [P] [US1] `Views/TypeSelector.swift` — Gasto/Ingreso/Transferencia segmented (§3)
- [ ] T046 [P] [US1] `Views/CategoryChip.swift` + `Views/AccountChip.swift` — pre-filled chips with category tile (§2.3/2.4)
- [ ] T047 [P] [US1] `Views/AiConfidenceBadge.swift` — `confidence()` waiting/to-confirm/confident (§2.3)
- [ ] T048 [P] [US1] `Views/SaveButton.swift` — `gradient.primary` + glow when amount>0; disabled at 0 (§2.7)
- [ ] T049 [P] [US1] `Views/UndoToast.swift` — `surfaceInverse` pill, 5000 ms or until next action (§3 / motion)
- [ ] T050 [P] [US1] `Views/ModeTabs.swift` — Teclado active; Texto/Recibo inert (visual parity, §3)
- [ ] T051 [US1] `Views/RecordScreen.swift` — assemble TypeSelector→AmountDisplay→chips→badge→ModeTabs→keypad→SaveButton + UndoToast overlay — depends on T043–T050, T042
- [ ] T052 [US1] Rebuild `Sources/FeatureRecord/RecordView.swift` to host `RecordScreen(model:)`; app launches into it (FR1) — depends on T051, T030

### Tests — model + UI
- [ ] T053 [P] [US1] `Tests/FeatureRecordTests/RecordModelTests.swift` (Swift Testing, `@MainActor`) — pre-fill, Save enabled while classifier "waiting", undo
- [ ] T054 [P] [US1] `ios/App/UITests/RecordFlowUITests.swift` (XCUITest) — 3-tap save flow (≤ 3 taps per spec FR5 — amount entry = 1 interaction, Save = 1 tap; digit presses excluded), balance updates, undo appears, **no loading screen during recording** (FR2)
- [ ] T055 [P] [US1] `ios/App/UITests/RecordLocalizationUITests.swift` — es and en rendering, no truncation/hard-coded text

**Checkpoint**: MVP — expense recording works end-to-end offline, independently testable.

---

## Phase 4: User Story 2 — Record income and transfers (Priority: P2)

**Goal**: Record income (adds to balance, marked income) and same-currency transfers as a linked double entry excluded from expense/income totals.

**Independent Test**: Income save raises balance with `+`; transfer between two different same-currency accounts creates a negative+positive linked pair (both balances move, excluded from totals); same account on both sides shows `record.transfer.sameAccountError` and blocks save (spec AC: Income, Transfer).

### Domain & data
- [ ] T056 [P] [US2] `Sources/BolsilloDomain/UseCases/RecordTransfer.swift` — build signed linked pair; `throw TransferError.sameAccount`; `throw TransferError.crossCurrency`; **freeze `fxRateMillis=1000`/`amountBase=amount` on both legs at creation** (Invariant 8, FR 17) (FR 9,10; Invariants 4,5,6,8)
- [ ] T057 [US2] Implement transfer methods in `Sources/BolsilloData/Repositories/GRDBTransactionRepository.swift` — `upsertTransfer`/`softDeleteGroup`/`restoreGroup` inside one `write {}` (atomic) — depends on T025

### Tests
- [ ] T058 [P] [US2] `Tests/BolsilloDomainTests/RecordTransferTests.swift` — signed pair sums to 0, excluded from expense/income totals, same-account thrown, cross-currency thrown
- [ ] T059 [P] [US2] `Tests/BolsilloDomainTests/IncomeTests.swift` — income raises balance and is marked `income`
- [ ] T060 [P] [US2] `Tests/BolsilloDataTests/TransferAtomicityTests.swift` — pair write is all-or-nothing

### Presentation
- [ ] T061 [US2] Extend `Sources/FeatureRecord/Presentation/RecordModel.swift` — income mode + transfer mode (destination selection, same-account validation) — depends on T056, T042
- [ ] T062 [P] [US2] `Sources/FeatureRecord/Views/TransferAccountsRow.swift` — source/dest pickers + inline same-account error (§3)
- [ ] T063 [US2] Wire income/transfer modes into `Sources/FeatureRecord/Views/RecordScreen.swift` (reveal `TransferAccountsRow`, income tinting) — depends on T061, T062

### UI test
- [ ] T064 [P] [US2] `ios/App/UITests/TransferIncomeUITests.swift` — transfer same-account error blocks save; income save path

**Checkpoint**: US1 + US2 both work independently.

---

## Phase 5: User Story 3 — Correct or delete a transaction (Priority: P3)

**Goal**: Edit a transaction with correct balance recompute (incl. account/type change and transfer legs); soft-delete to trash with restore; no hard deletes.

**Independent Test**: Edit amount/account → balances recompute and reconcile; delete → moves to trash, excluded from balance; restore → reappears; deleting one transfer leg removes both, restoring brings both back (spec AC: Edit, Delete & restore, Integrity).

### Domain
- [ ] T065 [P] [US3] `Sources/BolsilloDomain/UseCases/EditTransaction.swift` — recompute via re-derived balances; keep transfer pair consistent; **preserve frozen `fxRateMillis`/`amountBase` (never recompute on edit)** (Invariant 8) (FR 11)
- [ ] T066 [P] [US3] `Sources/BolsilloDomain/UseCases/SoftDeleteTransaction.swift` + `RestoreTransaction.swift` — single or whole transfer group (FR 12,13)

### Tests
- [ ] T067 [P] [US3] `Tests/BolsilloDomainTests/EditTransactionTests.swift` — amount change, account change (old restored/new adjusted), transfer-leg edit keeps pair consistent
- [ ] T068 [P] [US3] `Tests/BolsilloDomainTests/DeleteRestoreTests.swift` — soft-delete/restore balance effect; transfer group together
- [ ] T069 [US3] `Tests/BolsilloDomainTests/ReconciliationTests.swift` — property test over random record/edit/undo/delete/restore sequences (balances always reconcile, FR 15)

### Presentation
- [ ] T070 [US3] Extend `Sources/FeatureRecord/Presentation/RecordModel.swift` — edit/delete/restore intents — depends on T065, T066, T042
- [ ] T071 [P] [US3] `Sources/FeatureRecord/Views/TransactionRow.swift` — reflect saved result/balance + "Por confirmar" badge (§2.2)
- [ ] T072 [US3] Edit/delete/restore UI affordances + trash restore entry — depends on T070, T071
- [ ] T073 [P] [US3] `ios/App/UITests/EditDeleteRestoreUITests.swift` — edit recompute + delete→trash→restore round trip

**Checkpoint**: all three stories independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T074 [P] `Tests/BolsilloDesignSystemTests/` — `ThemeTests.swift` (light/dark resolution), `MoneyTextTests.swift` (tabular/locale/sign), `ConfidenceTests.swift` (boundaries 0, just-below/at 0.75), `categoryColor` covers all 13 palette ids, and `CategoryColorResolver` maps every taxonomy id via `colorToken`
- [ ] T075 [P] `Tests/BolsilloDataTests/SQLCipherSmokeTests.swift` — on simulator: DB opens only with the Keychain key; unreadable without it (Articles I/II)
- [ ] T076 Accessibility pass on `Sources/FeatureRecord/Views/RecordScreen.swift` — VoiceOver labels, Dynamic Type, hit targets, contrast (§10)
- [ ] T077 Performance validation (Instruments) — save ≤ 100 ms, cold start ≤ 1.5 s, no main-thread DB work on save path (Article IV); record sign-off
- [ ] T078 [P] `swiftlint --config ios/.swiftlint.yml` clean across the package
- [ ] T079 Run [quickstart-ios.md](./quickstart-ios.md) manual validation in airplane mode (golden path + edge cases + es↔en)

---

## Dependencies & Execution Order

### Phase dependencies
- **Setup (P1)** → no deps (T001→T002 same file sequential; T005 after T001–T004).
- **Foundational (P2)** → after Setup; **BLOCKS all stories**.
- **US1 (P3)** → after Foundational. **US2 (P4)** / **US3 (P5)** → after Foundational; both extend US1's `RecordModel`/`RecordScreen` (T042/T051), so sequence P1→P2→P3 unless staffed in parallel on separate files.
- **Polish (P6)** → after the desired stories.

### Critical intra-phase deps
- Theme: T006–T010 → T011 → {T012, T013}.
- Data: T020 → T021; T020–T022 + T002 → T023; {T021, T024, T017} → T025; {T021, T024, T018} → T026; T023/T025/T026/T027/T028 → T029 → T030.
- US1: T031–T034 + T041 → T042 → T051 (with T043–T050) → T052.
- US2: T056 → T057; T056 → T061 → T063 (with T062).
- US3: T065/T066 → T070 → T072 (with T071).

### Parallel opportunities
- Setup: T003, T004 [P] (T001→T002 sequential; T005 last).
- Foundational: theme T006–T010 [P]; domain T014–T016, T018, T019 [P]; data T022, T024, T027, T028 [P].
- US1: use cases T031–T034 [P]; tests T035–T039 [P]; strings T040 [P]; UI views T043–T050 [P]; model/UI tests T053–T055 [P].
- Cross-story: with capacity, US2/US3 domain+tests (T056/T058/T059/T060, T065–T069) proceed in parallel on separate files once Foundational is done.

---

## Parallel Example: User Story 1

```bash
# Use cases (separate files):
Task: "SuggestCategoryAndAccount.swift"  # T031
Task: "RecordTransaction.swift"          # T032
Task: "UndoLastRecord.swift"             # T033
Task: "ObserveAccountBalances.swift"     # T034

# UI views (separate files):
Task: "AmountKeypad.swift"  Task: "AmountDisplay.swift"  Task: "TypeSelector.swift"
Task: "CategoryChip.swift+AccountChip.swift"  Task: "AiConfidenceBadge.swift"
Task: "SaveButton.swift"  Task: "UndoToast.swift"  Task: "ModeTabs.swift"   # T043–T050
```

---

## Implementation Strategy

### MVP first (US1 only)
1. Phase 1 Setup → 2. Phase 2 Foundational (CRITICAL — includes the GRDB+SQLCipher wiring, validated early) → 3. Phase 3 US1 → **STOP & validate** the 3-tap offline flow → demo.

### Incremental delivery
Foundation ready → US1 (MVP, expense) → US2 (income + transfers) → US3 (edit/delete/restore) → Polish. Each story is an independently testable increment.

### Parallel team strategy
After Foundational: Dev A → US1; Dev B → US2 domain/data + `TransferAccountsRow`; Dev C → US3 domain/data + `TransactionRow`. UI integration tasks (T063, T072) merge onto US1's `RecordScreen` last.

---

## Notes
- `[P]` = different files, no incomplete-task dependency.
- Every money path uses `Money` (`Int` minor units) — no `Double`/`Float`/`Decimal`-from-double anywhere (Article III).
- Balance is **derived** via `ValueObservation` over the SUM — never a stored column (Invariant 2).
- Soft delete only; transfers move/restore as a group; multi-row writes in one GRDB `write {}` (Article III, WAL).
- `BolsilloDesignSystem` imports nothing from domain/data; `MoneyText` takes primitives (Article VIII).
- Localization via module `Localizable.xcstrings` with dotted keys = same ids as Android (Article VI parity).
- Release blocked if a migration loses data, balances fail to reconcile, es/en UI coverage is incomplete, or Article IV budgets fail (Article IX).
- Validate the GRDB+SQLCipher SPM wiring (T002) before building features on it.
- `RecordModel.swift` is created in US1 (T042) then extended in US2 (T061) and US3 (T070) — these touch the **same file**, so those story tasks are sequential, not parallel; split per-mode handling into separate files if parallelizing across developers.
- String keys are dotted in `shared-assets/i18n` and the iOS `.xcstrings` (`record.save`) — verbatim match, no renaming (parity with Android, which maps the same keys to underscores).
- Frozen FX (T032/T036) and banker's rounding (T019a/T036a) are trivial in same-currency 001 but their harness exists from day one (Articles III/IX); they carry the load when E8 adds cross-currency.
- Run `/speckit-analyze` before `/speckit-implement` to cross-check spec ↔ both plans ↔ both task lists against the constitution.
