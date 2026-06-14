# Data Model — Onboarding (002, Android)

> Delta from feature 001's `data-model.md`. Onboarding introduces **one** new table and **no** changes to existing tables. Reuses `Account`, `Currency`, and `Money` exactly as defined in 001.

## Reused entities (unchanged)

- **`Account`** (001) — `id`, `name` (string key), `type` (`AccountType`), `currencyCode`, `initialBalance: Money`, `icon`, `color`, `archived`, `createdAt`, `updatedAt`. Onboarding writes one row with `type = CASH`, `name = "account.defaultName.cash"`, `currencyCode = <base>`, `initialBalance` per user input (default `Money(0, decimalDigits)`), `archived = false`.
- **`Currency`** (001) — `code`, `symbol`, `decimalDigits`, `isEnabled`, `isEssential`. USD + COP seeded by `CurrencySeed` with `isEssential = true, isEnabled = true`. Onboarding may upsert one additional row (`isEnabled = true, isEssential = false`) if the user picks an ISO 4217 currency that wasn't already in the catalog.
- **`Money`** (001) — `Money(minorUnits: Long)` value class; integer minor units only (Article III).

## New entity: `AppSetting`

Key-value store for app-wide settings persisted in Room (schema v2).

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `key` | `String` | `PRIMARY KEY NOT NULL` | One of the well-known keys below |
| `value` | `String` | `NOT NULL` | String-encoded value (ISO code, ISO 8601 timestamp, BCP-47 tag) |

### Well-known keys (defined in `:domain/model/AppSettingKeys.kt`)

| Key | Domain meaning | Value format | Written by |
|---|---|---|---|
| `base_currency` | App-wide base currency (ISO 4217 code) | e.g. `"USD"`, `"COP"`, `"EUR"` | Onboarding completion |
| `language` | Persisted UI language tag (BCP-47) | `"es"` (default for 002), `"en"` (E12) | Onboarding completion |
| `onboarding_completed_at` | Marker that onboarding has finished | ISO 8601 timestamp string | Onboarding completion (LAST write of the atomic block) |

> Other keys (`ai_confidence_threshold`, lock, etc.) listed in `docs/TRD.md §5` will be added by their owning features. Onboarding only uses the three above.

### Invariants

- **(Onboarding atomic completion)** All four onboarding writes — `base_currency`, `language`, the first `Account` row, the optional `Currency` row, and `onboarding_completed_at` — execute in **one** Room `withTransaction { }` block. Either all land or none do.
- **(Marker is the truth)** `IsOnboardingComplete()` evaluates to `true` iff `app_settings[onboarding_completed_at]` is present. Other rows are not consulted for routing.
- **(Currency referential integrity)** Whatever value `app_settings[base_currency]` holds, there MUST exist a corresponding row in `currencies` with that code and `isEnabled = true`. The onboarding transaction enforces this by upserting the currency before writing the base.
- **(USD + COP essential)** `currencies` rows with `code IN ('USD', 'COP')` MUST have `isEssential = true` and MUST NOT be deletable through any code path. Seeded by 001's `CurrencySeed`; the v2 migration MUST NOT touch these rows.

## Schema delta — v1 → v2 (`MIGRATION_1_2`)

Additive only; no data loss (Article IX).

```sql
CREATE TABLE IF NOT EXISTS app_settings (
    key   TEXT PRIMARY KEY NOT NULL,
    value TEXT             NOT NULL
);
```

No changes to `accounts`, `transactions`, `categories`, or `currencies`. Pre-existing rows from a v1 install are preserved exactly. In particular: a v1 install will already contain the legacy auto-seeded `acc-default-cash` row from 001's old `AppSeeder` behavior — the migration leaves it; onboarding still runs (marker absent) and writes a *new* first account chosen by the user. Cleanup of the legacy row is an E3 concern.

## State transitions

`IsOnboardingComplete` is a function of `app_settings[onboarding_completed_at]`:

```text
                    completeOnboarding(...) success
                  ┌────────────────────────────────┐
                  ▼                                │
[onboarding shown]                          [marker present]
  marker == null                              marker != null
  base_currency == null                       base_currency != null
  language == null                            language == "es"
  first account: absent                       first account: present
                  ▲                                │
                  │  transaction rollback (any failure)
                  └────────────────────────────────┘
```

No other transitions. Once `marker != null`, all subsequent launches route directly to the record surface; onboarding is never shown again (spec FR9). Changing the base currency post-onboarding is an E8 concern; editing/archiving the first account is an E3 concern.

## Validation rules

| Rule | Enforced by |
|---|---|
| `base_currency` is a non-empty ISO 4217 code | `CompleteOnboarding` use case (rejects empty / malformed input) |
| Account `currencyCode` == `base_currency` at write time | `CompleteOnboarding` use case |
| `initialBalance.minorUnits >= 0` (overdraft on a brand-new account is meaningless) | `CompleteOnboarding` use case |
| `language` is a supported BCP-47 tag (in 002: must be `"es"`) | `CompleteOnboarding` use case |
| Exactly one account exists after onboarding completes | Spec FR5; verified in tests |

## Indices

`app_settings` has `key` as its primary key — no extra indices needed. Lookup at startup is a single `SELECT value FROM app_settings WHERE key = 'onboarding_completed_at'`, served by the PK index.

## Money / FX

Onboarding does not introduce FX work. The initial balance is denominated in the chosen base currency, so 001's frozen `fxRateMillis = 1.000` and `amountBase = amount` rule applies trivially (no rate to freeze for a balance that already lives in the base currency). E8 will handle multi-account base rollup; 002 is single-account by spec.
