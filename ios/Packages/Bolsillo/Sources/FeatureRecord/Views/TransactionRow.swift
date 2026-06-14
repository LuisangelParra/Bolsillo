import SwiftUI
import BolsilloDesignSystem
import BolsilloDomain

public struct TransactionRow: View {
    let tx: BolsilloDomain.Transaction
    let categoryIcon: String
    let categoryFg: Color
    let categoryContainer: Color
    let currencySymbol: String
    let decimalDigits: Int
    let isToConfirm: Bool
    @Environment(\.bolsilloTheme) var theme

    public init(
        transaction: BolsilloDomain.Transaction,
        categoryIcon: String = "ellipsis.circle",
        categoryFg: Color = .gray,
        categoryContainer: Color = Color.gray.opacity(0.15),
        currencySymbol: String = "$",
        decimalDigits: Int = 2,
        isToConfirm: Bool = false
    ) {
        self.tx = transaction
        self.categoryIcon = categoryIcon
        self.categoryFg = categoryFg
        self.categoryContainer = categoryContainer
        self.currencySymbol = currencySymbol
        self.decimalDigits = decimalDigits
        self.isToConfirm = isToConfirm
    }

    public var body: some View {
        HStack(spacing: theme.spacing.base) {
            CategoryIconTile(icon: categoryIcon, fg: categoryFg, container: categoryContainer, size: .md)
            VStack(alignment: .leading, spacing: 2) {
                Text(tx.merchant ?? (tx.note ?? "—"))
                    .font(theme.typography.bodyStrong)
                    .foregroundStyle(theme.colors.textPrimary)
                if isToConfirm {
                    Pill(
                        text: String(localized: "record.toConfirm", bundle: .module),
                        fg: theme.colors.onWarningContainer,
                        background: theme.colors.warningContainer
                    )
                }
            }
            Spacer()
            MoneyText(
                minorUnits: tx.amount.minorUnits,
                decimalDigits: decimalDigits,
                symbol: currencySymbol,
                locale: .current,
                showSign: true
            )
        }
        .padding(.horizontal, theme.spacing.xl)
        .padding(.vertical, theme.spacing.base)
        .background(theme.colors.surface)
        .cornerRadius(theme.radius.card)
        .shadow(
            color: theme.elevation.e2.color,
            radius: theme.elevation.e2.radius,
            x: theme.elevation.e2.x,
            y: theme.elevation.e2.y
        )
    }
}
