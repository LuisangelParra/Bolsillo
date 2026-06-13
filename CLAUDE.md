# CLAUDE.md — Bolsillo (root)

This is the agent's source of truth for this repository. Read it before acting.

## What this project is
Bolsillo is a **local-first personal finance app** (expenses, income, transfers, accounts, budgets) with **on-device AI expense categorization**. Two **native** apps: Android (`android/`, Kotlin + Jetpack Compose) and iOS (`ios/`, Swift + SwiftUI). There is **no shared application code** — the shared source of truth is the **specs**.

## Current state (greenfield)
Only docs + specs scaffolding exist so far. `android/`, `ios/`, `shared-assets/`, and `specs/` are NOT yet created, and the repo is NOT git-initialized. Create structure as features begin; don't assume these paths exist.

## Source-of-truth documents (read these, do not contradict them)
- Product: `docs/PRD.md`
- Technical: `docs/TRD.md`
- User stories: `docs/USER_STORIES.md`
- UI brief: `docs/UI_DESIGN_BRIEF.md`
- **Non-negotiable principles:** `.specify/memory/constitution.md` (ALWAYS obey)

## Method: Spec-Driven Development (Spec Kit)
Per feature, in a branch `NNN-feature-name`:
`/speckit-specify` (platform-neutral) → `/speckit-clarify` → `/speckit-plan` (once per platform) → `/speckit-tasks` (per platform) → `/speckit-analyze` → `/speckit-implement` (per platform).
Specs live in `specs/NNN-feature-name/`: `spec.md`, `plan-android.md`, `plan-ios.md`, `tasks-android.md`, `tasks-ios.md`. The spec is platform-neutral; plans/tasks are per platform.
Reference bar for spec/plan/task quality: `EXAMPLE_001_fast-expense-recording.md` (root).

## Hard rules (from the constitution)
- **Money:** integer minor units or fixed-precision Decimal. NEVER `float`/`double` for money.
- **Local-first / offline:** core features work with no network; nothing leaves the device by default.
- **Privacy:** no mandatory account; AI inference/learning on-device only; no financial telemetry.
- **AI:** cascade (user rules → merchant dictionary → on-device ML); confidence shown; never block saving; threshold default 0.75.
- **Speed:** recording ≤ 3 taps / ≤ 5 s; cold start ≤ 1.5 s; save ≤ 100 ms; inference ≤ 200 ms.
- **Localization:** default `es`, switchable to `en`; no hard-coded user-facing strings.
- **Currency:** USD and COP preloaded/essential; base currency configurable; others addable.
- **Architecture:** presentation → domain → data; domain depends only on interfaces; AI behind an `ExpenseClassifier` port.
- **No hard deletes:** soft delete + trash. Transfers = linked double entry; balances must reconcile.
- **Tests** accompany every feature; a migration that loses data blocks release.

## Shared assets (the only things shared across platforms)
`shared-assets/`: `taxonomy/category-taxonomy.json`, `merchant-dictionary/`, `i18n/{es,en}.json`, `models/` (Core ML `.mlpackage`, LiteRT `.tflite`, or ONNX). Keep both platforms consuming the same files.

## Working style
- Use **plan mode** for large changes; use **subagents** (Explore/Plan) to keep main context clean.
- Run `/speckit-analyze` before implementing. If platforms diverge in behavior, fix the **spec** first.
- Prefer small, reviewed PRs per platform. Keep `specs/` updated as the source of truth.

## Build & test
- Android: `android/CLAUDE.md` (Gradle tasks) — to be created when `android/` is scaffolded.
- iOS: `ios/CLAUDE.md` (Xcode/SPM commands) — to be created when `ios/` is scaffolded.
