import SwiftUI
import BolsilloDesignSystem

public struct AmountKeypad: View {
    let onDigit: (String) -> Void
    let onBackspace: () -> Void
    @Environment(\.bolsilloTheme) var theme

    private let rows: [[String]] = [
        ["1", "2", "3"],
        ["4", "5", "6"],
        ["7", "8", "9"],
        ["000", "0", "⌫"],
    ]

    public init(onDigit: @escaping (String) -> Void, onBackspace: @escaping () -> Void) {
        self.onDigit = onDigit
        self.onBackspace = onBackspace
    }

    public var body: some View {
        VStack(spacing: theme.spacing.sm) {
            // Hint strip
            HStack {
                Image(systemName: "sparkles")
                    .font(theme.typography.caption)
                    .foregroundStyle(theme.colors.onPrimaryContainer)
                Text(String(localized: "record.keypad.hint", bundle: .module))
                    .font(theme.typography.caption)
                    .foregroundStyle(theme.colors.onPrimaryContainer)
            }
            .padding(.horizontal, theme.spacing.base)
            .padding(.vertical, theme.spacing.xs)
            .frame(maxWidth: .infinity)
            .background(theme.colors.primaryContainer)
            .cornerRadius(theme.radius.control)

            ForEach(rows, id: \.self) { row in
                HStack(spacing: theme.spacing.sm) {
                    ForEach(row, id: \.self) { key in
                        KeypadButton(
                            key: key,
                            theme: theme,
                            action: {
                                if key == "⌫" { onBackspace() }
                                else { onDigit(key) }
                            }
                        )
                    }
                }
            }
        }
    }
}

private struct KeypadButton: View {
    let key: String
    let theme: BolsilloTheme
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(key)
                .font(theme.typography.keypadDigit)
                .foregroundStyle(theme.colors.textPrimary)
                .frame(maxWidth: .infinity, minHeight: 56)
                .background(theme.colors.surface)
                .cornerRadius(theme.radius.control)
                .shadow(
                    color: theme.elevation.key.color,
                    radius: theme.elevation.key.radius,
                    x: theme.elevation.key.x,
                    y: theme.elevation.key.y
                )
        }
        .buttonStyle(.plain)
    }
}
