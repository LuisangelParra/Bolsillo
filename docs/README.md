# Bolsillo — Documentation index

The shared source of truth for both native apps lives here and in `.specify/`. Implementations must not contradict these documents; if platforms diverge, fix the **spec** first.

| Document | Purpose |
|---|---|
| [PRD.md](./PRD.md) | Product requirements — vision, scope, functional requirements, personas. |
| [TRD.md](./TRD.md) | Technical requirements — stack, architecture, data model, on-device AI engine, performance budgets. |
| [USER_STORIES.md](./USER_STORIES.md) | Epics and user stories (referenced by feature specs). |
| [UI_DESIGN_BRIEF.md](./UI_DESIGN_BRIEF.md) | UI/UX direction and design constraints. |
| [../.specify/memory/constitution.md](../.specify/memory/constitution.md) | **Non-negotiable principles.** Always obey. Verified by `/speckit-plan` and `/speckit-analyze`. |
| [../EXAMPLE_001_fast-expense-recording.md](../EXAMPLE_001_fast-expense-recording.md) | Worked example — the quality bar for every spec/plan/task. |

Feature specs (platform-neutral) and per-platform plans/tasks live in `specs/NNN-feature-name/` once features begin.
