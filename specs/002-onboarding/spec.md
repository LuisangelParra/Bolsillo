# Feature Spec — `specs/002-onboarding/spec.md`

> Platform-neutral. Describes the **WHAT** and **WHY**, never the HOW. Must comply with `.specify/memory/constitution.md`.

## 1. Feature
`002` · **Name:** Onboarding and initial setup · **Source user stories:** US-1.1, US-1.2, US-1.3 (from `docs/USER_STORIES.md`) · **Epic:** E1

## 2. Problem / why
A new user must reach value (recording an expense) in under a minute with no account, no email, and no network. Forcing sign-up or long setup destroys the speed-first promise (Article IV) and the privacy/no-account promise (Article II). Onboarding must collect just enough to make balances meaningful — base currency, one initial account — and then get out of the way; reopens MUST go straight to recording.

## Clarifications

### Session 2026-06-13
- Q: What name and type does the default first account use? → A: Type is **cash**; display name resolves from the localized key `account.defaultName.cash` (es: "Efectivo", en: "Cash") — no free-form name entry on the defaults path; editing the name is deferred to E3.
- Q: What is the default initial balance of the first account? → A: **0** (zero) — confirmable without typing, no per-currency offset; honors `decimal_digits` (stored as `0` minor units).
- Q: How many accounts may onboarding create? → A: **Exactly one.** All multi-account creation, editing, and archiving is deferred to E3 (`002-accounts-balances`).
- Q: Does onboarding expose a language selector? → A: **No.** Locale silently defaults to `es` and is persisted; switching languages is deferred to E12 (`010-localization`). Onboarding MUST NOT render any language-choice control.
- Q: How is the currency picker list ordered? → A: **USD and COP MUST appear pinned at the top** of the picker (visually marked essential and non-removable). Any other ISO 4217 currency the user reaches via the "choose another" affordance appears below them; the two essentials' position is invariant regardless of the user's base-currency choice or device locale.

## 3. In scope / out of scope
- **In scope:**
  - First-launch detection that routes a new user into onboarding and a returning user straight to the recording screen built in feature 001.
  - Choosing the **base currency** from the seeded currency catalog (USD and COP preloaded/enabled/essential, plus the ability to pick another ISO 4217 currency for the base).
  - Creating **one first account** (default suggestion: Cash) with its type, currency, and initial balance entered as integer minor units.
  - Setting the default UI language to **Spanish (`es`)** as the persisted locale; **no in-app language toggle** ships in 002.
  - "Accept defaults and continue" path that lets the user complete onboarding in ≤ 1 minute with minimal taps.
  - Full offline operation: every onboarding write persists locally without network.
  - Persisting an onboarding-complete marker so subsequent app launches bypass onboarding.
- **Out of scope (deferred):**
  - Full account management — multiple accounts, editing/archiving, type switching, balance audit — owned by **E3 / `002-accounts-balances`**.
  - Full currency catalog management — enabling/disabling non-essential currencies, custom currencies, FX provider config — owned by **E8 / `008-multicurrency`**. 002 only consumes the seeded essentials + a one-time base-currency pick.
  - Runtime language switching applied app-wide ≤ 1 s — owned by **E12 / `010-localization`**. 002 only persists the default locale (`es`).
  - Cloud backup, sync, multi-device handoff, account migration.
  - Optional permissions prompts (camera, location) — irrelevant to onboarding since recording (001) needs neither.
  - Edits to the first account after onboarding completes (handled in E3).
  - Bulk account import, CSV/QFX import.

## 4. User stories covered
- **US-1.1** — As a new user, I want to start using the app without signing up, so that I can try it with no friction and with privacy.
- **US-1.2** — As a new user, I want to choose my base currency and create my first account (e.g., Cash), so that amounts and balances make sense from the start.
- **US-1.3** — As a new user, I want a short onboarding, so that I quickly reach recording my first expense.

