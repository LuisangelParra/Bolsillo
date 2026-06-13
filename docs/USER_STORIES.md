# User Stories — MVP “Bolsillo”

> **Product:** Local-first personal finance app with on-device AI classification
> **Base documents:** PRD “Bolsillo” v1.1 + TRD v1.1 · **Scope:** MVP only (v1.0)
> **Audience:** Product, Design, Development, QA.
> **v1.1 note:** adds stories for language (Spanish default, switchable to English) and currencies (USD and COP essential, others addable).

---

## 1. Conventions

- **Format:** *As a* `<role>`, *I want* `<action>`, *so that* `<benefit>`.
- **Acceptance criteria (AC):** Gherkin style — *Given / When / Then*.
- **Priority (MoSCoW):** **M** = Must · **S** = Should · **C** = Could (everything in the MVP is M/S; the C items are “if time allows”).
- **Estimate:** story points (1, 2, 3, 5, 8) — relative scale, not hours.
- **Roles:** *User* (anyone), *New user*, *Power user*.

### Definition of Ready
The story has clear AC, associated design (if applicable), identified dependencies, and is estimable and testable.

### Definition of Done
Code reviewed and merged · AC verified · relevant unit/UI tests · **works offline** · no data loss · meets applicable performance targets · basic accessibility · **localized in es and en**.

---

## 2. MVP Epics

| ID | Epic | Goal |
|---|---|---|
| **E1** | Onboarding and setup | Start recording in under 1 minute |
| **E2** | Fast transaction recording | Record an expense in ≤ 5 s / ≤ 3 taps |
| **E3** | Accounts and balances | Know how much I have and where |
| **E4** | Local AI categorization | Classify by itself and learn from me, on-device |
| **E5** | Budgets | Control how much I can spend |
| **E6** | Recurring and subscriptions | Don't forget repeating expenses |
| **E7** | Reports and insights | Understand where my money goes |
| **E8** | Multi-currency | Handle several currencies with historical fidelity |
| **E9** | Data: backup, export, import | Own my data and not lose it |
| **E10** | Security and privacy | Protect my information |
| **E11** | Search and management | Find and correct transactions |
| **E12** | Localization (language) | Use the app in my preferred language |

---

## E1 · Onboarding and setup

### US-1.1 — Use the app without an account · **M · 2 pts**
*As a new user, I want to start using the app without signing up, so that I can try it with no friction and with privacy.*
**AC**
- *Given* I open the app for the first time, *When* I complete the minimal onboarding, *Then* I can record transactions without creating any account or giving an email.
- *Given* there is no network, *When* I open the app, *Then* onboarding works the same (100% offline).

### US-1.2 — Set base currency and first account · **M · 3 pts**
*As a new user, I want to choose my base currency and create my first account (e.g., Cash), so that amounts and balances make sense from the start.*
**AC**
- *Given* onboarding, *When* I choose a base currency and initial balance, *Then* the account is created with that balance.
- *Then* **USD and COP are available by default** in the currency picker (essential, preloaded), with the option to enable others.
- *Then* the base currency is saved as `base_currency` and totals are expressed in it.

### US-1.3 — Onboarding in under 1 minute · **S · 2 pts**
*As a new user, I want a short onboarding, so that I quickly reach recording my first expense.*
**AC**
- *Given* a user following the default flow, *When* they proceed without customizing, *Then* they reach the recording screen in ≤ 1 min with sensible defaults (including **Spanish as the default language**).

---

## E2 · Fast transaction recording (core)

### US-2.1 — Record an expense in 3 taps · **M · 5 pts**
*As a user, I want to record an expense quickly, so that I don't drop the habit out of laziness.*
**AC**
- *Given* I open the app, *Then* the opening screen is the numeric keypad with the cursor on the amount.
- *Given* I type an amount and the AI already pre-suggests category and account, *When* I tap “Save”, *Then* the expense is recorded (goal: ≤ 3 taps, ≤ 5 s).
- *Then* the account balance updates immediately and everything happens without network.
- *Then* I can undo the save with one tap.

### US-2.2 — Record income and transfer · **M · 3 pts**
*As a user, I want to record income and transfers between accounts, so that I reflect all my movements.*
**AC**
- *When* I record income, *Then* it adds to the balance and is marked as `income`.
- *When* I record a transfer between two accounts, *Then* two linked entries are created and it does **not** count as expense or income in reports.

