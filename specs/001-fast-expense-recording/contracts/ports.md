# Contracts — Domain Ports (Android)

The `:domain` module exposes only these interfaces; `:data` implements them, `:feature-record` consumes use cases (not ports directly). Signatures are the contract both the implementation and tests must honor. **Existing** = already in the scaffold; **extend** = add methods; **new** = to create.

## ExpenseClassifier (exists — `com.bolsillo.domain.ai`)
```kotlin
interface ExpenseClassifier {
    suspend fun suggest(input: ClassificationInput): ClassificationResult
    suspend fun learn(input: ClassificationInput, chosenCategoryId: String) // no-op stub in 001
    companion object { const val DEFAULT_THRESHOLD: Double = 0.75 }
}
```
- 001 stub `LastUsedExpenseClassifier`: `suggest` → last-used `categoryId`, `confidence = 0.0`, no alternatives. MUST NOT throw and MUST return promptly (≤ 200 ms). `learn` is a no-op (personalization is E4).

## TransactionRepository (extend — `com.bolsillo.domain.port`)
```kotlin
interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>           // exists — active only (deletedAt == null)
    suspend fun getById(id: String): Transaction?       // exists
    suspend fun upsert(transaction: Transaction)        // exists — single-row, transactional
    suspend fun softDelete(id: String, deletedAt: Long) // exists
    suspend fun restore(id: String)                     // exists

    // --- extend for 001 ---
    suspend fun lastUsed(): Transaction?                                   // most recent active (for fallbacks)
    suspend fun upsertTransfer(legSource: Transaction, legDest: Transaction) // atomic pair write
    suspend fun softDeleteGroup(transferGroupId: String, deletedAt: Long)  // both legs together
    suspend fun restoreGroup(transferGroupId: String)                      // both legs together
}
```
**Contract**: no hard-delete method exists; multi-row methods are atomic (all-or-nothing).

## AccountRepository (extend — `com.bolsillo.domain.port`)
```kotlin
interface AccountRepository {
    fun observeCurrencies(): Flow<List<Currency>>       // exists — essentials always present

    // --- extend for 001 ---
    fun observeAccounts(): Flow<List<Account>>          // active (non-archived) accounts
    suspend fun getById(id: String): Account?
    fun observeBalance(accountId: String): Flow<Money>  // DERIVED: initial + SUM(signed, non-deleted)
    fun observeBalances(): Flow<Map<String, Money>>     // all accounts' derived balances
}
```
**Contract**: balance is derived per [data-model.md](../data-model.md) Invariant 2 — implementations MUST NOT return a cached/stored balance.