## 5. Behavioral requirements (platform-neutral)
1. On first launch (no persisted `base_currency` and no onboarding-complete marker), the app MUST present the onboarding flow before any other screen. The flow MUST NOT require sign-up, email, phone, social login, or any network call.
2. Onboarding MUST work fully offline: every step MUST be reachable, completable, and persisted with the device in airplane mode. There MUST be no spinner blocking on network.
3. The currency picker MUST list **USD and COP as preloaded essential options that cannot be removed or disabled**, **pinned at the top of the list** (in that order: USD first, COP second) and visually badged as essential. The picker MUST also let the user pick another ISO 4217 currency as the base via a "choose another" affordance; any non-essential currency added this way appears **below** USD/COP and never displaces them. Pinning is invariant regardless of base-currency choice or device locale.
4. The selected base currency MUST persist as the app's `base_currency` (`AppSetting.base_currency`). Once set, this currency MUST be marked enabled in the catalog if it was not already.
5. Onboarding MUST collect a **first account** with: account type fixed to **cash** (default suggested as Cash; type is not user-selectable on the defaults path), display name resolved from the localized key `account.defaultName.cash` (es: "Efectivo", en: "Cash") — **not** a free-form text field on the defaults path, currency (default = selected base currency), and an initial balance entered as integer minor units honoring the currency's `decimal_digits`. **Default initial balance is `0`** and confirmable without typing. The system MUST allow **exactly one** account to be created during onboarding; multi-account creation, name editing, and type changes are deferred to E3 (`002-accounts-balances`).
6. The first account MUST be persisted via the existing `AccountRepository` port introduced by feature 001 — the same `Account` entity, currency reference, and money model are reused. Onboarding MUST NOT introduce a parallel account or currency entity.
7. The default UI language MUST be **silently persisted as `es`** with no language-choice UI surfaced anywhere in onboarding (no picker, no toggle, no "language" link). Switching languages is deferred to E12 (`010-localization`). UI text in onboarding MUST resolve from localized resources in both `es` and `en` so the screen renders correctly once E12 ships a switch; no user-facing string may be hard-coded.
8. The flow MUST be **skippable with defaults**: a single "Use defaults" or equivalent affordance MUST let the user accept default currency, default account type (Cash), and zero initial balance without typing, and proceed directly to recording.
9. A returning user (onboarding-complete marker present) MUST be taken straight to the recording screen on launch; onboarding MUST NOT be shown again.
10. Onboarding completion MUST be **atomic and recoverable**: the `base_currency` setting, the first account row, the locale setting, and the onboarding-complete marker either ALL persist together, or none of them persist (so a crash mid-onboarding restarts the flow cleanly without partial state).
11. Onboarding MUST NOT block, gate, or delay any core recording behavior beyond the onboarding flow itself. Once the marker is set, recording (per 001) MUST be immediately available offline.
12. Onboarding MUST NOT collect or transmit PII. No telemetry MAY include any of the user's entered values (currency, balance, account name).
13. The system MUST NOT hard-delete any onboarding-created data; if the user reaches recording and later wants to change the first account, that path belongs to E3 (soft-delete + edit rules per Article III apply once they ship there).

## 6. Acceptance criteria (Gherkin)

### First launch with no account
- *Given* the app has never been opened on this device, *When* I open it, *Then* I see the onboarding entry screen — not a sign-up screen, not a login screen, not a network spinner.
- *Given* I am in the onboarding flow, *When* I inspect what is required of me, *Then* I am NOT asked for an email, phone, password, social login, or any external account.

### Offline onboarding
- *Given* the device is in airplane mode on first launch, *When* I complete every onboarding step, *Then* every step works the same as online; my selections persist locally and I reach the recording screen.

### Completing onboarding with defaults (≤ 1-minute happy path)
- *Given* I open the app for the first time, *When* I tap "Use defaults" / "Continue with defaults" without customizing currency, account type, or balance, *Then* the app sets a default base currency, creates a Cash account with zero initial balance, persists `language = es`, marks onboarding complete, and lands on the recording screen in ≤ 60 seconds elapsed real time.

### Choosing COP as base currency
- *Given* I am on the currency-picker step, *When* I select **COP**, *Then* `base_currency = COP` is persisted, the first account's currency defaults to COP, and amounts/totals shown after onboarding express in COP using its `decimal_digits`.

### Choosing USD as base currency
- *Given* I am on the currency-picker step, *When* I select **USD**, *Then* `base_currency = USD` is persisted, the first account's currency defaults to USD, and amounts/totals shown after onboarding express in USD.

### Choosing another ISO 4217 currency
- *Given* I am on the currency-picker step, *When* I select an ISO 4217 currency other than USD/COP (for example EUR), *Then* that currency is enabled in the catalog, `base_currency` is set to it, the first account defaults to it, and USD and COP remain in the catalog as non-removable essentials.

### Essentials are non-removable
- *Given* I am on the currency-picker step, *When* I attempt to disable or remove **USD** or **COP**, *Then* the system MUST NOT allow it; both currencies remain visible and enabled regardless of the base choice.

### Currency list order
- *Given* I am on the currency-picker step, *When* the list renders, *Then* **USD appears in position 1 and COP in position 2**, both visibly badged as essential. Any non-essential ISO 4217 currency the user has added (via "choose another") appears below them and MUST NOT precede or replace them.

### Default account name and type
- *Given* I take the defaults path, *When* onboarding completes, *Then* the first account persists with type `cash` and display name resolved from `account.defaultName.cash` (renders "Efectivo" in `es`, "Cash" in `en`); the user is NOT shown a free-form name field and the type is NOT user-selectable on the defaults path.

