# EXAMPLE — `specs/001-fast-expense-recording/`

> Worked example showing the standard of definition expected: a platform-neutral **spec**, two per-platform **plans**, and two per-platform **task lists**. Use it as the reference bar for every other feature. (In the real repo, split into `spec.md`, `plan-android.md`, `plan-ios.md`, `tasks-android.md`, `tasks-ios.md`.)

---

# spec.md (platform-neutral)

## 1. Feature
`001` · **Fast expense recording** · **User stories:** US-2.1, US-2.2, US-2.7 · **Epic:** E2

## 2. Problem / why
Manual entry friction is the #1 cause of churn in expense apps. Recording must be near-instant or users abandon the habit.

## 3. In scope / out of scope
- **In scope:** record an expense, income, and transfer; opening straight into amount entry; pre-suggested category/account; save with undo; edit; soft-delete with restore; immediate balance update; full offline operation.
- **Out of scope:** natural-language entry (spec 007), receipt OCR (007), AI model training (003) — here we only *consume* a category suggestion via the `ExpenseClassifier` port.

## 4. User stories covered
- US-2.1 — As a user, I want to record an expense quickly, so that I don't drop the habit.
- US-2.2 — As a user, I want to record income and transfers, so that I reflect all movements.
- US-2.7 — As a user, I want to edit/delete a transaction, so that I keep data accurate.

## 5. Behavioral requirements
1. On launch, the app MUST present amount entry with focus on the amount.
2. The system MUST request a category + account suggestion from the `ExpenseClassifier` port and pre-fill them; if unavailable, fall back to last-used.
3. Saving an expense MUST be reachable in ≤ 3 taps and ≤ 5 s and MUST work offline.
4. After save, the account balance MUST update immediately and an **undo** affordance MUST be available.
5. A transfer MUST create two linked entries (`transfer_group_id`) and MUST NOT count as expense/income in reports.
6. Editing a transaction MUST recompute affected balances correctly.
7. Deleting MUST be a soft delete (to trash) and MUST be restorable.
8. Money MUST be handled in integer minor units; balances MUST always reconcile.

## 6. Acceptance criteria (Gherkin)
- *Given* the app is opened *When* it loads *Then* the amount keypad is shown with the cursor on the amount.
- *Given* an amount and pre-filled category/account *When* I tap Save *Then* the transaction is stored offline, the balance updates, and an undo is offered.
- *Given* a saved expense *When* I tap undo *Then* the transaction is removed and the balance reverts.
- *Given* two accounts *When* I record a transfer *Then* two linked entries exist and reports exclude it from expense/income totals.
- *Given* a transaction *When* I edit its amount *Then* the source account balance recomputes correctly.
- *Given* a transaction *When* I delete it *Then* it moves to trash and can be restored with balance restored.

## 7. Data touched
`Transaction` (create/update/soft-delete), `Account` (balance recompute), `Category` (read), `ExpenseClassifier` port (read suggestion). Invariants: integer minor units; transfer = linked pair; soft delete only.

## 8. Constitution check
III (money integrity ✔ integer minor units, reconciling balances), IV (speed ✔ ≤3 taps/≤5s, save ≤100 ms), V (AI ✔ suggestion only, never blocks save), I/II (offline + on-device ✔), VII (localized strings, base-currency amounts ✔).

## 9. Localization & currency notes
Keys: `record.amount`, `record.save`, `record.undo`, `record.income`, `record.transfer`, `record.delete`, `record.restore` (es default + en). Amounts entered in the account's currency; totals roll up to base currency.

## 10. Non-functional targets
Save ≤ 100 ms; cold start ≤ 1.5 s; 100% offline; accessible keypad (screen reader + large text).

## 11. Open questions (for /speckit.clarify)
- Default account when multiple exist? (proposed: last-used)
- Undo window duration? (proposed: until next action or 5 s snackbar)

## 12. Definition of Done
AC pass on both platforms · offline · no data loss · unit tests for balance math + transfer integrity · UI test for 3-tap flow · localized es+en · performance targets met.

---

# plan-android.md (HOW · Android)

- **Modules:** `:feature-record` (UI), `:domain` (use cases, entities, ports), `:data` (Room + repositories).
- **UI:** Compose screen `RecordScreen` + `RecordViewModel` (MVI: state + intents). Amount keypad composable; category/account chips.
- **Domain:** use cases `RecordTransaction`, `RecordTransfer`, `EditTransaction`, `SoftDeleteTransaction`, `UndoLastRecord`; port `ExpenseClassifier`; entities use `Long` minor units.
- **Data:** Room entities + DAOs over SQLite/SQLCipher; `TransactionRepository`, `AccountRepository`; balance recompute in a single DB transaction; `transfer_group_id` for pairs; `deleted_at` for trash.
- **Concurrency:** Coroutines + Flow; balance updates exposed as `StateFlow`.
- **DI:** Hilt modules bind repositories + a stub `ExpenseClassifier` (returns last-used until spec 003).
- **Tests:** JUnit for use cases + money math; Turbine for flows; Compose UI test for the 3-tap path.
- **Constitution:** money as `Long` minor units; all writes transactional; strings in `res/values{,-en}/strings.xml`.

