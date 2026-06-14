# Research — Onboarding & initial setup (002, Android)

> Phase 0 output for `plan-android.md`. All `NEEDS CLARIFICATION` resolved here. References to spec sections use `spec.md §N`.

## D1 — Settings persistence: Room `app_settings` table vs DataStore Preferences

**Decision**: Add a new Room table `app_settings(key TEXT PRIMARY KEY, value TEXT)` (schema v2) and a `RoomSettingsRepository`. **Reject** DataStore Preferences.

**Rationale**:
- Spec §5 Invariant 10 + Article III require **atomic** persistence of `base_currency`, `language`, the first account row, and the onboarding-complete marker — either all four land or none. The account lives in Room (`accounts` table from 001); only Room can wrap it and the settings writes in a single transaction. DataStore is a separate file/process — `db.withTransaction { dataStore.edit { } }` doesn't actually couple the two stores.
- Avoiding DataStore also avoids a second persistence dependency, a second IO surface, and a second set of migration concerns.
- Schema cost is minimal: one migration (additive, no data loss).

**Alternatives considered**:
- **DataStore Preferences for settings + Room for the account, with a "completion marker is the last DataStore write" rule**: rejected — a crash between the Room commit and the DataStore commit leaves an orphan account row and no marker, which is exactly the partial state spec §5 Invariant 10 forbids.
- **SharedPreferences**: rejected on the same atomicity grounds, plus no `Flow` observation without a wrapper.

## D2 — Atomic write strategy

