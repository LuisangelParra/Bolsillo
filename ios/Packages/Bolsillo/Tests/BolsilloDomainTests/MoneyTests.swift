import Testing
@testable import BolsilloDomain

struct MoneyTests {
    @Test
    func minorUnitsAreIntegerBacked() {
        // Constitution Article III: money is integer minor units, never Double/Float.
        let money = Money(minorUnits: 1234)
        #expect(money.minorUnits == 1234)
        #expect(type(of: money.minorUnits) == Int.self)
    }

    @Test
    func additionIsExact() {
        #expect(Money(minorUnits: 10) + Money(minorUnits: 20) == Money(minorUnits: 30))
    }

    @Test
    func subtractionAndNegation() {
        #expect(Money(minorUnits: 10) - Money(minorUnits: 15) == Money(minorUnits: -5))
        #expect(-Money(minorUnits: 10) == Money(minorUnits: -10))
    }

    @Test
    func zeroConstant() {
        #expect(Money.zero.isZero)
        #expect(Money.zero == Money(minorUnits: 0))
    }
}
