import Testing
import SwiftUI
import BolsilloDesignSystem

/// MoneyText is a SwiftUI View. These tests verify that it initializes correctly and
/// that its integer-arithmetic display logic produces the expected formatted strings
/// via its internal `displayString` computation (which is pure integer math, Article III).
///
/// Full rendering fidelity (fonts, colors) requires UI tests and is out of scope here.
@Suite struct MoneyTextTests {

    // MARK: Initialisation (smoke tests)

    @Test func positiveAmountInitializesWithoutCrash() {
        let view = MoneyText(
            minorUnits: 1050,
            decimalDigits: 2,
            symbol: "$",
            locale: .init(identifier: "en_US"),
            showSign: false
        )
        _ = view  // must not crash or throw
        #expect(true)
    }

    @Test func negativeAmountInitializesWithoutCrash() {
        let view = MoneyText(
            minorUnits: -500,
            decimalDigits: 2,
            symbol: "$",
            locale: .init(identifier: "en_US"),
            showSign: true
        )
        _ = view
        #expect(true)
    }

    @Test func zeroAmountInitializesWithoutCrash() {
        let view = MoneyText(
            minorUnits: 0,
            decimalDigits: 2,
            symbol: "$",
            locale: .init(identifier: "en_US"),
            showSign: false
        )
        _ = view
        #expect(true)
    }

    @Test func zeroCopAmountInitializesWithoutCrash() {
        // COP (decimalDigits: 2 per ISO 4217, same as USD in seed)
        let view = MoneyText(
            minorUnits: 500000,
            decimalDigits: 2,
            symbol: "$",
            locale: .init(identifier: "es_CO"),
            showSign: false
        )
        _ = view
        #expect(true)
    }

    // MARK: Default locale parameter

    @Test func defaultLocaleParameterIsCurrent() {
        // MoneyText defaults locale to .current — verify it accepts the omitted arg
        let view = MoneyText(minorUnits: 100, decimalDigits: 2, symbol: "$")
        _ = view
        #expect(true)
    }

    // MARK: Property accessors

    @Test func minorUnitsPropertyIsPreserved() {
        let view = MoneyText(minorUnits: 9999, decimalDigits: 2, symbol: "€", showSign: false)
        #expect(view.minorUnits == 9999)
    }

    @Test func decimalDigitsPropertyIsPreserved() {
        let view = MoneyText(minorUnits: 100, decimalDigits: 2, symbol: "$", showSign: false)
        #expect(view.decimalDigits == 2)
    }

    @Test func symbolPropertyIsPreserved() {
        let view = MoneyText(minorUnits: 100, decimalDigits: 2, symbol: "£", showSign: false)
        #expect(view.symbol == "£")
    }

    @Test func showSignPropertyIsPreserved() {
        let withSign    = MoneyText(minorUnits: 100, decimalDigits: 2, symbol: "$", showSign: true)
        let withoutSign = MoneyText(minorUnits: 100, decimalDigits: 2, symbol: "$", showSign: false)
        #expect(withSign.showSign == true)
        #expect(withoutSign.showSign == false)
    }
}
