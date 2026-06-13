# Phase 1 — Data Model (Shared / platform-neutral): Fast expense recording

**Authoritative shared model**: the entities (`Account`/`Transaction`/`Category`), the 9 invariants, indices, and state transitions below are **platform-neutral** and binding on both platforms. The "Room schema v1" section is the **Android** persistence realization; the iOS/GRDB realization lives in [data-model-ios.md](./data-model-ios.md) and does not restate or override this model.

Schema **v1**. Money is integer minor units (`Money` = `Long` on Android / `Int` on iOS). Amounts are **signed** (see Invariant 1). All tables encrypted at rest via SQLCipher.

## Entities

### Account
| Field | Type | Notes |
|---|---|---|
| `id` | String (UUID) | PK |
| `name` | String | display text (user-entered); not a localized key |
| `type` | enum | CASH, DEBIT, CREDIT, BANK, SAVINGS, WALLET |
| `currencyCode` | String | ISO 4217; FK→ currency catalog (USD/COP essential) |
| `initialBalance` | Money (`Long` minor) | opening balance; current balance is **derived**, never stored |
| `icon` | String | resource/icon id |
| `color` | Int/Long | ARGB |
| `archived` | Boolean | archived accounts hidden from pickers, history kept |
| `createdAt` / `updatedAt` | Long (epoch ms) | |

> Full account management is E3; 001 needs create/read + a seeded default account.

### Transaction (exists in `:domain` — extended by convention)
| Field | Type | Notes |
|---|---|---|
| `id` | String (UUID) | PK |
| `accountId` | String | FK→ Account |
| `type` | enum | EXPENSE, INCOME, TRANSFER |
| `amount` | Money (`Long`) | **signed** minor units (Invariant 1) |
| `currencyCode` | String | account's currency in 001 |
| `amountBase` | Money (`Long`) | **signed**, frozen at creation; = `amount` when currency = base |
| `fxRateMillis` | Long | frozen FX ×1000; = 1000 for same-currency 001 |
| `categoryId` | String? | nullable; pre-filled by classifier/last-used |
| `merchant` | String? | optional |
| `note` | String? | optional |
| `occurredAt` | Long (epoch ms) | user-facing date |
| `transferGroupId` | String? | non-null on both legs of a transfer; links the pair |
| `createdAt` / `updatedAt` | Long | |
| `deletedAt` | Long? | soft delete / trash; null = active |

### Category (minimal in 001)
| Field | Type | Notes |
|---|---|---|
| `id` | String | PK |
| `nameKey` | String | localized string key (es/en), NOT raw text |
| `icon` / `color` | String / Int | |

> Category creation/editing UI is E4 (US-4.4); 001 seeds a default "Uncategorized" and reads existing categories.

## Relationships
- `Transaction.accountId` → `Account.id` (many-to-one).
- `Transaction.categoryId` → `Category.id` (many-to-one, nullable).
- Transfer = two `Transaction` rows sharing one `transferGroupId`; one with `accountId` = source (negative `amount`), one = destination (positive `amount`).

## Invariants (enforced + tested)
1. **Signed amounts**: `amount`/`amountBase` are signed — expense < 0, income > 0, transfer source < 0, destination > 0. UI shows magnitude.
2. **Derived balance**: `balance(account) = initialBalance + SUM(amount WHERE accountId = ? AND deletedAt IS NULL)`. No stored balance column. Reconciliation (FR 15) holds by construction.
3. **Integer money only**: no float/double at rest or in computation. `Money` has no floating accessor.
4. **Transfer double entry**: a transfer always writes exactly two linked rows; `legSource.amount + legDest.amount == 0` (same-currency, 001). Excluded from expense/income totals by filtering `type != TRANSFER`.
5. **Same-account transfer forbidden**: `legSource.accountId != legDest.accountId`.
6. **Same-currency transfer (001)**: both legs share `currencyCode`; cross-currency deferred to E8.
7. **Soft delete only**: removal sets `deletedAt`; no row is ever physically deleted on user action. Restore clears `deletedAt`. Deleting/restoring one transfer leg applies to the whole `transferGroupId`.
8. **Frozen FX**: `fxRateMillis` and `amountBase` set once at creation, never recomputed (=1000 / =`amount` for same-currency 001).
9. **Atomic writes**: multi-row operations (transfer pair, edit touching two accounts) run in one DB transaction — all-or-nothing.

## Room schema v1 (data layer)
- Tables: `accounts`, `transactions`, `categories`.
- Money columns are `INTEGER` (`Long`): `amount_minor`, `amount_base_minor`, `initial_balance_minor`.
- Indices: `transactions(accountId, deletedAt)` for balance SUM; `transactions(transferGroupId)` for pair ops; `transactions(occurredAt)` for history ordering.
- `TypeConverters`: enums ↔ String. `Money` mapped directly to `Long` (no converter needed beyond field mapping).
- Migration baseline = v1; migration-test harness seeded with sample data (release blocked on any data loss — Article IX).

## State transitions (Transaction)
```
                 create
   (none) ─────────────────▶ Active (deletedAt = null)
      ▲                          │  softDelete (set deletedAt)
      │  undo of just-created    ▼
      └──────────────────── Trashed (deletedAt != null)
                  restore (clear deletedAt) ▲──┘
```
Edit mutates an Active row (and may move it between accounts) within one transaction; balances of all affected accounts re-derive automatically.