### Single account only in onboarding
- *Given* I am in onboarding, *When* I look for a way to create a second account, *Then* no such affordance is present; multi-account creation is reachable only after onboarding completes (in E3).

### No language selector in onboarding
- *Given* I am anywhere inside the onboarding flow, *When* I scan every screen, *Then* there is NO language picker, toggle, or "change language" link; the UI renders in `es` and the persisted locale is `es`.

### Creating the first account
- *Given* I am on the account-creation step with type Cash and currency = base, *When* I enter an initial balance of `123,45` (in a 2-decimal currency) and confirm, *Then* the account persists with `initial_balance_minor = 12345` (integer minor units) honoring `decimal_digits`, and is the only account in the system.

### Initial balance zero
- *Given* I am on the account-creation step, *When* I confirm without entering any balance, *Then* the first account persists with initial balance `0` and onboarding still completes successfully.

### Re-launch skips onboarding
- *Given* I have completed onboarding and closed the app, *When* I reopen it (with or without network), *Then* the recording screen from feature 001 is shown directly and onboarding is NOT shown again.

### Crash mid-onboarding is recoverable
- *Given* the app is force-killed before I tap the final "Continue" / "Done" in onboarding, *When* I reopen the app, *Then* the onboarding flow restarts from a clean state with no partially-written base currency, account, or completion marker.

### Default language
- *Given* I complete onboarding (with or without customizing), *When* I land on the recording screen, *Then* the UI is rendered in Spanish (`es`) by default. No language toggle is presented inside the 002 surface.

## 7. Data touched
Entities and fields read/written (reference `data-model.md` from feature 001 — onboarding reuses these, it does not duplicate them):