### US-2.3 — Record with natural language · **S · 5 pts**
*As a user, I want to type “coffee 8k”, so that I record without navigating menus.*
**AC**
- *Given* the text “coffee 8k”, *When* I confirm, *Then* it is interpreted as amount = 8,000 and suggested category = Coffee shop.
- *Then* the parser handles both Spanish and English number/keyword expressions.
- *Given* ambiguous text, *Then* the app asks to confirm the doubtful field without blocking.

### US-2.4 — Record from receipt photo (OCR) · **S · 5 pts**
*As a user, I want to photograph a receipt, so that I capture amount, date, and merchant without typing.*
**AC**
- *When* I take/choose a photo, *Then* on-device OCR extracts amount, date, and merchant and pre-fills the form.
- *Then* the AI proposes a category from the detected merchant.
- *Given* there is no network, *Then* OCR works the same (on-device).

### US-2.5 — Favorites and templates · **S · 3 pts**
*As a user, I want to repeat frequent expenses (e.g., “daily coffee”) with one tap, so that I save time.*
**AC**
- *Given* I save an expense as a template, *When* I tap it, *Then* a new identical transaction is pre-filled ready to save.

### US-2.6 — System shortcuts (widget / quick action) · **C · 3 pts**
*As a user, I want to record from a widget or shortcut, so that I do it without opening the full app.*
**AC**
- *When* I use the widget/shortcut, *Then* I land directly on the amount screen.

### US-2.7 — Edit and delete transactions (with trash) · **M · 3 pts**
*As a user, I want to correct or delete a transaction, so that I keep my data accurate.*
**AC**
- *When* I edit a transaction, *Then* the balance recomputes correctly.
- *When* I delete a transaction, *Then* it goes to the trash (soft delete) and I can restore it.

---

## E3 · Accounts and balances

### US-3.1 — Create and manage accounts · **M · 3 pts**
*As a user, I want to create accounts (cash, debit, credit, bank, savings, wallet), so that I organize my money.*
**AC**
- *When* I create an account with type, currency, initial balance, icon, and color, *Then* it appears in the list with its balance.
- *When* I archive an account, *Then* it stops appearing in selection but its history is kept.

### US-3.2 — See balance in real time · **M · 2 pts**
*As a user, I want to see the updated balance of each account and the total, so that I know how much I have.*
**AC**
- *When* I record/edit/delete a transaction, *Then* the account balance and the total update instantly.
- *Then* the total is shown in the base currency.

### US-3.3 — Credit card with statement and payment · **C · 3 pts**
*As a user, I want to record my card's statement and payment dates, so that I anticipate the payment.*
**AC**
- *Given* a credit-type account with a statement/payment day, *Then* the app shows the amount due and a reminder.

---

## E4 · Local AI categorization

### US-4.1 — Automatic category suggestion · **M · 5 pts**
*As a user, I want the app to propose the category by itself, so that I don't classify each expense manually.*
**AC**
- *When* I enter an expense, *Then* the on-device AI proposes a category in ≤ 200 ms.
- *Given* `confidence ≥ threshold` (default 0.75), *Then* it is applied automatically with an undo option.
- *Given* `confidence < threshold`, *Then* the expense is flagged “to confirm” and 3 suggested categories are shown.
- *Given* there is no network, *Then* the suggestion works the same (all on-device).

### US-4.2 — Learn from my corrections · **M · 5 pts**
*As a user, I want the app to learn when I correct a category, so that it gets more accurate for me over time.*
**AC**
- *When* I correct a suggested category, *Then* it is saved as **on-device** learning and improves future suggestions for the same merchant/pattern.
- *Then* no learning data leaves the device.
- *Given* ~50–100 corrections, *Then* the accuracy rises noticeably (measured locally).

### US-4.3 — User categorization rules · **S · 3 pts**
*As a power user, I want to create rules (“if merchant contains UBER → Transport”), so that I force classifications.*
**AC**
- *When* I create a rule, *Then* it applies with priority over the model and with full confidence.

### US-4.4 — Customizable categories · **M · 3 pts**
*As a user, I want to create/edit categories and subcategories with icon and color, so that I adapt them to my life.*
**AC**
- *When* I create or edit a category, *Then* it becomes available for classifying and for the AI.
- *Then* a default set of categories exists at the start (localized in es/en).

