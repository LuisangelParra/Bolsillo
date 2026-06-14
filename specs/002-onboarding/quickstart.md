# Quickstart — Onboarding (002, Android)

> Build & validate guide. Pairs with [plan-android.md](./plan-android.md), [data-model.md](./data-model.md), and [contracts/](./contracts/). Doesn't duplicate implementation details.

## Prerequisites

- Android Studio Ladybug+ (or matching CLI: JDK 17 toolchain, `compileSdk` 35).
- Repo cloned; `android/` is the working directory.
- Feature 001 already merged (provides `:domain`, `:data`, `:designsystem`, `:feature-record`, Room DB v1, SQLCipher key bootstrap).
- A clean emulator (or `adb shell pm clear com.bolsillo.app`) for first-launch testing.

## Build the new module

```bash
cd android
./gradlew :feature-onboarding:assembleDebug
./gradlew assembleDebug
```

First run will fail until `:feature-onboarding` is added to `settings.gradle.kts`:

```kotlin
include(":feature-onboarding")
```

…and the new `app_settings` migration is wired into `BolsilloDatabase` (`addMigrations(MIGRATION_1_2)`, `version = 2`).

## Run the JVM unit tests

```bash
./gradlew :domain:test
./gradlew :data:test
```

Domain suite must include:
- `GetEnabledCurrenciesTest` — USD@0, COP@1, rest sorted; pinning invariant across inputs.
- `EnableCurrencyTest` — idempotent; never flips `isEssential`.
- `ResolveDefaultBaseCurrencyTest` — COP for `"CO"`, USD otherwise (incl. null).
- `CompleteOnboardingTest` — rejects negative balance / mismatched currency / empty code.

Data suite must include:
- `AppSettingDaoTest` — round-trip.
- `RoomSettingsRepositoryAtomicityTest` — forced failure rolls back all writes; `isOnboardingComplete()` returns `false`.
- `Migration1To2Test` — v1 seeded sample data still intact after migration; `app_settings` table exists.

## Run the instrumentation tests

```bash
./gradlew :feature-onboarding:connectedDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

The `:feature-onboarding` suite covers each acceptance scenario from `spec.md §6`:

| Scenario | Test |
|---|---|
| Completing onboarding with defaults (≤ 60 s) | `DefaultsPathUITest` |
| Choosing COP as base currency | `PickCopUITest` |
| Choosing USD as base currency | `PickUsdUITest` |
| Choosing another ISO 4217 currency (EUR) | `PickIsoUITest` |
| Essentials are non-removable | `EssentialsNonRemovableUITest` |
| Currency list order (USD@0, COP@1) | `CurrencyOrderUITest` |
| Default account name and type | `DefaultAccountFixedUITest` |
| Single account only in onboarding | `SingleAccountUITest` |
| No language selector in onboarding | `NoLanguageSelectorUITest` |
| Re-launch skips onboarding | `RelaunchSkipsOnboardingUITest` |
| Crash mid-onboarding is recoverable | `CrashRecoveryUITest` (uses an injected failure in `SettingsRepository`) |
| Default language (es) | covered by `DefaultsPathUITest` (asserts persisted `language == "es"`) |
| es + en rendering | `OnboardingLocalizationUITest` |

## Manual validation in airplane mode

1. Cold-boot the emulator.
2. Enable airplane mode (`adb shell settings put global airplane_mode_on 1 && adb shell am broadcast -a android.intent.action.AIRPLANE_MODE`).
3. Launch the app — assert no network spinner, welcome screen renders ≤ 1.5 s.
4. Tap **Usar valores predeterminados** — assert landing on the recording screen within ~5 s wall-clock.
5. Inspect the DB (`adb shell run-as com.bolsillo.app sqlite3 ...`) to confirm:
   - `app_settings` has `base_currency`, `language = es`, `onboarding_completed_at`.
   - `accounts` has exactly one new row with `type = CASH`, `currencyCode = USD` (or `COP` on a `CO` device).
6. Kill the app and relaunch — assert it goes straight to the recording screen; no onboarding shown.

## Performance check

- Cold start to welcome screen MUST be ≤ 1.5 s on the reference low-end device (Android 8, 2 GB RAM).
- `completeOnboarding(...)` write MUST complete ≤ 100 ms; measure via Android Studio Profiler or a Macrobenchmark in `:app`.

## Lint / style

```bash
./gradlew ktlintCheck
```

No hard-coded strings (`stringResource(...)` everywhere); no literal hex/dp/sp in composables (read tokens through `BolsilloTheme.*`).

## Regenerate strings from shared assets

After adding the new onboarding keys to `shared-assets/i18n/{es,en}.json`:

```bash
./gradlew :feature-onboarding:syncSharedI18n  # task added in T004a-equivalent for 002
```

This task converts dotted keys → underscored resource names (`onboarding.cta.useDefaults` → `onboarding_cta_useDefaults`) and writes them into `feature-onboarding/src/main/res/values/strings.xml` (es) + `values-en/strings.xml`.
