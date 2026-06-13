# Feature Spec Template — `specs/NNN-feature-name/spec.md`

> Platform-neutral. Describes the **WHAT** and **WHY**, never the HOW (no Compose/SwiftUI/DB details — those go in `plan-android.md` / `plan-ios.md`). Must comply with `.specify/memory/constitution.md`.

## 1. Feature
`NNN` · **Name:** … · **Source user stories:** US-… (from `docs/USER_STORIES.md`) · **Epic:** E…

## 2. Problem / why
One or two sentences: what user need this serves and why it matters now.

## 3. In scope / out of scope
- **In scope:** bullet list of behaviors this feature delivers.
- **Out of scope:** explicitly excluded behaviors (deferred features).

## 4. User stories covered
List each story (As a … I want … so that …) with its ID.

## 5. Behavioral requirements (platform-neutral)
Numbered, testable statements. Use **MUST / SHOULD / MUST NOT**. Example: "The system MUST update the account balance immediately after saving a transaction (offline)."

## 6. Acceptance criteria (Gherkin)
Given / When / Then scenarios covering happy path, edge cases, offline behavior, and error states. These are the contract both platforms must pass.

## 7. Data touched
Entities and fields read/written (reference `data-model.md`). Note invariants (e.g., balances reconcile, soft delete only, FX frozen).

## 8. Constitution check
Tick each relevant article and note how it's honored: money integrity (III), speed (IV), AI behavior (V), localization/currency (VII), privacy/offline (I, II). Flag any tension.

## 9. Localization & currency notes
Strings needed (es/en keys), any locale/currency-specific behavior.

## 10. Non-functional targets
Performance budgets that apply (e.g., save ≤ 100 ms), accessibility, offline guarantees.

## 11. Open questions
Anything to resolve via `/speckit.clarify` before planning.

## 12. Definition of Done (per platform)
Matches `docs/USER_STORIES.md`: AC verified, works offline, no data loss, tests, accessibility, localized es+en, performance targets met.
