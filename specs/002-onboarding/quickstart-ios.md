# Quickstart — Onboarding (002, iOS)

> Build & validate guide. Pairs with [plan-ios.md](./plan-ios.md), [data-model.md](./data-model.md), and [contracts/](./contracts/). Doesn't duplicate implementation details.

## Prerequisites

- Xcode 15+ / 16; `ios` directory is the working dir.
- Tuist installed: `curl -Ls https://install.tuist.io | bash`.
- Feature 001 already merged (provides `BolsilloDomain`, `BolsilloData`, `BolsilloDesignSystem`, `FeatureRecord`, GRDB+SQLCipher DB v1, Keychain key bootstrap).
- A clean simulator (or `xcrun simctl erase all`) for first-launch testing.

## Build the new package product

```bash
cd ios
# After Package.swift adds FeatureOnboarding library + target:
swift build --package-path Packages/Bolsillo --target FeatureOnboarding

# Regenerate the Xcode project (Project.swift gains FeatureOnboarding as app dep):
tuist generate --no-open
```

`Package.swift` must declare:
- `.library(name: "FeatureOnboarding", targets: ["FeatureOnboarding"])` in `products`
- `.target(name: "FeatureOnboarding", dependencies: ["BolsilloDomain", "BolsilloDesignSystem"], resources: [.process("Resources")])`
- `.testTarget(name: "FeatureOnboardingTests", dependencies: ["FeatureOnboarding", "BolsilloDomain", "BolsilloData", .product(name: "GRDB", package: "GRDB.swift")])`

`Project.swift` must add `.package(product: "FeatureOnboarding")` to the `Bolsillo` app target dependencies.

`BolsilloDatabase.makeMigrator()` must register `DatabaseMigratorV2` after V1:
```swift
DatabaseMigratorV1.register(in: &migrator)
DatabaseMigratorV2.register(in: &migrator)
```

## Run the Swift Testing unit tests

```bash
swift test --package-path Packages/Bolsillo
```

Domain suite must include:
- `GetEnabledCurrenciesTests` — USD@0, COP@1, rest sorted; pinning invariant across inputs.
- `EnableCurrencyTests` — idempotent; never flips `isEssential`; throws on unknown code.
- `ResolveDefaultBaseCurrencyTests` — COP for `"CO"`, USD otherwise (incl. nil).
- `CompleteOnboardingTests` — rejects negative balance / mismatched currency / empty code.

Data suite (simulator-bound via `xcodebuild test`) must include:
- `AppSettingRoundTripTests` — `GRDBSettingsRepository` round-trip.
- `OnboardingAtomicityTests` — forced failure rolls back all writes; `isOnboardingComplete()` returns `false`.
- `MigrationV1ToV2Tests` — v1 seeded sample data still intact; `app_settings` table exists; balances reconcile.

## Run the UI tests

```bash
xcodebuild test \
  -workspace Bolsillo.xcworkspace \
  -scheme Bolsillo \
  -destination 'platform=iOS Simulator,name=iPhone 16,OS=latest' \
  -only-testing:BolsilloUITests/OnboardingUITests \
  CODE_SIGNING_ALLOWED=NO
```

`BolsilloUITests/OnboardingUITests` covers each acceptance scenario from `spec.md §6`:

| Scenario | Test |
|---|---|
| Completing onboarding with defaults (≤ 60 s) | `testDefaultsPath_landsOnRecording_underOneMinute` |
| Choosing COP as base currency | `testPickCop_persistsCopAsBase` |
| Choosing USD as base currency | `testPickUsd_persistsUsdAsBase` |
| Choosing another ISO 4217 currency (EUR) | `testPickEur_enablesEurAsNonEssential_setsBase` |
| Essentials are non-removable | `testEssentials_areNotRemovable` |
| Currency list order (USD@0, COP@1) | `testCurrencyOrder_usdFirstCopSecond` |
| Default account name and type | `testDefaultsPath_accountIsCash_nameKeyResolves` |
| Single account only in onboarding | `testOnboarding_exposesNoMultiAccountAffordance` |
| No language selector in onboarding | `testOnboarding_hasNoLanguagePickerAccessibilityID` |
| Re-launch skips onboarding | `testRelaunch_goesStraightToRecording` |
| Crash mid-onboarding is recoverable | `testCrashMidWrite_restartsCleanOnboarding` (uses an injected throw in `SettingsRepository`) |
| Default language (es) | covered by `testDefaultsPath_…` (asserts persisted `language == "es"`) |
| es + en rendering | `testOnboarding_rendersEsAndEn_noTruncation` |

## Manual validation in airplane mode

1. Boot a fresh simulator: `xcrun simctl erase all && open -a Simulator`.
2. Enable airplane mode in the simulator (Settings → Airplane Mode).
3. Run the app from Xcode — assert the welcome view renders ≤ 1.5 s with no spinner.
4. Tap **Usar valores predeterminados** — assert landing on the recording screen within ~5 s wall-clock.
5. Inspect the DB:
   ```bash
   APP_DIR=$(xcrun simctl get_app_container booted com.bolsillo.app data)
   sqlite3 "$APP_DIR/Documents/bolsillo.sqlite" \
     "SELECT key, value FROM app_settings; SELECT id, name, type, currencyCode, initialBalanceMinor FROM accounts;"
   ```
   Assert:
   - `app_settings` has `base_currency`, `language = es`, `onboarding_completed_at`.
   - `accounts` has exactly one new row with `type = cash`, `currencyCode = USD` (or `COP` on a `CO` simulator).
6. Kill the app from the multitask switcher and relaunch — assert it goes straight to recording; no onboarding shown.

> Note: a dev install from feature 001 will additionally have the legacy seeded `default-cash` row. Acceptable for 002 — cleanup is owned by E3.

## Performance check

- Cold start to welcome view MUST be ≤ 1.5 s on the reference device (use the Instruments Time Profiler from app launch to first frame).
- `completeOnboarding(...)` write MUST complete ≤ 100 ms; measure via `XCTMetric` (`XCTClockMetric` around the call).

## Lint

```bash
swiftlint --config ios/.swiftlint.yml
```

No hard-coded strings (`String(localized: ..., bundle: .module)` everywhere); no literal hex/dp/sp in views (read tokens through `bolsilloTheme.*`).

## Regenerate strings from shared assets

After adding the new onboarding keys to `shared-assets/i18n/{es,en}.json`:

```bash
# Same generation step as 001's FeatureRecord Localizable.xcstrings:
.specify/scripts/bash/sync-i18n.sh ios FeatureOnboarding
```

This writes `Sources/FeatureOnboarding/Resources/Localizable.xcstrings` with the dotted keys verbatim (no renaming — keys map 1:1 from shared JSON to `.xcstrings` keys; this differs from Android's underscoring).