**Decision**: One Room `withTransaction { }` block that writes, in order: `base_currency`, `language`, an optional `currencies` upsert (if the picked currency wasn't already enabled), the first account row, and finally `onboarding_completed_at`. Any failure rolls back all five.

**Rationale**:
- Single transaction = SQLite atomicity ⇒ Article III guarantee holds by construction.
- Writing the completion marker **last** means absence of marker ⇒ onboarding incomplete, regardless of what other rows exist.
- The `IsOnboardingComplete` use case keys off the marker only — see D3.

**Alternatives considered**:
- **Marker first**: rejected — a crash after the marker but before the account write would land on the recording screen with no account.
- **Per-row commits with idempotent retry**: rejected — far more complex than the spec needs, and still fragile under crash.

## D3 — First-launch detection signal

**Decision**: The single source of truth is `app_settings[ONBOARDING_COMPLETED_AT_KEY]`. Routing rule: `onboardingComplete = (marker != null)`. (`spec.md §12` assumption is now locked.)

**Rationale**:
- Decouples routing from any individual data row — clean recovery semantics.
- Keeps a single key to read at startup (one indexed SELECT, no joins) → no impact on cold-start budget (Article IV).

**Alternatives considered**:
- **Absence-of-`base_currency`**: rejected on its own — possible if the user partially completes and then the DB is wiped to an inconsistent state.
- **Both signals (belt-and-suspenders)**: rejected — D2's single-transaction guarantee makes the redundancy unnecessary; we'd still trust the marker as the tiebreaker.

## D4 — Currency-list-order data shape

**Decision**: `GetEnabledCurrencies` use case in `:domain` returns `List<Currency>` where:
1. USD always at index 0.
2. COP always at index 1.
3. All other enabled currencies sorted alphabetically by `code`, appended after.

The pinning is **invariant** across the user's base choice and device locale (spec FR3 clarification). UI just iterates the list.

**Rationale**:
- Centralizes the rule in the domain so both screens (`CurrencyPickerScreen` and any future picker) share the same order.
- Pure function over the catalog ⇒ easy unit-testable.
- USD/COP rows render with an essential badge regardless of position (the badge is keyed off `Currency.isEssential`, not list index).

**Alternatives considered**:
- **Sort by selected base first**: rejected — spec FR3 explicitly says the pinning is invariant regardless of base choice.
- **UI-side sort**: rejected on Article VIII (clean architecture) — the order is a domain rule, not a presentation detail.

## D5 — Default base currency on the "Use defaults" path

**Decision**: `ResolveDefaultBaseCurrency` returns `"COP"` if `LocaleProvider.regionCode() == "CO"`, otherwise `"USD"`. (`spec.md §12` assumption.)

**Rationale**:
- Honors the LATAM-first product framing (app default is `es`) without penalizing users outside Colombia.
- Either choice remains switchable in the picker if the user backs out of the defaults path.

**Alternatives considered**:
- **Always USD**: rejected — needlessly friction-y for the most likely first-cohort users (es-CO).
- **Always COP**: rejected — wrong for the majority of non-CO Spanish-speaking users and worse for any non-Spanish locale.
- **Prompt the user explicitly**: rejected — that *is* the picker, which the user opted to skip.

If product later prefers a fixed default, the use case is the single place to change.

## D6 — App routing + locale application

**Decision**: `MainActivity` calls a suspend `IsOnboardingComplete()` on `Dispatchers.IO` before composing the root NavHost. NavHost start destination = `RecordRoute` if true, else `OnboardingRoute`. After `OnboardingUiEvent.FinishedToRecord`, the nav controller does `popUpTo(OnboardingRoute) { inclusive = true }` then `navigate(RecordRoute)`. Locale is applied via `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))` *once*, immediately after the atomic write commits successfully.

**Rationale**:
- Single check at app start; routing decision is independent of UI composition cost.
- Locale apply is idempotent (calling it with the already-active locale is a no-op) and survives restart because Android persists `AppCompatDelegate.setApplicationLocales` to `per-app language preferences`.
- No ambient `LocalConfiguration` recomposition penalty during onboarding since we set the locale only after the user finishes (the welcome screen renders in the device's default language until the marker is committed — by spec, the default is `es` anyway).

**Alternatives considered**:
- **Set the locale at app process start**: rejected — premature; we honor whatever the device language is on the welcome screen, then lock to `es` once the user is committed.
- **Use `Configuration` overrides**: rejected — `AppCompatDelegate.setApplicationLocales` is the recommended path on minSdk 26.

## D7 — Source of the "choose another" ISO 4217 list

**Decision**: Static bundled `Iso4217Catalog` in `:data` (Kotlin object backed by a generated list from a small JSON in `shared-assets/currency/iso4217.json` — to be added to the shared assets in T002). UI reads it via a `CurrencyRepository.getIso4217Candidates()` method.

**Rationale**:
- Article I (offline) forbids a network call to populate the picker.
- Bundling keeps the asset small (~3 KB) and shareable with iOS.
- Localized names resolve through `currency.name.<code>` keys (Article VII), so the catalog stores codes + decimal digits only.

**Alternatives considered**:
- **Inline the full list in code**: rejected — readability + parity with iOS suffer; better to share the asset.
- **Fetch lazily from a server**: rejected on Article I.

## D8 — `AppSeeder` change: stop auto-creating the default Cash account

**Decision**: Remove the unconditional `accountDao.upsert(AccountSeed.defaultCash(...))` from `AppSeeder` on first DB open. `CurrencySeed.essentials` and `CategorySeed.ALL` inserts **remain** (the catalog and category taxonomy are independent of user choice). Onboarding now owns first-account creation.

**Rationale**:
- The seed currently lands a USD-denominated account before the user has a chance to choose a currency — directly contradicting spec FR4/FR5 (the first account's currency must equal the user-selected base).
- A v1-installed user already has the seeded account; the v1→v2 migration leaves their data intact. The routing check is keyed off the onboarding marker, not account existence, so a v1 user without a marker enters onboarding once on the v2 update (they can keep or replace the seeded account — out of scope for 002, owned by E3).

**Alternatives considered**:
- **Keep seeding USD Cash and let onboarding "edit" it**: rejected — onboarding never edits; that violates the single-write-per-step contract and reintroduces partial-state risk.
- **Delete the seeded account on first onboarding completion**: rejected — hard delete violates Article III.

## Risks / open items

- **Migration v1→v2 on an existing dev install**: the seeded `acc-default-cash` row remains in `accounts`. Acceptable for 002 (E3 will surface it for archive/edit). Documented in [data-model.md](./data-model.md).
- **`LocaleProvider` impl**: trivial wrapper around `LocaleListCompat.getDefault().get(0)?.country`. Tested in `androidTest`.
- **`AppCompatDelegate.setApplicationLocales` requires `androidx.appcompat:appcompat` ≥ 1.6**. Already on the classpath.

## No remaining `NEEDS CLARIFICATION`

All Technical Context values are resolved. Plan re-evaluation post-design: Constitution Check still PASS (Articles I, II, III, IV, VI, VII, VIII, IX explicitly satisfied; V N/A).
