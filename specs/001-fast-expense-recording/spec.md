# Feature Spec — `specs/001-fast-expense-recording/spec.md`

> Platform-neutral. Describes the **WHAT** and **WHY**, never the HOW. Must comply with `.specify/memory/constitution.md`.

## 1. Feature
`001` · **Name:** Fast expense recording · **Source user stories:** US-2.1, US-2.2, US-2.7 (from `docs/USER_STORIES.md`) · **Epic:** E2

## 2. Problem / why
Manual-entry friction is the leading cause of abandonment in expense apps: if recording is slow or fiddly, users drop the habit and the data goes stale. Recording must feel near-instant and work everywhere (including with no signal), so capturing a movement is faster than deciding not to.

## Clarifications

### Session 2026-06-13
- Q: Are cross-currency transfers (different-currency source/destination) in scope for 001? → A: Out of scope for 001 — transfers assume same-currency accounts; cross-currency transfers deferred to E8.
- Q: When multiple accounts exist and the `ExpenseClassifier` returns no account suggestion, which account is pre-filled? → A: Last-used account (on very first use, the single onboarding account).
- Q: How long does the post-save undo affordance remain available? → A: ~5 s dismissable affordance, or until the user's next action — whichever comes first.
- Q: Is recording an expense that pushes an account balance negative allowed? → A: Allow, never block — the balance may go negative; saving is never blocked.

### Session 2026-06-13 (post-analysis)
- Q: How is the "≤ 3 taps" metric counted, given a multi-digit amount needs several keypad presses? → A: A "tap" is a discrete decision/confirmation action, NOT each digit. Entering the amount on the keypad counts as **one** interaction regardless of digit count; a pre-filled category and account cost **0** taps; **Save** is **1** tap. The golden path is therefore amount-entry → Save (2 taps), leaving headroom for at most one correction (category/account) within the ≤ 3 budget.
- Q: What is the launch presentation shell for 001 — full-screen record surface or the mock's Home → FAB → bottom sheet? → A: For 001 the **record surface is the launch destination, presented full-screen**, built from the same record components as the mock's sheet. Home, FAB, and bottom-sheet presentation are deferred to later navigation work (E3). Both platforms MUST match this.
- Q: Where does a category's icon/color come from, given the taxonomy ids differ from the design palette ids? → A: A transaction's `categoryId` is a **taxonomy** id (`shared-assets/taxonomy/category-taxonomy.json`); its color resolves through that node's **`colorToken`** into the design palette (`shared-assets/design/tokens.json → category`). Both platforms MUST resolve color this way, not by guessing from the taxonomy id.

