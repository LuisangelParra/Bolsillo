# Phase 1 — Data Model (iOS): Fast expense recording

The **entities, invariants, and schema v1 are platform-neutral** — see [data-model.md](./data-model.md) for the authoritative table of `Account` / `Transaction` / `Category` fields, the 9 invariants, indices, and state transitions. This file records only the **iOS/GRDB-specific** persistence mechanics; it does not restate or override the shared model. Money is integer minor units (`Money.minorUnits: Int`); amounts are **signed** (shared Invariant 1).

## Domain types (`BolsilloDomain`)
- **Exists**: `Money` (Int minor units), `Currency`, `Transaction` (already signed-capable, soft-delete via `markDeleted`/`restored`), `TransactionType`, ports + `ExpenseClassifier`.
- **New**: `Account` (`id`, `name`, `type: AccountType`, `currencyCode`, `initialBalance: Money`, `icon`, `color`, `archived`, `createdAt`, `updatedAt`), `AccountType` enum (cash/debit/credit/bank/savings/wallet), `Category` (`id`, `nameKey`, `icon`, `color`), and small value types used by use cases (`TransactionDraft`, `Suggestion`, `TransferPair`, `SavedRef`, `AmountInput`). All are plain `Sendable` value types — no GRDB/SwiftUI imports (Article VIII).

## GRDB persistence (`BolsilloData`) — schema v1
- **Records**: GRDB `Codec` row types (e.g. `AccountRecord`, `TransactionRecord`, `CategoryRecord`) conforming to `Codable`, `FetchableRecord`, `PersistableRecord` — kept **separate** from the domain `Transaction`/`Account` (mappers convert both ways so GRDB types never leak into `BolsilloDomain`).
- **Tables**: `accounts`, `transactions`, `categories`, created in a GRDB **`DatabaseMigrator`** (v1 baseline; the migrator is the versioned/idempotent harness Article IX requires from the start).
- **Money columns** are SQLite `INTEGER` (`Int`): `amount_minor`, `amount_base_minor`, `initial_balance_minor`. `fx_rate_millis` `INTEGER`. Enums (`type`, account `type`) stored as `TEXT`. **Dates: `INTEGER` epoch milliseconds** (decided — parity with Android's `Long` epoch ms; domain `Date` ↔ epoch-ms conversion lives in the mapper). Mappers are unit-tested for the round-trip.
- **Indices**: `transactions(accountId, deletedAt)` for the balance SUM; `transactions(transferGroupId)` for pair ops; `transactions(occurredAt)` for history ordering. (Matches shared data-model indices.)
- **Derived balance query**: `SELECT COALESCE(SUM(amount_minor), 0) FROM transactions WHERE accountId = ? AND deletedAt IS NULL`, added to `initialBalance`, exposed via `ValueObservation` (research-ios R2). No `balance` column on `accounts`.
- **Atomic writes**: multi-row operations (transfer pair write; an edit that moves a transaction between two accounts; soft-delete/restore of a transfer group) run inside a single GRDB `write { db in … }` transaction — all-or-nothing (Invariant 9, Article III). GRDB uses WAL journaling, satisfying the constitution's crash-recovery requirement.
- **Encryption**: every table is encrypted at rest because the whole DB file is opened through SQLCipher (research-ios R1).

## Mapping to the shared model
| Shared (data-model.md) | iOS realization |
|---|---|
| Invariant 1 — signed amounts | `Money` unary minus; sign applied by use case per type/leg |
| Invariant 2 — derived balance | `ValueObservation` over the SUM query; no stored column |
| Invariant 3 — integer money only | `Money.minorUnits: Int`; no floating accessor exists |
| Invariant 4 — transfer double entry | `upsertTransfer(legSource:legDest:)` writes both rows in one `write {}` |
| Invariant 5 — same-account forbidden | enforced in `RecordTransfer` use case |
| Invariant 6 — same-currency (001) | enforced in `RecordTransfer`; cross-currency deferred E8 |
| Invariant 7 — soft delete only | `deletedAt` column; `softDeleteGroup`/`restoreGroup` for pairs; no hard-delete API |
| Invariant 8 — frozen FX | `fx_rate_millis = 1000`, `amount_base_minor = amount_minor` for same-currency 001, set once |
| Invariant 9 — atomic writes | single GRDB transaction per multi-row op; WAL |

## Migration policy (Article IX)
- v1 is the baseline registered in `DatabaseMigrator`. A migration test opens a v1 DB, seeds sample rows, runs the migrator, and asserts **no data loss** and that balances still reconcile. Release is blocked on any data-losing or irreversible migration. The harness is in place from v1 even though there is nothing to migrate yet, so future schema changes inherit it.