- **`AppSetting`** — write: `base_currency` (string, ISO 4217 code), `language` (`es` by default); write the onboarding-complete marker (e.g., `onboarding_completed_at` timestamp or equivalent flag).
- **`Currency`** — read the seeded catalog (USD, COP marked essential/enabled by 001's `CurrencySeed`); if the user picks a non-seeded ISO 4217 currency for the base, write a new row flagged enabled (non-essential).
- **`Account`** — write: exactly one row with `name` (defaults to a localized "Cash" string), `type` (`cash`/etc.), `currency` (= base), `initial_balance_minor` (integer minor units), `created_at`. Reuses the `Account` entity and `AccountRepository` port introduced in 001.

**Invariants honored:**
- Initial balance stored as **integer minor units** honoring `Currency.decimal_digits` (Article III).
- All writes are **transactional**: base currency + account + locale + completion marker land together or not at all (Invariant 10 above).
- USD and COP remain **essential / non-removable** (Article VII).
- No hard delete of onboarding data (Article III).

## 8. Constitution check
- **Article I — Local-first / offline:** every step works in airplane mode; no network call gates onboarding. ✅
- **Article II — Privacy / no account:** no email, phone, social login, or account creation; no PII collected; no telemetry of entered values. ✅
- **Article III — Financial integrity:** initial balance stored as integer minor units; onboarding completion is transactional; no hard deletes. ✅
- **Article IV — Speed:** target ≤ 60 s to reach recording on the defaults path; cold-start budget (≤ 1.5 s) is not regressed; no loading screen mid-flow. ✅
- **Article V — AI:** N/A in 002 (onboarding has no classifier interaction). ✅
- **Article VI — Native + spec source of truth:** this spec is platform-neutral; both platforms MUST render the same flow against the canonical design (`docs/design/Bolsillo.dc.html`, `shared-assets/design/design-system.md`) — visual divergence is fixed in the design system first, then in both apps. ✅
- **Article VII — Localization & currency:** default language persisted as `es`; USD and COP shipped essential and non-removable; other ISO 4217 currencies addable as the base; amounts use each currency's `decimal_digits`. ✅
- **Article VIII — Architecture:** reuses `AccountRepository` / `Currency` ports/entities from 001; no UI-to-DB shortcuts; no parallel domain types introduced. ✅
- **Article IX — Tests / no data loss:** atomic onboarding completion (no partial state on crash); migration impact is nil (002 introduces no schema change beyond optionally adding an `AppSetting` row, which MUST be additive and tested). ✅

## 9. Localization & currency notes
- All user-facing strings live under dotted keys (e.g., `onboarding.welcome.title`, `onboarding.currency.title`, `onboarding.currency.essential.badge`, `onboarding.account.initialBalance.label`, `onboarding.cta.useDefaults`, `onboarding.cta.continue`) and MUST resolve in both `es` and `en`. Both renderings MUST be verified — no truncation, no hard-coded text.
- The default account's display name MUST resolve through the localized key **`account.defaultName.cash`** (es: "Efectivo", en: "Cash") — never stored as a hard-coded literal — so the same row reads correctly when the user later switches language in E12.
- The currency picker MUST render USD at position 1 and COP at position 2, both badged via the localized `onboarding.currency.essential.badge` key. Position is invariant across locales and base-currency choice.
- Number, currency, and date formats follow the active locale and the selected currency's `decimal_digits` (USD/COP both = 2; other ISO 4217 codes use their own).
- The currency picker MUST visually mark USD and COP as **essential** and disable any "remove" affordance on them.

## 10. Non-functional targets
- **Time-to-recording (defaults path):** ≤ 60 s wall-clock from cold launch on the reference low-end device.
- **Cold start:** Article IV's ≤ 1.5 s budget MUST hold for the onboarding entry screen as well.
- **Save / persistence:** writing the onboarding-complete bundle (base currency + first account + locale + marker) MUST feel instant — no loading screen, no spinner — and complete within Article IV's ≤ 100 ms transactional-write budget on the reference device.
- **Offline guarantee:** 100% functional in airplane mode end-to-end.
- **Accessibility:** all steps MUST support VoiceOver/TalkBack, Dynamic Type / large-text scaling, sufficient color contrast, and minimum hit-target sizes per platform guidelines.
- **Visual parity:** Android and iOS onboarding screens MUST consume the shared design tokens (`shared-assets/design/tokens.json`) and replicate the canonical design (`docs/design/Bolsillo.dc.html`, `shared-assets/design/design-system.md`).

## 11. Open questions
1. **Default base currency on the "Use defaults" path** — When the user takes the skip-with-defaults path without picking a currency, which essential currency is selected: USD or COP? **Assumption (see §12):** derive from the device region — `CO` → COP, otherwise USD; both remain switchable on the picker. Re-run `/speckit-clarify` to lock a fixed default if locale-derived selection is rejected.
2. **First-launch detection signal** — Is the onboarding-shown decision driven by *absence of an `AppSetting.base_currency` row*, by a dedicated *`onboarding_completed_at` marker*, or both (belt-and-suspenders)? **Assumption (see §12):** dedicated completion marker, written in the same transaction as the other onboarding writes.

## 12. Assumptions (informed defaults; replace with `/speckit-clarify` answers if challenged)
- **Default base currency** when the user accepts defaults without picking: derived from the device region — Colombia (`CO`) → COP; everywhere else → USD. Both remain switchable in the picker without leaving the defaults path.
- **First-launch detection**: a dedicated onboarding-complete marker (e.g., `AppSetting.onboarding_completed_at`) — written atomically with the base currency, the first account, and the locale — is the single source of truth. Absence of this marker triggers onboarding regardless of any other state.
- **Onboarding steps**: a short linear flow (welcome → currency → first account → done) collapsible into a single "Use defaults" CTA from the welcome screen.
- **Currency picker scope in 002**: shows USD + COP (essential, pinned top) and a single-step "Choose another currency" affordance to pick one additional ISO 4217 code for the base. Full enable/disable management of the broader catalog is deferred to E8.
- **Persistence channel**: reuses feature 001's local encrypted store (Article I). No new database, no separate keystore.
- **Telemetry**: anonymous opt-in only, and onboarding-entered values (currency choice, balance, account name) MUST NEVER be included.

> Locked decisions (see `## Clarifications · Session 2026-06-13`): default account type = `cash`, name key = `account.defaultName.cash` (es "Efectivo" / en "Cash"), default initial balance = `0`, exactly one account in onboarding, no language picker, USD+COP pinned at top of currency list.

## 13. Definition of Done (per platform)
- All acceptance criteria pass in both `es` and `en` rendering.
- Works end-to-end offline (airplane mode).
- No data loss: crash mid-flow leaves zero partial state; atomic completion verified by test.
- Tests cover: first-launch routing, defaults path, COP/USD/other-ISO-4217 selection, USD/COP non-removable, atomic completion, re-launch skips onboarding, integer-minor-unit balance storage per `decimal_digits`, locale persisted as `es`.
- Accessibility checks pass (VoiceOver/TalkBack, Dynamic Type/scaling, contrast, hit targets).
- Performance targets met: ≤ 60 s time-to-recording on defaults path, no loading screen, transactional write ≤ 100 ms.
- Visual parity with canonical design (`docs/design/Bolsillo.dc.html`, `shared-assets/design/design-system.md`); both apps consume shared tokens.
- No hard-coded user-facing strings; both `es` and `en` resource sets ship with all onboarding keys.
- USD and COP verified essential and non-removable.
- Reuses 001's `Account`, `Currency`, `AccountRepository` — no parallel entities introduced.
