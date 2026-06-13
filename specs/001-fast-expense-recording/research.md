# Phase 0 — Research (Android): Fast expense recording

Decisions that resolve the plan's unknowns. Format: Decision · Rationale · Alternatives rejected.

## R1 — SQLCipher key management
- **Decision**: Generate a random 256-bit DB passphrase once on first launch; wrap (encrypt) it with an AES key held in the Android Keystore (`AndroidKeyStore`), persist only the wrapped blob in app-private storage. Open Room via SQLCipher `SupportFactory` with the unwrapped passphrase held in memory only.
- **Rationale**: Keystore keys are non-exportable and hardware-backed where available; the DB file is useless if copied off-device (Articles I/II). No user-managed secret needed for MVP recording.
- **Alternatives rejected**: hard-coded/derived-from-deviceID passphrase (trivially extractable); requiring a user PIN to open the DB (blocks fast recording — biometric/PIN lock is US-10.1, a separate feature); storing passphrase in plain SharedPreferences (not encrypted at rest).

## R2 — Balance: derived vs cached
- **Decision**: Balance is **always derived** — `initialBalance + SUM(signed amount of non-deleted legs)` via an indexed Room aggregate query exposed as `Flow`. No `balance` column is stored on `accounts`.
- **Rationale**: Reconciliation (FR 15) holds **by construction** — edit/undo/delete/restore change the rows and the SUM follows; impossible to drift. At MVP scale a SUM over one account's indexed rows is well under the 100 ms budget.
- **Alternatives rejected**: cached running balance column (must be recomputed transactionally on every edit/delete/restore; any missed path silently breaks reconciliation — exactly the bug Article III forbids). Revisit caching only if profiling on the reference device shows the SUM exceeds budget at realistic row counts.

## R3 — Float-free amount entry
- **Decision**: Custom integer keypad + `MoneyParser` that accumulates digits into `Long` minor units using `Currency.decimalDigits` (e.g., 2 → divide-by-100 semantics done as integer placement). Display formatting uses `NumberFormat`/ICU for the active locale on the integer value; parsing never goes through `Double`.
- **Rationale**: Article III forbids float for money including intermediate steps. A digit-accumulator keypad also removes locale-decimal-separator ambiguity and is faster (fewer taps) than a free text field.
- **Alternatives rejected**: `TextField` + `toDouble()`/`BigDecimal.toDouble()` (float path); `BigDecimal` everywhere (heavier than needed; `Long` minor units is the constitution's primary representation and already implemented as `Money`).

## R4 — Signed-amount ledger convention
- **Decision**: Store `Transaction.amount` and `amountBase` as **signed** minor units: expense < 0, income > 0, transfer source leg < 0, destination leg > 0. UI displays magnitude.
- **Rationale**: Makes balance a trivial `SUM` (R2) and makes transfer double-entry self-balancing (legA + legB = 0 across the pair). Aligns with spec FR 9 wording ("one negative at source, one positive at destination").
- **Alternatives rejected**: magnitude-only storage + sign inferred from `type`/leg at query time (every aggregate must re-derive sign with CASE logic — error-prone, and SUM can't be a plain column sum). The current scaffold doesn't yet enforce a sign, so adopting signed now costs nothing.

## R5 — Testing Room/SQLCipher off the emulator
- **Decision**: DAO, query, and migration tests run as JVM unit tests using Room with an in-memory/JVM SQLite driver (Robolectric where an Android context is required); SQLCipher encryption is exercised in an `androidTest` smoke test on device/emulator. Migration tests open a v1 DB, seed sample rows, and assert no data loss.
- **Rationale**: Keeps the bulk of data-layer tests fast and CI-friendly while still proving encryption opens correctly on a real device. Article IX requires migrations tested with sample data and blocks release on data loss.
- **Alternatives rejected**: all data tests as instrumented `androidTest` (slow, flaky in CI); skipping migration tests at v1 (Article IX requires the harness in place from the start).

## R6 — Undo mechanics
- **Decision**: After a save, keep the new transaction id (and, for transfers, the `transferGroupId`) in `RecordUiState`. **Undo = soft-delete** that record/group and revert the derived balance (which follows automatically). The undo affordance is a Compose `Snackbar` shown ~5 s or dismissed on the next user action (FR 6). No hard delete (Article III) — undone items are recoverable like any trashed item.
- **Rationale**: One code path for undo and delete (soft delete), so reconciliation and "no data loss" hold uniformly; matches the clarified undo window.
- **Alternatives rejected**: buffer the write and only commit after the snackbar times out (violates "save ≤ 100 ms" perception and "balance updates immediately"); hard-delete on undo (violates Article III).

## R7 — Classifier stub behavior (until spec 003)
- **Decision**: `LastUsedExpenseClassifier.suggest()` returns the last-used category id with `confidence = 0.0` and empty alternatives; `learn()` is a no-op. Account fallback (last-used, else onboarding account) is handled in `SuggestCategoryAndAccount`.
- **Rationale**: 001 only *consumes* a suggestion (spec §3 out-of-scope: AI internals + correction logging are E4). Confidence/"to-confirm" UI is deferred; Save is never gated on the classifier (Article V).
- **Alternatives rejected**: returning confidence 1.0 (would imply a real user-rule-grade suggestion); implementing the full cascade now (out of scope, belongs to E4/spec 003).
