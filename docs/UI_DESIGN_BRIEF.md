# UI Design Prompt / Brief — “Bolsillo”

## How to use this document

You are the one designing this mobile app's interface. Your task is to design the complete UI/UX that covers everything described here.

This brief gives you only **what the app is, what data it handles, its features, and its user stories**. It contains no design decisions *on purpose*: if anything visual or experiential seems missing, it is not an oversight — it is left out so as not to bias you.

**All design decisions are yours**, including (but not limited to): the number and organization of screens, navigation structure, visual hierarchy, layout, visual style, color palette, typography, component system, iconography, microinteractions, animations, states (empty, loading, error, success), information density, visual tone, and final copy.

> Design for **Android and iOS**. The app works **100% offline** and stores data on the device itself; it **does not require creating an account**. Its users value, above all else, **speed and low friction** when recording money.

---

## 1. What the application is

Bolsillo is a personal-finance mobile app for **recording and managing money**: expenses, income, transfers, accounts, and budgets.

Its core purpose is twofold:
1. Making **recording a transaction extremely fast** (goal: record an expense in ≤ 3 taps and ≤ 5 seconds).
2. Ensuring every expense is **correctly classified into a category automatically**.

The app includes automatic classification that **suggests a category for each expense and learns from the user's corrections**, all on the phone. It works fully offline, and the user's data never leaves the device by default.

---

## 2. Data structure (the information the interface works with)

This is the data the interface displays, captures, and edits. *How* it is presented is your decision.

- **Account:** name · type (cash, debit, credit, bank, savings, wallet, other) · currency · initial balance · icon · color · archived (yes/no) · current balance (computed) · credit only: limit, statement day, payment day.
- **Transaction:** type (expense / income / transfer) · amount · currency · exchange rate · amount in base currency · category · merchant · note · date and time · tag(s) · account · transfer link (for the source/destination pair) · associated recurrence · source (manual / natural language / receipt) · AI-suggested category · AI confidence level · status (confirmed / to confirm) · in trash (yes/no).
- **Category:** name · parent category (2-level subcategories) · icon · color · system or custom · archived.
- **Tag:** name (assignable to multiple transactions).
- **Budget:** associated category (or global) · period (weekly / monthly / custom) · amount · spent (computed) · available (computed) · spending pace (computed) · configured alerts (e.g., 80%, 100%, overspend).
- **Recurring / subscription:** template (type, amount, category, account, merchant) · frequency and interval · next date · end date (optional) · reminder (yes/no) · auto-create (yes/no).
- **User categorization rule:** condition (e.g., “merchant contains X”) · target category · priority · active (yes/no).
- **Exchange rate:** source currency · target currency · rate · date.
- **Settings:** base currency · AI confidence threshold · lock (biometrics/PIN) · permissions · general preferences.

---

## 3. Features (functional capabilities to cover)

**Recording transactions**
- Record expenses, income, and transfers between accounts.
- Very fast recording (goal ≤ 3 taps / ≤ 5 s) with pre-suggested category and account.
- Natural-language entry (e.g., “coffee 8 bucks”).
- Receipt-photo entry (automatic reading of amount, date, and merchant).
- Favorites / templates to repeat frequent expenses.
- System shortcuts (widget / quick action) to record without opening the full app.
- Edit and delete transactions, with a trash and restore.

**Accounts and balances**
- Create, edit, and archive accounts of different types.
- Per-account balance and total, updated in real time.
- Credit card with statement and payment day, and amount due.

**Local AI categorization**
- Automatic category suggestion for each expense, with a confidence indicator.
- “To confirm” status with 3 alternative categories when confidence is low.
- Learning from the user's corrections (on-device).
- User-defined categorization rules.
- Customizable categories and subcategories (icon and color).
- Reclassifying multiple transactions at once.

**Budgets**
- Budget per category (weekly/monthly/custom).
- “Spending pace” indicator and alerts (80%, 100%, overspend).
- “Available to spend” view.

**Recurring and subscriptions**
- Mark transactions as recurring (frequency and next date).
- Auto-generate the next transaction or a reminder to confirm it.
- List of active subscriptions with their total monthly/annual cost.

**Reports and insights**
- Spending by category and by period.
- Month-over-month comparison and trend.
- Weekly recap.
- Filters by account, category, tag, and date range.

**Multi-currency**
- Transactions in currencies other than the base, preserving the original amount.
- Totals converted to the base currency without losing the actual amount paid.
- Updatable exchange rates (manual or optional with connectivity).

**User data**
- Encrypted backup and restore.
- Export and import (CSV).

**Security and privacy**
- Lock with biometrics or PIN.
- Explained permissions (camera for receipts, optional location), with the app working even if they are denied.

**Search and management**
- Search and filter transactions by text, amount, date, category, tag, or account.
- Free-form tags in addition to categories.

---

## 4. User stories

Format: *As a* `<role>`, *I want* `<action>`, *so that* `<benefit>`. Each one includes the expected behavior that must be possible from the interface.