## 3. In scope / out of scope
- **In scope:**
  - Open directly into amount entry with focus on the amount field. The recording surface is the **launch destination, presented full-screen** (same components as the mock's record sheet); Home/FAB/bottom-sheet navigation is deferred to E3 (see Clarifications, post-analysis session).
  - Pre-suggest category and account via the `ExpenseClassifier` port; fall back to last-used when the port is unavailable or returns nothing.
  - Record an **expense** in ≤ 3 taps and ≤ 5 s, fully offline.
  - Record **income** (adds to balance, marked `income`).
  - Record a **transfer** between two **same-currency** accounts as a linked double entry, excluded from expense/income totals.
  - Immediate balance update after save; one-tap **undo** of the just-saved transaction.
  - **Edit** a transaction with correct balance recomputation.
  - **Soft delete** to trash with **restore**; no hard deletes.
  - All money handled as integer minor units; balances always reconcile.
- **Out of scope (deferred):**
  - Natural-language entry (US-2.3) and receipt OCR (US-2.4).
  - AI model **training/personalization** internals (E4) — this feature only *consumes* a suggestion through the `ExpenseClassifier` port; corrections logging is out of scope here.
  - Category creation/editing UI (US-4.4), budgets (E5), recurring (E6), reports UI (E7), multi-currency entry of a non-account currency (E8) beyond rolling totals to base.
  - **Cross-currency transfers** (source and destination in different currencies) — deferred to E8; `001` assumes both transfer legs share the same currency.
  - Templates/favorites (US-2.5), widgets/quick actions (US-2.6).
  - Trash management UI (bulk purge, auto-purge policy) beyond single-item restore.
  - **Runtime language-switch UI** (in-app es↔en toggle, applied app-wide ≤ 1 s — Article IV/IX) is deferred to settings/E3. 001 covers **es and en rendering** of this screen (both verified, no truncation/hard-coding); the language-switch *flow test* belongs to the feature that ships the toggle.

## 4. User stories covered
- **US-2.1** — As a user, I want to record an expense quickly, so that I don't drop the habit out of laziness.
- **US-2.2** — As a user, I want to record income and transfers between accounts, so that I reflect all my movements.
- **US-2.7** — As a user, I want to correct or delete a transaction, so that I keep my data accurate.

## 5. Behavioral requirements (platform-neutral)
1. On launch, the app MUST present amount entry with input focus on the amount, ready to receive digits without an extra tap.
2. The app MUST NOT show a loading screen during recording.
3. The system MUST request a category and account suggestion from the `ExpenseClassifier` port and pre-fill both before the user taps save.
4. If the `ExpenseClassifier` port is unavailable, errors, or returns no suggestion, the system MUST fall back to the **last-used** category and account, and MUST still allow saving. On very first use (no prior transaction), the account defaults to the single onboarding account. The AI MUST NEVER block or delay saving.
5. Saving an **expense** MUST be reachable in ≤ 3 taps and ≤ 5 s from launch and MUST complete fully offline. A "tap" counts a discrete decision/confirmation action: amount entry on the keypad is **one** interaction (not one per digit), a pre-filled category/account costs **0** taps, and Save is **1** tap (see Clarifications, post-analysis session). UI tests assert the tap budget using this definition.
6. After any save, the affected account balance(s) MUST update immediately and an **undo** affordance MUST be presented for the just-saved transaction. The affordance MUST remain available for ~5 s or until the user's next action, whichever comes first.
7. Tapping **undo** MUST remove the just-saved transaction and revert affected balance(s) to their prior values.
8. Recording **income** MUST add to the account balance and mark the transaction as `income`.
9. Recording a **transfer** MUST create exactly two linked entries sharing a `transfer_group_id` — one negative at the source account, one positive at the destination — and MUST NOT count as expense or income in any total.
10. The source and destination accounts of a transfer MUST be different; a transfer MUST NOT be saved with the same account on both sides. In `001`, both accounts MUST share the same currency (cross-currency transfers are deferred to E8).
11. **Editing** a transaction MUST recompute all affected account balances correctly, including when the account, amount, or type changes; editing one leg of a transfer MUST keep the linked pair consistent so balances still reconcile.
12. **Deleting** a transaction MUST be a soft delete (set `deleted_at`, move to trash); it MUST NOT hard-delete data. Deleting one leg of a transfer MUST remove both legs from active balances together.
13. A soft-deleted transaction MUST be **restorable** from trash, with balances restored to reflect its reappearance.
14. All monetary amounts MUST be stored and computed as integer minor units honoring the currency's `decimal_digits`; `float`/`double` MUST NOT be used for money, including intermediate sums. Any rounding (e.g. future FX/base conversion) MUST use **banker's half-up**, defined once and tested (Article III); same-currency 001 entries require no rounding (identity), but the policy and its test harness exist from day one.
15. After every record / edit / undo / delete / restore operation, account balances and rolled-up totals MUST reconcile (sum of non-deleted entries equals stored balance), with no half-written state.
16. Every save, undo, edit, and delete MUST be transactional: it either fully applies or has no effect.
17. Amounts are entered in the selected account's currency; totals shown across accounts MUST roll up to the configured base currency while preserving each transaction's original amount. In 001 every transaction MUST still store a frozen `fx_rate` and `amount_base_minor` at creation (= 1.000 / = `amount_minor` for same-currency entries), so the base-rollup data exists from day one; cross-account base-currency **aggregation UI** is latent in 001 (first launch seeds a single account) and the multi-account rollup view is exercised from E8.
18. All user-facing strings in this feature MUST resolve from localized resources (es default, en switchable); none may be hard-coded.
19. Saving an expense that would drive an account balance negative MUST be allowed; the system MUST NOT block or warn-to-block on overdraft (balances may go negative).

## 6. Acceptance criteria (Gherkin)

### Happy path — record expense
- *Given* the app is opened, *When* it loads, *Then* the amount keypad is shown with input focus on the amount and no loading screen.
- *Given* I have typed an amount and a category and account are pre-filled by the suggestion (or last-used fallback), *When* I tap **Save**, *Then* the expense is stored offline, the account balance updates immediately, and an **undo** affordance appears — achieved in ≤ 3 taps and ≤ 5 s.

### Undo
- *Given* I have just saved a transaction and the undo affordance is visible, *When* I tap **undo**, *Then* the transaction is removed and the affected balance reverts to its previous value.

### Income
- *Given* I choose to record income, *When* I save it, *Then* the amount is added to the account balance and the transaction is marked `income`.

### Transfer
- *Given* two distinct accounts, *When* I record a transfer between them, *Then* two linked entries are created (one negative at source, one positive at destination) sharing a `transfer_group_id`, both balances update, and reports exclude the transfer from expense and income totals.
- *Given* a transfer form with the same account selected as source and destination, *When* I attempt to save, *Then* saving is prevented and I am told source and destination must differ.
- *Given* a transfer between two same-currency accounts, *When* I save it, *Then* both legs reconcile; cross-currency transfer entry is not offered in `001`.

### Overdraft
- *Given* an account balance lower than the expense amount, *When* I save the expense, *Then* the save succeeds without block or warning and the balance goes negative.

### Edit
- *Given* an existing transaction, *When* I edit its amount, *Then* the affected account balance recomputes correctly.
- *Given* an existing transaction, *When* I change its account, *Then* the old account's balance is restored and the new account's balance is adjusted, so both reconcile.
- *Given* one leg of a transfer, *When* I edit its amount, *Then* the linked pair stays consistent and both account balances still reconcile.

### Delete & restore
- *Given* a transaction, *When* I delete it, *Then* it moves to trash (soft delete), is excluded from active balances, and remains recoverable.
- *Given* a soft-deleted transaction in trash, *When* I restore it, *Then* it reappears in the account and the balance reflects it again.
- *Given* one leg of a transfer, *When* I delete it, *Then* both legs leave active balances together and both can be restored together.

### Offline & AI fallback
- *Given* there is no network, *When* I record, edit, undo, delete, or restore, *Then* every operation works exactly the same (100% on-device).
- *Given* the `ExpenseClassifier` port is unavailable or returns no suggestion, *When* I open the recording screen, *Then* category and account default to last-used and I can still save without delay.

### Integrity
- *Given* any sequence of record/edit/undo/delete/restore operations, *When* balances are recomputed, *Then* the sum of non-deleted entries per account equals that account's stored balance (reconciliation holds).

## 7. Data touched
- **`Transaction`** — create / update / soft-delete / restore. Fields used (conceptual): id, type (`expense` | `income` | `transfer`), `amount_minor`, currency, account ref, category ref, `transfer_group_id` (for transfer legs), timestamp, `deleted_at`, plus frozen `fx_rate` / `amount_base_minor` when the account currency differs from base.
- **`Account`** — read + balance recompute on every write.
- **`Category`** — read (for pre-fill / display).
- **`ExpenseClassifier` port** — read a category/account suggestion only.

**Invariants:** integer minor units only; transfer = linked negative/positive pair sharing `transfer_group_id`; soft delete only (`deleted_at`); `fx_rate` and `amount_base_minor` computed once and frozen; balances reconcile after every operation.

## 8. Constitution check
- **Article I — Local-first / offline:** ✔ All recording, editing, undo, delete, restore, and balance updates work with no network; on-device DB is the source of truth (FR 4, 5, offline AC).
- **Article II — Privacy:** ✔ No account or network required; the `ExpenseClassifier` suggestion is on-device; no financial data leaves the device.
- **Article III — Financial integrity:** ✔ Integer minor units, no floats (FR 14); transfers are a linked double entry excluded from totals (FR 9); soft delete only (FR 12); transactional, reconciling writes (FR 15, 16); FX frozen at creation (Data touched).
- **Article IV — Speed:** ✔ ≤ 3 taps / ≤ 5 s, focus-on-amount at launch, no loading screen (FR 1, 2, 5); save ≤ 100 ms (§10).
- **Article V — AI suggests, never blocks:** ✔ Suggestion is pre-fill only with last-used fallback and never blocks or delays saving (FR 3, 4). Correction logging / personalization is out of scope here (deferred to E4).
- **Article VI — Native per platform, spec is source of truth:** ✔ This spec is platform-neutral; any divergence is fixed here first.
- **Article VII — Localization & currency:** ✔ es default + en, no hard-coded strings (FR 18); amounts in account currency, totals roll up to base preserving originals (FR 17).
- **Article VIII — Clean architecture:** ✔ AI accessed only via the `ExpenseClassifier` port; spec references ports, not storage/UI tech.
- **Article IX — Tests / no data loss:** ✔ DoD requires money-math, transfer-integrity, reconciliation, and 3-tap flow tests in es+en; soft delete guarantees no data loss.
- **Tensions / flags:** None outstanding. Multi-currency *entry* in a non-account currency is deferred (E8); here amounts use the account's currency and totals roll up to base.

## 9. Localization & currency notes
- **String keys (es default + en), authoritative against `shared-assets/i18n/{es,en}.json`:** `record.title`, `record.amount`, `record.amount.placeholder`, `record.save`, `record.undo`, `record.income`, `record.expense`, `record.transfer`, `record.transfer.source`, `record.transfer.destination`, `record.transfer.sameAccountError`, `record.category`, `record.account`, `record.edit`, `record.delete`, `record.restore`, `record.deleted.toast`, `record.saved.toast`, `record.keypad.hint`, `record.toConfirm`, `record.ai.waiting`, `record.ai.toConfirm`, `record.ai.confident`.
  - **Two distinct "to confirm" keys** (both render "Por confirmar" in es): `record.toConfirm` labels the **transaction-row** badge (design §2.2); `record.ai.toConfirm` labels the **AI-confidence badge** below-threshold state (design §2.3). They are separate keys by design context, not a duplicate to merge.
- Amounts are entered in the selected account's currency and formatted per that currency's `decimal_digits` and the active locale.
- Cross-account totals roll up to the configured base currency while each transaction keeps and displays its original amount.

### 9.1 UI reference (binding visual contract)
- `docs/design/Bolsillo.dc.html` (canonical pixels) together with `shared-assets/design/design-system.md` and `shared-assets/design/tokens.json` are the **binding visual reference** for this feature on **both** platforms (Android, iOS).
- Both apps MUST replicate the canonical design and consume the **same design tokens** (color, typography, spacing, radius, elevation, category colors, confidence states) and the **same component behavior** (record sheet, amount keypad, type segmented control, category/account chips, AI confidence states, save button, undo toast). No literal colors/sizes in UI code — values resolve from the shared tokens.
- The mock ships light only; dark values are a derived proposal until a dark mockup exists. Confidence threshold keys off `ExpenseClassifier.DEFAULT_THRESHOLD` (0.75), not a hard-coded number.
- **Visual divergence is a design-system defect**: fix it in `shared-assets/design/` (and `docs/design/`) first, then in both implementations — per the constitution's visual-parity rule (Article VI).
- **Strings come from shared i18n**: every `record.*` key in this section is defined in `shared-assets/i18n/{es,en}.json` (the shared source of truth, Article VI/VII). Each platform generates its native string resources from those files — `es.json`→`values/strings.xml` (`record_save`) / iOS `Localizable.xcstrings` (`record.save`); keys are NOT hand-authored per platform.
- **Category icon/color** resolves `categoryId` (a taxonomy id) → `colorToken` in `shared-assets/taxonomy/category-taxonomy.json` → the design palette in `shared-assets/design/tokens.json → category`. Both platforms MUST use this resolution path.

## 10. Non-functional targets
- **Save a transaction ≤ 100 ms** (perceived; offline).
- **Cold start ≤ 1.5 s** to a usable amount keypad.
- **Recording in ≤ 3 taps and ≤ 5 s** (tap = a discrete decision action per FR 5; digit presses excluded); no loading screen during recording.
- **Suggestion pre-fill ≤ 200 ms** and MUST NOT block saving (if slower/unavailable, last-used is used).
- **100% offline** for every operation in this feature.
- **Accessibility:** keypad and actions usable with screen reader and large text; adequate touch targets and contrast.
- **Reconciliation:** balances provably consistent after any operation sequence.

## 11. Open questions
All prior open questions resolved in **Clarifications · Session 2026-06-13**:
- Default account when multiple exist & no suggestion → last-used (onboarding account on first use). See FR 4.
- Undo window → ~5 s or until next action, whichever first. See FR 6.
- Cross-currency transfers → out of scope for `001`; deferred to E8. See FR 10 and §3.
- Overdraft on expense → allowed, never blocked. See FR 19.

No open questions remain.

## 12. Definition of Done (per platform)
- All acceptance criteria pass on the platform.
- Works fully offline; no data loss (soft delete only).
- Unit tests for money arithmetic, transfer linked-pair integrity, and balance reconciliation after record/edit/undo/delete/restore.
- UI test for the 3-tap recording flow.
- Localized in **es** and **en** with no hard-coded or truncated strings.
- Performance targets met on the reference low-end device (save ≤ 100 ms, cold start ≤ 1.5 s, ≤ 3 taps / ≤ 5 s).
- Basic accessibility verified on the recording screen.