### US-4.5 — Bulk reclassification · **S · 3 pts**
*As a user, I want to select several transactions and change their category at once, so that I correct quickly.*
**AC**
- *When* I select several and choose a new category, *Then* they all update and the AI learns from the change.

---

## E5 · Budgets

### US-5.1 — Create a budget per category · **M · 3 pts**
*As a user, I want to set a monthly budget per category, so that I control my spending.*
**AC**
- *When* I define an amount and period for a category, *Then* the app shows spent vs available.

### US-5.2 — Spending pace (alerts) · **S · 5 pts**
*As a user, I want to be warned if I'm spending too fast, so that I don't overshoot before month's end.*
**AC**
- *Given* an active budget, *When* my spending is ahead of pace given the days remaining, *Then* I get a “pace” alert.
- *Then* I get configurable alerts at 80%, 100%, and overspend.

### US-5.3 — “Available to spend” · **S · 3 pts**
*As a user, I want to see how much I have free after what's budgeted, so that I decide clearly.*
**AC**
- *Then* the app shows a clear “available” number in everyday language.

---

## E6 · Recurring and subscriptions

### US-6.1 — Mark a transaction as recurring · **M · 3 pts**
*As a user, I want to mark an expense as recurring (frequency and next date), so that I don't forget it.*
**AC**
- *When* I define a recurrence, *Then* the app schedules it and shows it in upcoming transactions.

### US-6.2 — Generate the next transaction · **S · 3 pts**
*As a user, I want the recurring item created automatically or with one tap, so that I keep the record up to date.*
**AC**
- *Given* `auto_create` enabled, *When* the date arrives, *Then* the transaction is created automatically.
- *Given* `auto_create` disabled, *Then* I get a reminder to confirm it.

### US-6.3 — See subscriptions and their total cost · **S · 2 pts**
*As a user, I want to see my active subscriptions and how much they total per month/year, so that I decide which to cut.*
**AC**
- *Then* the app lists subscription-type recurrences with their aggregated monthly and annual cost.

---

## E7 · Reports and insights

### US-7.1 — Spending by category and period · **M · 3 pts**
*As a user, I want to see which categories and periods I spend in, so that I understand my habits.*
**AC**
- *When* I open reports, *Then* I see spending by category (donut/bar chart) for the chosen period.
- *Then* I can filter by account, category, tag, and date range.

### US-7.2 — Monthly comparison and trend · **S · 3 pts**
*As a user, I want to compare months and see the trend, so that I know if I'm improving.*
**AC**
- *Then* I see a line/series of spending by month and the change vs the previous month.

### US-7.3 — Weekly recap · **C · 2 pts**
*As a user, I want a weekly recap of my spending, so that I stay on top of it.*
**AC**
- *Then* the app generates a weekly recap **on-device** with the key totals.

---

## E8 · Multi-currency

### US-8.1 — Transactions in another currency · **M · 5 pts**
*As a user, I want to record expenses in currencies other than the base, so that I handle travel or income in another currency.*
**AC**
- *When* I record in another currency, *Then* it stores the **original amount** and its currency, plus `fx_rate` and `amount_base_minor` **frozen** at that moment.
- *Then* changing future rates does **not** alter past transactions.

### US-8.2 — Unified view in base currency · **S · 3 pts**
*As a user, I want to see totals in my base currency without losing the amount actually paid, so that I have context.*
**AC**
- *Then* totals are shown converted to the base currency, but each transaction keeps and shows its original amount.

### US-8.3 — Update rates (manual / optional online) · **C · 2 pts**
*As a user, I want to update rates manually or download them when online, so that I have up-to-date conversions.*
**AC**
- *When* I edit a rate or download it (with network), *Then* it is used for **new** transactions, not retroactively.

### US-8.4 — Manage available currencies (USD & COP + others) · **M · 3 pts**
*As a user, I want USD and COP available out of the box and the ability to add other currencies, so that I cover my essential cases and can grow.*
**AC**
- *Given* a fresh install, *Then* **USD and COP are preloaded, enabled, and flagged essential** (cannot be removed).
- *When* I open currency settings, *Then* I can **enable additional ISO 4217 currencies** and **set any enabled currency as the base currency**.
- *Then* amounts format according to each currency's decimal rules and the active locale.

---

## E9 · Data: backup, export, and import

