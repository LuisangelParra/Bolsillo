# CLAUDE.md — Bolsillo (Android)

Platform-specific agent guidance. Obey root `../CLAUDE.md` and `../.specify/memory/constitution.md` first.

## Stack
- Kotlin 2.0.21, Jetpack Compose (BOM 2024.12.01, Material 3), Hilt DI.
- Persistence wired but not yet used: Room 2.6.1 + SQLCipher 4.6.1.
- Async: kotlinx-coroutines 1.9.0 (Flow).
- Build: AGP 8.7.3, `compileSdk`/`targetSdk` 35, `minSdk` 26, JDK 17 toolchain (CI runs on JDK 21).
- Lint: ktlint (jlleitschuh plugin 12.1.1). Versions are centralized in `gradle/libs.versions.toml`.

## Module layout
Clean architecture, one Gradle module per layer. Packages under `com.bolsillo.*`.
- `:app` — Compose entry point, Hilt app, theme, navigation.
- `:core` — cross-cutting utilities (e.g. `DispatcherProvider`).
- `:domain` — value types + ports only, no Android deps (`Money`, `Currency`, `Transaction`, `ExpenseClassifier`, repository ports).
- `:data` — port implementations, DI wiring, currency/account seeds.
- `:feature-record` — fast expense-recording UI feature.
Dependency direction: presentation → domain ← data. `:domain` depends on nothing platform-specific; AI sits behind the `ExpenseClassifier` port.

## Build & test
Run from `android/`:
- `./gradlew ktlintCheck` — lint.
- `./gradlew :domain:test :data:test` — JVM unit tests.
- `./gradlew assembleDebug` — build debug APK.

## Money rule (non-negotiable)
Money is **integer minor units** via the `Money` value class (`minorUnits: Long`). NEVER `Double`/`Float` for money — no floating-point constructor or accessor exists on `Money`, keep it that way. Minor-unit count per currency comes from `Currency.decimalDigits`.

## Localization rule
Default locale `es`; `en` switchable via `values-en/`. NO hard-coded user-facing strings — every string lives in `res/values/strings.xml` (es) with an `en` counterpart in `res/values-en/strings.xml`.

## Source of truth
The spec in `specs/NNN-feature-name/` (`spec.md` + `plan-android.md` + `tasks-android.md`) governs. If code and spec disagree, fix the spec first, then the code.
