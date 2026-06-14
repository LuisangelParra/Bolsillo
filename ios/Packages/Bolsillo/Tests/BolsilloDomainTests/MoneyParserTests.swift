import Testing
import BolsilloDomain

@Suite struct MoneyParserTests {
    let parser = MoneyParser()
    let usd = Currency(code: "USD", symbol: "$", decimalDigits: 2, isEnabled: true, isEssential: true)
    // COP is ISO 4217 decimalDigits: 2, but the keypad treats "5000" as 5000 minor units
    // regardless — the parser never divides; decimalDigits only affects display formatting.
    let cop = Currency(code: "COP", symbol: "$", decimalDigits: 2, isEnabled: true, isEssential: true)

    @Test func parseEmptyDigits() {
        #expect(parser.parse(digits: "", currency: usd) == .zero)
    }

    @Test func parseZeroDigits() {
        #expect(parser.parse(digits: "0", currency: usd) == Money(minorUnits: 0))
    }

    @Test func parseIntegerOnlyNeverUsesFloat() {
        // "1234" → 1234 minor units (= 12.34 USD displayed)
        let result = parser.parse(digits: "1234", currency: usd)
        #expect(result.minorUnits == 1234)
    }

    @Test func parseCOPAmount() {
        // COP keypad: "5000" → 5000 minor units
        let result = parser.parse(digits: "5000", currency: cop)
        #expect(result.minorUnits == 5000)
    }

    @Test func parseStripsNonDigits() {
        // Non-digit characters are stripped defensively
        let result = parser.parse(digits: "12abc34", currency: usd)
        #expect(result.minorUnits == 1234)
    }

    @Test func parseLargeAmount() {
        let result = parser.parse(digits: "999999", currency: usd)
        #expect(result.minorUnits == 999999)
    }

    @Test func parseSingleDigit() {
        let result = parser.parse(digits: "5", currency: usd)
        #expect(result.minorUnits == 5)
    }

    @Test func parseAllZeros() {
        let result = parser.parse(digits: "000", currency: usd)
        #expect(result.minorUnits == 0)
    }

    @Test func parseOnlyNonDigitsReturnsZero() {
        let result = parser.parse(digits: "abc", currency: usd)
        #expect(result == .zero)
    }
}
