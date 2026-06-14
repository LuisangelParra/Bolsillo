import Testing
import BolsilloDomain

@Suite struct MoneyRoundingTests {

    // MARK: round(_:) — identity for same-currency (no fractional minor units)

    @Test func roundPositiveIsIdentity() {
        #expect(MoneyRounding.round(100) == 100)
    }

    @Test func roundZeroIsIdentity() {
        #expect(MoneyRounding.round(0) == 0)
    }

    @Test func roundNegativeIsIdentity() {
        #expect(MoneyRounding.round(-50) == -50)
    }

    // MARK: divide(_:by:) — banker's rounding (round-half-to-even)

    @Test func divisionExact() {
        #expect(MoneyRounding.divide(100, by: 4) == 25)
        #expect(MoneyRounding.divide(10, by: 2) == 5)
    }

    @Test func divisionTruncatesDown() {
        // 10 / 3 = 3.333… → truncate to 3
        #expect(MoneyRounding.divide(10, by: 3) == 3)
    }

    @Test func divisionHalfToEvenRoundsUp() {
        // 7 / 2 = 3.5 → nearest even = 4
        #expect(MoneyRounding.divide(7, by: 2) == 4)
    }

    @Test func divisionHalfToEvenRoundsDown() {
        // 5 / 2 = 2.5 → nearest even = 2
        #expect(MoneyRounding.divide(5, by: 2) == 2)
    }

    @Test func divisionByZeroReturnsZero() {
        // Guard clause: division by zero returns 0, never crashes
        #expect(MoneyRounding.divide(100, by: 0) == 0)
    }

    @Test func sameCurrencyFxRateIsIdentity() {
        // In same-currency feature 001, fxRateMillis = 1000
        // divide(1000, by: 1000) = 1 (no rounding needed)
        #expect(MoneyRounding.divide(1000, by: 1000) == 1)
    }

    @Test func divisionNegativeNumerator() {
        // -10 / 2 = -5 exact
        #expect(MoneyRounding.divide(-10, by: 2) == -5)
    }

    @Test func divisionNegativeNumeratorHalf() {
        // -7 / 2 = -3.5 → nearest even = -4
        #expect(MoneyRounding.divide(-7, by: 2) == -4)
    }
}
