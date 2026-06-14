import SwiftUI
import BolsilloDesignSystem

public struct AmountDisplay: View {
    let digits: String
    let currencySymbol: String
    let currencyCode: String
    @State private var caretVisible = true
    let timer = Timer.publish(every: 0.55, on: .main, in: .common).autoconnect()
    @Environment(\.bolsilloTheme) var theme

    public init(digits: String, currencySymbol: String, currencyCode: String) {
        self.digits = digits
        self.currencySymbol = currencySymbol
        self.currencyCode = currencyCode
    }

    public var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: theme.spacing.xs) {
            Text(currencySymbol)
                .font(theme.typography.moneyL)
                .foregroundStyle(digits.isEmpty ? theme.colors.textDisabled : theme.colors.textPrimary)

            Text(digits.isEmpty ? "0" : digits)
                .font(theme.typography.displayAmount)
                .foregroundStyle(digits.isEmpty ? theme.colors.textDisabled : theme.colors.textPrimary)
                .monospacedDigit()

            Rectangle()
                .fill(caretVisible ? theme.colors.caret : Color.clear)
                .frame(width: 2, height: 52)
        }
        .onReceive(timer) { _ in caretVisible.toggle() }
        .padding(.vertical, theme.spacing.m)
    }
}