# tasks-android.md
Phase Setup → Foundation → User Stories → Polish. `[P]` = parallelizable.
1. [Setup] Add Room + SQLCipher + Hilt deps; configure version catalog. → `gradle/libs.versions.toml`, `:data/build.gradle.kts`
2. [Foundation][P] Define entities `Transaction`, `Account`, `Category` (Long minor units). → `:domain/.../model/*.kt`
3. [Foundation] Define ports `TransactionRepository`, `AccountRepository`, `ExpenseClassifier`. → `:domain/.../port/*.kt`
4. [Foundation] Room schema + DAOs + SQLCipher key via Keystore; migration v1. → `:data/.../db/*.kt`
5. [US-2.1] Use case `RecordTransaction` + balance recompute (txn). → `:domain/.../usecase/RecordTransaction.kt`
6. [US-2.1] `RecordViewModel` + `RecordScreen` (keypad, pre-fill, save, undo). → `:feature-record/.../*.kt`
7. [US-2.2][P] `RecordTransfer` (linked pair, excluded from totals). → `:domain/.../usecase/RecordTransfer.kt`
8. [US-2.7][P] `EditTransaction`, `SoftDeleteTransaction`, restore. → `:domain/.../usecase/*.kt`
9. [Polish] i18n keys es/en; accessibility on keypad. → `res/values{,-en}/strings.xml`
10. [Tests] Money math + transfer integrity (JUnit); 3-tap UI test (Compose). → `:domain/test`, `:feature-record/androidTest`

---

# plan-ios.md (HOW · iOS)

- **Packages (SPM):** `BolsilloDomain` (use cases, entities, ports), `BolsilloData` (GRDB + repos), `FeatureRecord` (SwiftUI).
- **UI:** `RecordView` + `RecordModel` (`@Observable`); amount keypad view; category/account pickers.
- **Domain:** use cases `RecordTransaction`, `RecordTransfer`, `EditTransaction`, `SoftDeleteTransaction`, `UndoLastRecord`; protocol `ExpenseClassifier`; money as `Int` minor units (or `Decimal`).
- **Data:** GRDB records over SQLite/SQLCipher (key in Keychain); `TransactionRepository`, `AccountRepository`; balance recompute inside a DB transaction; `transferGroupId`; `deletedAt`.
- **Concurrency:** Swift Concurrency (async/await); balances via `@Observable`/AsyncStream.
- **DI:** lightweight container; stub `ExpenseClassifier` (last-used until spec 003).
- **Tests:** Swift Testing/XCTest for use cases + money math; UI test for 3-tap path.
- **Constitution:** money as `Int` minor units; transactional writes; strings via `String(localized:)` (es base + en).

# tasks-ios.md
Phase Setup → Foundation → User Stories → Polish. `[P]` = parallelizable.
1. [Setup] Add GRDB + SQLCipher via SPM; create packages. → `Package.swift`
2. [Foundation][P] Entities `Transaction`, `Account`, `Category` (Int minor units). → `BolsilloDomain/Sources/Model/*.swift`
3. [Foundation] Protocols `TransactionRepository`, `AccountRepository`, `ExpenseClassifier`. → `BolsilloDomain/Sources/Port/*.swift`
4. [Foundation] GRDB schema + migrations + SQLCipher key (Keychain). → `BolsilloData/Sources/DB/*.swift`
5. [US-2.1] Use case `RecordTransaction` + balance recompute (txn). → `BolsilloDomain/Sources/UseCase/RecordTransaction.swift`
6. [US-2.1] `RecordModel` + `RecordView` (keypad, pre-fill, save, undo). → `FeatureRecord/Sources/*.swift`
7. [US-2.2][P] `RecordTransfer` (linked pair, excluded from totals). → `BolsilloDomain/Sources/UseCase/RecordTransfer.swift`
8. [US-2.7][P] `EditTransaction`, `SoftDeleteTransaction`, restore. → `BolsilloDomain/Sources/UseCase/*.swift`
9. [Polish] Localized strings es/en; accessibility on keypad. → `*.xcstrings`
10. [Tests] Money math + transfer integrity; 3-tap UI test. → `BolsilloDomainTests`, `FeatureRecordUITests`

---

> **How to run this example:** `git checkout -b 001-fast-expense-recording` → `/speckit.specify` (paste section spec.md intent) → `/speckit.clarify` → `/speckit.plan` per platform → `/speckit.tasks` per platform → `/speckit.analyze` → `/speckit.implement` per platform.
