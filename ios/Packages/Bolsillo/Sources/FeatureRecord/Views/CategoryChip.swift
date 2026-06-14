import SwiftUI
import BolsilloDesignSystem

public struct CategoryChip: View {
    let icon: String
    let label: String
    let fg: Color
    let container: Color
    let isLowConfidence: Bool
    @Environment(\.bolsilloTheme) var theme

    public init(icon: String, label: String, fg: Color, container: Color, isLowConfidence: Bool = false) {
        self.icon = icon
        self.label = label
        self.fg = fg
        self.container = container
        self.isLowConfidence = isLowConfidence
    }

    public var body: some View {
        HStack(spacing: theme.spacing.xs) {
            CategoryIconTile(icon: icon, fg: fg, container: container, size: .sm)
            VStack(alignment: .leading, spacing: 0) {
                Text(String(localized: "record.category", bundle: .module))
                    .font(theme.typography.caption)
                    .foregroundStyle(theme.colors.textMuted)
                Text(label)
                    .font(theme.typography.bodyValue)
                    .foregroundStyle(theme.colors.textPrimary)
            }
        }
        .padding(.horizontal, theme.spacing.base)
        .padding(.vertical, theme.spacing.sm)
        .background(theme.colors.surface)
        .overlay(
            RoundedRectangle(cornerRadius: theme.radius.chip)
                .stroke(
                    isLowConfidence ? theme.colors.warningBorder : theme.colors.outline,
                    lineWidth: 1.5
                )
        )
        .cornerRadius(theme.radius.chip)
    }
}

public struct AccountChip: View {
    let icon: String
    let label: String
    let balance: String?
    @Environment(\.bolsilloTheme) var theme

    public init(icon: String, label: String, balance: String? = nil) {
        self.icon = icon
        self.label = label
        self.balance = balance
    }

    public var body: some View {
        HStack(spacing: theme.spacing.xs) {
            Image(systemName: icon)
                .font(theme.typography.body)
                .foregroundStyle(theme.colors.primary)
                .frame(width: 28, height: 28)
                .background(theme.colors.primaryContainer)
                .cornerRadius(theme.radius.iconTileSm)
            VStack(alignment: .leading, spacing: 0) {
                Text(String(localized: "record.account", bundle: .module))
                    .font(theme.typography.caption)
                    .foregroundStyle(theme.colors.textMuted)
                Text(label)
                    .font(theme.typography.bodyValue)
                    .foregroundStyle(theme.colors.textPrimary)
            }
        }
        .padding(.horizontal, theme.spacing.base)
        .padding(.vertical, theme.spacing.sm)
        .background(theme.colors.surface)
        .overlay(
            RoundedRectangle(cornerRadius: theme.radius.chip)
                .stroke(theme.colors.outline, lineWidth: 1)
        )
        .cornerRadius(theme.radius.chip)
    }
}
