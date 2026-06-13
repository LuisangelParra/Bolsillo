# Implementation Plan: Fast expense recording

**Feature**: `001-fast-expense-recording` | **Spec**: [spec.md](./spec.md) | **Constitution**: `.specify/memory/constitution.md`

> **This project plans per platform.** There is no single platform-neutral implementation plan — the spec (`spec.md`) is the platform-neutral source of truth, and each native platform has its own plan:
>
> - **Android** — [plan-android.md](./plan-android.md) (Kotlin / Jetpack Compose, Room + SQLCipher)
> - **iOS** — [plan-ios.md](./plan-ios.md) (Swift / SwiftUI, GRDB + SQLCipher)
>
> Companion design artifacts: [research.md](./research.md) / [research-ios.md](./research-ios.md), [data-model.md](./data-model.md) (shared) / [data-model-ios.md](./data-model-ios.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md) / [quickstart-ios.md](./quickstart-ios.md).
>
> Tasks are likewise per platform: [tasks-android.md](./tasks-android.md), [tasks-ios.md](./tasks-ios.md).

This file exists only as an index so prerequisite tooling that expects `plan.md` resolves; the binding plans are the two per-platform documents above. Any behavioral divergence between platforms is fixed in `spec.md` first (Article VI).
