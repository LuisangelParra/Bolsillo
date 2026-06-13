# Bolsillo

Local-first personal finance app (expenses, income, transfers, accounts, budgets) with **on-device AI expense categorization**. Two **native** apps that share **no application code** — only data assets and the feature specs.

- **Android** — Kotlin + Jetpack Compose (`android/`)
- **iOS** — Swift + SwiftUI (`ios/`)
- **Shared data assets** — taxonomy, merchant dictionary, i18n strings, AI models (`shared-assets/`)

> The shared source of truth is the **spec**, not code. See `.specify/memory/constitution.md` (non-negotiable principles) and `docs/`.

## Repository layout

| Path | What |
|---|---|
| `android/` | Native Android app (Gradle, multi-module clean architecture) |
| `ios/` | Native iOS app (Tuist-generated Xcode project, SPM packages) |
| `shared-assets/` | Cross-platform data: `taxonomy/`, `merchant-dictionary/`, `i18n/`, `models/` |
| `docs/` | PRD, TRD, user stories, UI brief (see `docs/README.md`) |
| `.specify/` | Spec Kit: constitution + templates |
| `specs/` | Per-feature specs/plans/tasks (created per feature, not yet present) |

## Core principles (from the constitution)

- **Money** = integer minor units — never `float`/`double`.
- **Local-first / offline**: core features work with no network; nothing leaves the device by default.
- **Privacy**: no mandatory account; AI inference + learning on-device only.
- **No hard deletes**: soft delete + trash everywhere.
- **Localization**: Spanish default, switchable to English. **USD + COP** essential currencies.
- **Clean architecture**: presentation → domain → data; domain depends only on interfaces (ports); AI behind an `ExpenseClassifier` port.

## How to build

### Android (`android/`)
Requires **Android Studio + SDK** and **JDK 21** (the build is pinned to JDK 21 via `gradle.properties`).
```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"   # if not already set
cd android
./gradlew ktlintCheck        # lint
./gradlew :domain:test       # domain unit tests (pure JVM, incl. Money type)
./gradlew assembleDebug      # build the debug APK
./gradlew test               # all unit tests
```

### iOS (`ios/`)
Requires **Xcode 26+** (active: `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`) and **Tuist** (`brew install tuist`). The `.xcodeproj`/`.xcworkspace` are **generated** (gitignored) from the committed Tuist manifests.
```bash
cd ios
tuist generate              # generate the Xcode workspace from Project.swift
swiftlint                   # lint
xcodebuild build -workspace Bolsillo.xcworkspace -scheme Bolsillo \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro'
xcodebuild test  -workspace Bolsillo.xcworkspace -scheme Bolsillo \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro'
```

## Spec-driven development

Per feature, in a branch `NNN-feature-name`:
`/speckit-specify` → `/speckit-clarify` → `/speckit-plan` (per platform) → `/speckit-tasks` (per platform) → `/speckit-analyze` → `/speckit-implement` (per platform). Reference bar: `EXAMPLE_001_fast-expense-recording.md`.
