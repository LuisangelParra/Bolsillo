import SwiftUI

/// Renders a money amount from primitive inputs only — no domain Money import (Article VIII).
/// `minorUnits` is the integer amount in the currency's smallest unit (e.g. cents).
/// `decimalDigits` is the number of decimal places for the currency (e.g. 2 for USD).
/// Display conversion uses integer arithmetic only — never floating-point.
public struct MoneyText: View {
    public let minorUnits: Int
    public let decimalDigits: Int
    public let symbol: String
    public let locale: Locale
    public let showSign: Bool

    @Environment(\.bolsilloTheme) private var theme

    public init(
        minorUnits: Int,
        decimalDigits: Int,
        symbol: String,
        locale: Locale = .current,
        showSign: Bool = false
    ) {
        self.minorUnits    = minorUnits
        self.decimalDigits = decimalDigits
        self.symbol        = symbol
        self.locale        = locale
        self.showSign      = showSign
    }

    public var body: some View {
        Text(displayString)
            .font(theme.typography.amountRow)
            .foregroundStyle(amountColor)
    }

    // MARK: Private helpers

    private var amountColor: Color {
        minorUnits >= 0 ? theme.colors.amountPositive : theme.colors.amountNegative
    }

    private var displayString: String {
        let absUnits = abs(minorUnits)
        let formatted: String
        if decimalDigits == 0 {
            formatted = "\(symbol)\(absUnits)"
        } else {
            let divisor   = pow10(decimalDigits)
            let whole     = absUnits / divisor
            let remainder = absUnits % divisor
            let fracStr   = String(remainder).leftPadded(toLength: decimalDigits, with: "0")
            let decSep    = locale.decimalSeparator ?? "."
            formatted = "\(symbol)\(whole)\(decSep)\(fracStr)"
        }
        if showSign {
            return minorUnits < 0 ? "-\(formatted)" : "+\(formatted)"
        }
        return minorUnits < 0 ? "-\(formatted)" : formatted
    }

    private func pow10(_ n: Int) -> Int {
        var result = 1
        for _ in 0..<n { result *= 10 }
        return result
    }
}

private extension String {
    func leftPadded(toLength length: Int, with pad: Character) -> String {
        guard count < length else { return self }
        return String(repeating: pad, count: length - count) + self
    }
}
