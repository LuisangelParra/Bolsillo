# Quickstart — Validate "Fast expense recording" (iOS)

Run from `ios/`. Proves the feature end-to-end against the spec acceptance criteria. Not implementation code — see [contracts/ports-ios.md](./contracts/ports-ios.md), [contracts/use-cases-ios.md](./contracts/use-cases-ios.md), [data-model.md](./data-model.md), and [data-model-ios.md](./data-model-ios.md) for shapes.

## Prerequisites
- Xcode 15+/16 (iOS 17 SDK), an iPhone 16 simulator (or device) on iOS 17+.
- Tuist installed (project is generated from `Project.swift`).
- No network needed — the feature is 100% offline (validate in airplane mode).

## Generate & build
```bash
tuist generate --no-open                 # regenerate Bolsillo.xcworkspace from Project.swift
swiftlint --config .swiftlint.yml        # lint
```

## Unit / integration tests (the contract)
```bash
# Fast domain loop (host build via the package .macOS floor):
#   money math, use cases, transfer integrity, reconciliation, overdraft, same-account + cross-currency reject
swift test --package-path Packages/Bolsillo

# Full build + data/design/UI tests on the simulator:
#   GRDB DAO + balance SUM + soft-delete filtering + migration-with-sample-data + SQLCipher open;
#   theme/MoneyText/confidence; 3-tap flow + transfer same-account error + es/en rendering
xcodebuild test -workspace Bolsillo.xcworkspace -scheme Bolsillo \
  -destination 'platform=iOS Simulator,name=iPhone 16,OS=latest' CODE_SIGNING_ALLOWED=NO
```
Expected: all green. Release is blocked (Article IX) if any migration loses data, balances fail to reconcile, or es/en UI coverage is incomplete.

## Manual validation (golden path + edge cases)
Launch the debug app **in airplane mode**:

1. **3-tap expense** — app opens on the keypad with the amount ready (custom keypad on screen, no system keyboard, no loading screen). Type an amount → category + account are pre-filled → tap **Save**. Expect: balance drops immediately, an **undo** toast appears (≤ 3 taps, ≤ 5 s).
2. **Undo** — tap undo within ~5 s → transaction gone, balance restored.
3. **Income** — switch to Ingreso, enter amount, Save → balance rises, marked income (green `+`).
4. **Transfer** — switch to Transferencia, pick two **different** same-currency accounts, Save → source drops, destination rises; selecting the same account on both sides shows `record.transfer.sameAccountError` and blocks save; the transfer is excluded from expense/income totals.
5. **Overdraft** — record an expense larger than the balance → it saves, balance goes negative, no block/warning.
6. **Edit** — open a saved transaction, change amount or account → affected balances recompute and reconcile.
7. **Delete & restore** — delete a transaction → moves to trash, excluded from balance; restore → reappears, balance reflects it. Deleting one transfer leg removes both; restoring brings both back.
8. **Reconciliation** — after any sequence above, each account balance equals `initialBalance + Σ(signed non-deleted amounts)`.

## Language check
Run the scheme with the app language set to English (Scheme ▸ Options ▸ App Language = English, or `-AppleLanguages '(en)'`) → all recording strings render from the `en` entries in `Localizable.xcstrings` with no truncation and no hard-coded text; switch back to Spanish (es, the source/default).

## Encryption spot-check
- Confirm the on-device DB file opens only with the Keychain key: the SQLCipher smoke test fails to read the DB without the passphrase (Articles I/II).

## Performance spot-check (reference device, Instruments)
- Cold start to usable keypad ≤ 1.5 s.
- Save feels instant (≤ 100 ms); no spinner during recording; no main-thread DB work on the save path.
- Suggestion pre-fill ≤ 200 ms (stub is instant; last-used fallback if absent).
