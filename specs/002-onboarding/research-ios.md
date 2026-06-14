# Research — Onboarding & initial setup (002, iOS)

> Phase 0 output for `plan-ios.md`. iOS-specific decisions; behavior parity with `research.md` (Android) ensured. All `NEEDS CLARIFICATION` resolved here. References to spec sections use `spec.md §N`.

## D1 — Settings persistence: GRDB `app_settings` table vs `UserDefaults`

**Decision**: Add a new GRDB table `app_settings(key TEXT PRIMARY KEY, value TEXT NOT NULL)` (schema v2) and a `GRDBSettingsRepository`. **Reject** `UserDefaults`.

**Rationale**:
- Spec §5 Invariant 10 + Article III require **atomic** persistence of `base_currency`, `language`, the first account row, and the onboarding-complete marker — either all four land or none. The account lives in GRDB (`accounts` table from 001); only GRDB can wrap it and the settings writes in a single `write {}` block. `UserDefaults` is a separate `.plist` flushed independently — a crash between the GRDB commit and the `UserDefaults` synchronize call leaves an orphan account row and no marker, exactly the partial state spec §5 Invariant 10 forbids.
- Avoiding `UserDefaults` also avoids a second persistence surface, a second backup-restore behavior (`UserDefaults` is included in iCloud backup by default, while GRDB+SQLCipher data stays on-device per `KeychainKeyProvider` design — Article I/II).
- Schema cost is minimal: one additive migration, no data loss (Article IX).

**Alternatives considered**:
- **`UserDefaults` for settings + GRDB for the account, with "marker last in UserDefaults"**: rejected on atomicity grounds above + leak into iCloud backup violates Article II's "nothing leaves the device" default.
- **CoreData**: rejected — second persistence framework in the same app for no behavioral gain.
- **Keychain for the marker**: rejected — Keychain is for the SQLCipher key, not arbitrary app state; still doesn't solve the atomicity problem (it's a third store).

## D2 — Atomic write strategy

