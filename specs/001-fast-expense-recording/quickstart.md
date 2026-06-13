# Quickstart — Validate "Fast expense recording" (Android)

Run from `android/`. Proves the feature end-to-end against the spec acceptance criteria. Not implementation code — see [contracts/](./contracts/) and [data-model.md](./data-model.md) for shapes.

## Prerequisites
- JDK 17 toolchain (CI: JDK 21), Android SDK (`compileSdk` 35), an emulator/device on `minSdk` 26+.
- No network needed — the feature is 100% offline (validate in airplane mode).

## Build & static checks
```bash
./gradlew ktlintCheck
./gradlew :domain:test :data:test
./gradlew assembleDebug
```

## Unit / integration tests (the contract)
```bash
# Domain: money math, use cases, transfer integrity, reconciliation, overdraft, same-account reject
./gradlew :domain:test

# Data: Room DAO + balance SUM + soft-delete filtering + migration-with-sample-data
./gradlew :data:test

# UI: 3-tap flow + transfer same-account error + es/en rendering
./gradlew :feature-record:connectedAndroidTest
```
Expected: all green. Release is blocked (Article IX) if any migration loses data, balances fail to reconcile, or es/en UI coverage is incomplete.

## Manual validation (golden path + edge cases)
Launch the debug app **in airplane mode**:

1. **3-tap expense** — app opens on the keypad with the amount focused (keyboard up, no loading screen). Type an amount → category + account are pre-filled → tap **Save**. Expect: balance drops immediately, an **undo** snackbar appears (≤ 3 taps, ≤ 5 s).
2. **Undo** — tap undo within ~5 s → transaction gone, balance restored.
3. **Income** — switch to Income, enter amount, Save → balance rises, marked income.
4. **Transfer** — switch to Transfer, pick two **different** same-currency accounts, Save → source drops, destination rises; selecting the same account on both sides shows `record_transfer_sameAccountError` and blocks save; a transfer is excluded from expense/income totals.
5. **Overdraft** — record an expense larger than the balance → it saves, balance goes negative, no block/warning.
6. **Edit** — open a saved transaction, change amount or account → affected balances recompute and reconcile.
7. **Delete & restore** — delete a transaction → moves to trash, excluded from balance; restore → reappears, balance reflects it. Deleting one transfer leg removes both; restoring brings both back.
8. **Reconciliation** — after any sequence above, each account balance equals `initialBalance + Σ(signed non-deleted amounts)`.

## Language check
Switch device language to English → all recording strings render from `values-en/` with no truncation and no hard-coded text; switch back to Spanish (`values/`, default).

## Performance spot-check (reference low-end device)
- Cold start to usable keypad ≤ 1.5 s.
- Save feels instant (≤ 100 ms); no spinner during recording.
- Suggestion pre-fill ≤ 200 ms (stub is instant; last-used fallback if absent).