### Onboarding and setup
- **Use without an account:** *As a new user, I want to start using the app without signing up, so that I can try it with no friction and with privacy.* → Can record transactions with no email or account, also without connectivity.
- **Set up base currency and first account:** *As a new user, I want to choose my base currency and create my first account, so that amounts make sense from the start.*
- **Short onboarding:** *As a new user, I want a short onboarding, so that I quickly reach recording my first expense.* → Reach recording in ≤ 1 minute following the default flow.

### Recording transactions
- **Fast recording:** *As a user, I want to record an expense quickly, so that I don't drop the habit out of laziness.* → Amount + pre-suggested category/account + save; balance updates instantly; option to undo.
- **Income and transfer:** *As a user, I want to record income and transfers, so that I reflect all my movements.* → A transfer links two accounts and doesn't count as expense/income in reports.
- **Natural language:** *As a user, I want to type “coffee 8 bucks”, so that I record without navigating menus.* → Amount and category are interpreted; if ambiguous, it asks to confirm without blocking.
- **Receipt photo:** *As a user, I want to photograph a receipt, so that I capture amount, date, and merchant without typing.* → Pre-fills the form and proposes a category; works offline.
- **Favorites/templates:** *As a user, I want to repeat frequent expenses with one tap, so that I save time.*
- **System shortcuts:** *As a user, I want to record from a widget/shortcut, so that I do it without opening the full app.*
- **Edit/delete:** *As a user, I want to correct or delete a transaction, so that I keep my data accurate.* → Recomputes balance; deletion goes to trash and can be restored.

### Accounts and balances
- **Manage accounts:** *As a user, I want to create and manage accounts of different types, so that I organize my money.* → Create with type, currency, initial balance, icon, and color; archive while keeping history.
- **Real-time balance:** *As a user, I want to see each account's balance and the total, so that I know how much I have.* → Updates with each transaction; total in base currency.
- **Credit card:** *As a user, I want to record my card's statement and payment dates, so that I anticipate the payment.* → Shows amount due and a reminder.

### AI categorization
- **Automatic suggestion:** *As a user, I want the app to propose the category by itself, so that I don't classify manually.* → Immediate suggestion with a confidence indicator; high confidence applies automatically (with undo); low confidence is marked “to confirm” with 3 alternatives; works offline.
- **Learn from corrections:** *As a user, I want the app to learn when I correct it, so that it gets more accurate over time.* → Learning happens on-device and improves future suggestions for the same merchant/pattern.
- **Own rules:** *As a power user, I want to create categorization rules, so that I force classifications.*
- **Customizable categories:** *As a user, I want to create/edit categories and subcategories with icon and color, so that I adapt them to my life.* → A default set exists at the start.
- **Bulk reclassification:** *As a user, I want to recategorize several transactions at once, so that I correct quickly.*

### Budgets
- **Budget per category:** *As a user, I want to set a monthly budget per category, so that I control my spending.* → Shows spent vs available.
- **Spending pace:** *As a user, I want to be warned if I'm spending too fast, so that I don't overshoot before month's end.* → Pace alert and alerts at 80%/100%/overspend.
- **Available to spend:** *As a user, I want to see how much I have left, so that I decide clearly.*

### Recurring and subscriptions
- **Mark recurring:** *As a user, I want to mark an expense as recurring, so that I don't forget it.*
- **Generate the next one:** *As a user, I want the recurring item to be created automatically or with one tap, so that I keep the record up to date.* → Auto-create or a reminder, per preference.
- **See subscriptions:** *As a user, I want to see my active subscriptions and their total cost, so that I decide which to cut.*

### Reports and insights
- **Spending by category and period:** *As a user, I want to see in which categories and periods I spend, so that I understand my habits.* → With filters by account, category, tag, and dates.
- **Monthly comparison:** *As a user, I want to compare months and see the trend, so that I know if I'm improving.*
- **Weekly recap:** *As a user, I want a weekly recap of my spending, so that I stay on top of it.*

### Multi-currency
- **Transactions in another currency:** *As a user, I want to record expenses in other currencies, so that I handle travel or income in another currency.* → Preserves the original amount and its currency.
- **Unified view:** *As a user, I want to see totals in my base currency without losing the amount paid, so that I have context.* → Each transaction still shows its original amount.
- **Update rates:** *As a user, I want to update rates manually or online, so that I have up-to-date conversions.*

### User data
- **Encrypted backup:** *As a user, I want to back up my data encrypted to my own storage, so that I don't lose it when changing phones.*
- **Restore:** *As a user, I want to restore a backup, so that I recover everything on a new device.*
- **Export/import CSV:** *As a user, I want to export and import my data, so that I own it and migrate without friction.*

### Security and privacy
- **Lock:** *As a user, I want to lock the app with fingerprint/face or PIN, so that I protect my information.*
- **Permission transparency:** *As a user, I want to understand why camera/location are requested, so that I grant permissions with confidence.* → The app works even if they are denied.

### Search and management
- **Search and filter:** *As a user, I want to search and filter transactions by several criteria, so that I find something quickly.*
- **Free-form tags:** *As a user, I want to add tags in addition to categories, so that I group flexibly.*

---

## 5. Your deliverable

Design the interface that makes it possible to fulfill **all** of the features and user stories above, for Android and iOS. You decide the number of screens, their organization, their flow, and all of their appearance. This brief imposes no visual or interaction solution: those decisions are entirely yours.
