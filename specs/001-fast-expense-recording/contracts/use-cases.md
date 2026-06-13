# Contracts — Use Cases (Android)

New `com.bolsillo.domain.usecase` package. Each is a single-responsibility class with constructor-injected ports (Hilt). The UI (`RecordViewModel`) depends on these, never on repositories/DB directly (Article VIII). Signatures are indicative; each maps to spec FRs and gets unit tests (Article IX).

```kotlin
// FR 3,4,17 — get pre-fill; never throws, never blocks save
class SuggestCategoryAndAccount(
    private val classifier: ExpenseClassifier,
    private val transactions: TransactionRepository,
    private val accounts: AccountRepository,
) {
    suspend operator fun invoke(input: ClassificationInput): Suggestion // category + account, last-used fallback
}

// FR 5,8,14,16,19 — record expense or income (signed), single transactional write
class RecordTransaction(private val transactions: TransactionRepository) {
    suspend operator fun invoke(draft: TransactionDraft): Transaction // sign applied by type; overdraft allowed
}

// FR 9,10 — build + persist the linked pair atomically
class RecordTransfer(
    private val transactions: TransactionRepository,
    private val accounts: AccountRepository,
) {
    suspend operator fun invoke(
        sourceAccountId: String,
        destAccountId: String,
        amount: Money,            // magnitude; signs applied to each leg
        occurredAt: Long,
    ): TransferPair
    // throws/returns error if sourceAccountId == destAccountId (FR 10)
    //                       or currencies differ (Invariant 6, cross-currency deferred E8)
}

// FR 11 — edit; recompute by re-deriving balances; keeps transfer pair consistent
class EditTransaction(private val transactions: TransactionRepository) {
    suspend operator fun invoke(updated: Transaction)
}

// FR 12 — soft delete (single or whole transfer group)
class SoftDeleteTransaction(private val transactions: TransactionRepository) {
    suspend operator fun invoke(id: String, now: Long)
}

// FR 13 — restore from trash (single or whole transfer group)
class RestoreTransaction(private val transactions: TransactionRepository) {
    suspend operator fun invoke(id: String)
}

// FR 6,7 — undo the just-created record/group via soft delete (within undo window)
class UndoLastRecord(private val transactions: TransactionRepository) {
    suspend operator fun invoke(lastSaved: SavedRef, now: Long) // SavedRef = id or transferGroupId
}

// FR 6, US-3.2 — derived balances for the UI
class ObserveAccountBalances(private val accounts: AccountRepository) {
    operator fun invoke(): Flow<Map<String, Money>>
}

// FR 14 — float-free parse of keypad input → signed-able Money magnitude
class MoneyParser {
    fun parse(digits: String, currency: Currency): Money   // integer math only; honors decimalDigits
}
```

## Mapping to acceptance criteria
| Use case | Spec FR / AC |
|---|---|
| `SuggestCategoryAndAccount` | FR 3, 4; Offline & AI fallback AC |
| `RecordTransaction` | FR 5, 8, 14, 16, 19; Happy path, Income, Overdraft AC |
| `RecordTransfer` | FR 9, 10; Transfer AC (incl. same-account, same-currency) |
| `EditTransaction` | FR 11; Edit AC (amount, account change, transfer leg) |
| `SoftDeleteTransaction` / `RestoreTransaction` | FR 12, 13; Delete & restore AC |
| `UndoLastRecord` | FR 6, 7; Undo AC |
| `ObserveAccountBalances` | FR 6, 15; Integrity AC |
| `MoneyParser` | FR 14 |