**Decision**: One GRDB `db.queue.write { dbq in ... }` block that writes, in order: `base_currency`, `language`, an optional `currencies` upsert (if the picked currency wasn't already enabled), the first account row, and finally `onboarding_completed_at`. Any thrown error rolls back all five.

**Rationale**:
- Single GRDB transaction = SQLite atomicity ⇒ Article III guarantee holds by construction.
- Writing the completion marker **last** means absence of marker ⇒ onboarding incomplete, regardless of what other rows exist.
- The `IsOnboardingComplete` use case keys off the marker only — see D3.
- GRDB's `DatabaseQueue.write` propagates `throw` as a rollback — no manual `BEGIN/COMMIT/ROLLBACK` plumbing needed.

**Alternatives considered**:
- **Marker first**: rejected — a crash after the marker but before the account write would land on the recording screen with no account.
- **Per-row commits with idempotent retry**: rejected — far more complex than the spec needs, and still fragile under crash.

## D3 — First-launch detection signal

**Decision**: The single source of truth is `app_settings[AppSettingKeys.onboardingCompletedAt]`. Routing rule: `onboardingComplete = (marker != nil)`. (`spec.md §12` assumption locked.)

**Rationale**:
- Decouples routing from any individual data row — clean recovery semantics.
- Keeps a single key to read at startup (one indexed `SELECT`, no joins) → no impact on cold-start budget (Article IV).

**Alternatives considered**:
- **Absence-of-`base_currency`**: rejected on its own — possible if the user partially completes and then the DB is wiped to an inconsistent state.
- **Both signals (belt-and-suspenders)**: rejected — D2's single-transaction guarantee makes redundancy unnecessary; the marker is the tiebreaker.

## D4 — Currency-list-order data shape

**Decision**: `GetEnabledCurrencies` use case in `BolsilloDomain` returns `AsyncStream<[Currency]>` where:
1. USD always at index 0.
2. COP always at index 1.
3. All other enabled currencies sorted alphabetically by `code`, appended after.

Pinning is **invariant** across the user's base choice and device locale (spec FR3 clarification). The view just iterates the list.

**Rationale**:
- Centralizes the rule in the domain so any future picker shares the same order.
- Pure stream transformation over the catalog ⇒ unit-testable without GRDB.
- USD/COP rows render with an essential badge regardless of position (the badge is keyed off `Currency.isEssential`, not list index).

**Alternatives considered**:
- **Sort by selected base first**: rejected — spec FR3 says the pinning is invariant regardless of base choice.
- **View-side sort**: rejected on Article VIII (clean architecture) — order is a domain rule.

## D5 — Default base currency on the "Use defaults" path

**Decision**: `ResolveDefaultBaseCurrency` returns `"COP"` if `LocaleProvider.regionCode() == "CO"`, otherwise `"USD"`. (`spec.md §12` assumption.)

**Rationale**:
- Honors the LATAM-first product framing (app default is `es`) without penalizing users outside Colombia.
- Either choice remains switchable in the picker if the user backs out of the defaults path.
- `LocaleProvider` lives in `BolsilloDomain`; its impl (`LocaleProviderImpl` in `BolsilloData`) just wraps `Locale.current.region?.identifier`. Test by injecting a stub `LocaleProvider`.

**Alternatives considered**:
- **Always USD**: rejected — needlessly friction-y for the most likely first-cohort users (es-CO).
- **Always COP**: rejected — wrong for the majority of non-CO Spanish-speaking users and worse for any non-Spanish locale.
- **Prompt the user explicitly**: rejected — that *is* the picker, which the user opted to skip.

If product later prefers a fixed default, the use case is the single place to change.

## D6 — SwiftUI app routing + locale application

**Decision**:
- `BolsilloApp.body` (root `Scene`) shows `OnboardingView` or `RecordView` based on a `@State` `onboardingComplete: Bool?` populated from `root.settings.isOnboardingComplete()` in a `.task { }` on the root container.
- During the brief `nil` window (the suspend `await` resolving), the app shows the design-token neutral background (`bolsilloTheme.colors.background`) with no spinner (Article IV — no loading screens in onboarding/recording paths).
- Locale: SwiftUI's `.environment(\.locale, Locale(identifier: persistedLanguage ?? "es"))` applied at the root view modifier. iOS does not need a UIKit-level locale switch in 002 (the persisted value is always `"es"`); E12 will swap this dynamically. The `.xcstrings` resolves through `Bundle.module` against the environment locale.
- After `OnboardingEvent.finishedToRecord`, the root sets `onboardingComplete = true` and swaps to `RecordView`. On subsequent cold starts the suspend `await` resolves to `true` immediately ⇒ recording is the start view ⇒ onboarding never shown again.

**Rationale**:
- Pure SwiftUI, no `UIWindowSceneDelegate` plumbing.
- The `@State` swap is a state change, not a navigation push — no animation glitches, no back-stack confusion.
- Environment-locale override is the idiomatic SwiftUI way; works with `.xcstrings` resource bundles out of the box.

**Alternatives considered**:
- **`NavigationStack` with two roots**: rejected — onboarding and recording are mutually-exclusive launch destinations, not stack pages; a `NavigationStack` over `OnboardingView` would suggest a back affordance that doesn't make sense.
- **`UIApplication.shared.windows.first?.rootViewController = ...`**: rejected — UIKit interop unnecessary on iOS 17+ with SwiftUI lifecycle.
- **`AppDelegate.applicationDidBecomeActive` locale apply**: rejected — environment override is cleaner and confined to the SwiftUI tree.

## D7 — Source of the "choose another" ISO 4217 list

**Decision**: Static bundled `Iso4217Catalog.swift` in `BolsilloData` (Swift enum returning a `[Currency]` list). Backed by `shared-assets/currency/iso4217.json` (to be added to shared assets in T002) — a small Swift code-gen step or `JSONDecoder` at startup builds the list. UI reads it via `CurrencyRepository.getIso4217Candidates()`.

**Rationale**:
- Article I (offline) forbids a network call to populate the picker.
- Bundling keeps the asset small (~3 KB) and shareable with Android (single source of truth).
- Localized names resolve through `currency.name.<code>` keys (Article VII), so the catalog stores codes + decimal digits only.

**Alternatives considered**:
- **`Locale.commonISOCurrencyCodes`**: rejected — gives codes but not symbols/decimal-digits in a shared schema with Android; introduces platform-specific behavior in a domain port.
- **Inline the full list in Swift code**: rejected — readability + parity with Android suffer; better to share the asset.
- **Fetch lazily from a server**: rejected on Article I.

## D8 — `CompositionRoot` change: stop auto-creating the default Cash account

**Decision**: Remove the unconditional `try AccountSeed.seed(db)` call from `CompositionRoot.init()`. `CurrencySeed.essentials` insert via `BolsilloDatabase` first-open **remains** (the catalog is independent of user choice). `CategorySeed.seed(db)` **remains** (categories are needed for recording, independent of onboarding). Onboarding now owns first-account creation.

**Rationale**:
- The seed currently lands a USD-denominated account before the user has a chance to choose a currency — directly contradicting spec FR4/FR5 (the first account's currency must equal the user-selected base).
- An existing dev install already has the seeded `default-cash` row; the v1→v2 migration leaves it intact. The routing check is keyed off the onboarding marker, not account existence, so a v1 user without a marker enters onboarding once on the v2 update (they can keep or replace the seeded account — out of scope for 002, owned by E3).

**Alternatives considered**:
- **Keep seeding USD Cash and let onboarding "edit" it**: rejected — onboarding never edits; that violates the single-write-per-step contract and reintroduces partial-state risk.
- **Delete the seeded account on first onboarding completion**: rejected — hard delete violates Article III.

## Risks / open items

- **Migration v1→v2 on an existing dev install**: the seeded `default-cash` row remains in `accounts`. Acceptable for 002 (E3 will surface it for archive/edit). Documented in [data-model.md](./data-model.md).
- **`LocaleProvider` impl on iOS 16 vs 17**: `Locale.current.region` is iOS 16+. Deployment target is iOS 17, so safe.
- **`Bundle.module` for `FeatureOnboarding`**: requires the package target to declare `resources: [.process("Resources")]` so the `.xcstrings` ships inside the module bundle. Already established for `FeatureRecord` in 001.
- **SwiftUI sheet on iPad**: `IsoCurrencyPickerSheet` should declare a fixed `presentationDetents` ([.medium, .large]) for consistent rendering — iPhone-only at MVP but design parity matters.

## No remaining `NEEDS CLARIFICATION`

All Technical Context values are resolved. Plan re-evaluation post-design: Constitution Check still PASS (Articles I, II, III, IV, VI, VII, VIII, IX explicitly satisfied; V N/A).
