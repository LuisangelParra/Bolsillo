import BolsilloData
import BolsilloDomain
import Testing

struct SmokeTests {
    @Test
    func currencySeedHasEssentialUsdAndCop() {
        let codes = CurrencySeed.essentials.map(\.code)
        let allEssential = CurrencySeed.essentials.allSatisfy(\.isEssential)
        #expect(codes.contains("USD"))
        #expect(codes.contains("COP"))
        #expect(allEssential)
    }

    @Test
    func softDeleteKeepsRecordOutOfActiveList() async {
        let repo = InMemoryTransactionRepository()
        let tx = Transaction(
            id: "t1",
            accountId: "acc-1",
            type: .expense,
            amount: Money(minorUnits: 1500),
            currencyCode: "COP",
            amountBase: Money(minorUnits: 1500),
            fxRateMillis: 1000,
            occurredAt: .init(timeIntervalSince1970: 0),
            createdAt: .init(timeIntervalSince1970: 0),
            updatedAt: .init(timeIntervalSince1970: 0)
        )
        await repo.upsert(tx)
        await repo.softDelete(id: "t1", deletedAt: .init(timeIntervalSince1970: 123))

        let active = await repo.observeAll()
        #expect(active.isEmpty)

        // No hard delete: the record still exists and is restorable.
        let stored = await repo.getById("t1")
        #expect(stored?.deletedAt != nil)

        await repo.restore(id: "t1")
        let afterRestore = await repo.observeAll()
        #expect(afterRestore.count == 1)
    }
}