### US-9.1 — Encrypted backup · **M · 5 pts**
*As a user, I want to back up my data encrypted to my own storage, so that I don't lose it when changing phones.*
**AC**
- *When* I create a backup, *Then* an **encrypted** file is generated at the destination I choose (local / the user's own iCloud-Drive).
- *Then* no data is sent to the product's servers by default.

### US-9.2 — Restore from backup · **M · 3 pts**
*As a user, I want to restore a backup, so that I recover everything on a new device.*
**AC**
- *When* I select a backup and provide the key, *Then* accounts, transactions, categories, and settings are restored intact.

### US-9.3 — Export / import CSV · **S · 3 pts**
*As a user, I want to export to CSV and import statements/other apps, so that I own my data and migrate without friction.*
**AC**
- *When* I export, *Then* I get a CSV with transactions, accounts, and categories.
- *When* I import a supported CSV, *Then* transactions are loaded and the AI proposes categories.

---

## E10 · Security and privacy

### US-10.1 — Biometric/PIN lock · **M · 3 pts**
*As a user, I want to lock the app with fingerprint/face or PIN, so that I protect my information.*
**AC**
- *When* I enable the lock, *Then* the app asks for authentication on open and after auto-lock.
- *Then* the database is encrypted at rest.

### US-10.2 — Permission transparency · **S · 2 pts**
*As a user, I want to understand why camera/location are requested, so that I grant permissions with confidence.*
**AC**
- *When* a permission is requested, *Then* its use is explained and that it is processed on-device; the app works even if denied.

---

## E11 · Search and management

### US-11.1 — Search and filter transactions · **M · 3 pts**
*As a user, I want to search by text, amount, date, category, tag, or account, so that I find something quickly.*
**AC**
- *When* I apply filters/search, *Then* the list updates instantly (offline).

### US-11.2 — Free-form tags · **C · 2 pts**
*As a user, I want to add tags in addition to categories, so that I group flexibly (e.g., “trip 2026”).*
**AC**
- *When* I tag transactions, *Then* I can filter them and see them in reports by tag.

---

## E12 · Localization (language)

### US-12.1 — Default language Spanish, switchable to English · **M · 3 pts**
*As a user, I want the app to be in Spanish by default and to switch it to English, so that I use it in my preferred language.*
**AC**
- *Given* a fresh install, *Then* the UI is in **Spanish (`es`) by default**.
- *Given* settings, *When* I switch the language to **English (`en`)**, *Then* the entire UI (labels, default categories, dates, numbers) updates app-wide, ideally without restart.
- *Then* switching language does not alter any financial data, only its presentation.
- *Then* there are no hard-coded/untranslated strings in either language.

### US-12.2 — Locale-aware formats · **S · 2 pts**
*As a user, I want dates, numbers, and currency to display in my language/region format, so that everything reads naturally.*
**AC**
- *Given* the selected language, *Then* date, number, and currency formats follow that locale.

---

## 3. Prioritized backlog and sprint suggestion

> Approximate total estimate for the MVP (main M + S): ~105 pts. Indicative 2-week sprints.

| Sprint | Focus | Stories |
|---|---|---|
| **1 — Foundations** | Data, accounts, base recording, language | US-1.1, 1.2, 12.1, 3.1, 3.2, 2.1, 2.2, 2.7 |
| **2 — AI and categories** | On-device categorization | US-4.1, 4.4, 4.2, 4.3, 4.5 |
| **3 — Budget and recurring** | Spending control | US-5.1, 5.2, 5.3, 6.1, 6.2, 6.3 |
| **4 — Advanced capture + multi-currency** | Speed and currencies | US-2.3, 2.4, 2.5, 8.1, 8.2, 8.4 |
| **5 — Reports, data, and security** | Insight + trust | US-7.1, 7.2, 9.1, 9.2, 9.3, 10.1, 10.2, 11.1, 12.2 |
| **Buffer / Could** | If time allows | US-2.6, 3.3, 7.3, 8.3, 11.2 |

---

## 4. Risks to watch in the MVP

- **Initial AI quality:** mitigated with a merchant dictionary + rules (the app is useful from day 1 even before the model “knows you”).
- **Recording speed:** it's success criterion #1; any flow exceeding ≤ 5 s / ≤ 3 taps must be reviewed.
- **No data loss:** every migration/backup must be tested with real data before release.
- **Localization completeness:** ship with both es and en fully translated; no truncation or hard-coded strings.

---

> **Note:** stories marked **C (Could)** are candidates to cut if time gets tight, without compromising the core value proposition (fast recording + local AI + offline).
