import SwiftUI
import BolsilloDesignSystem

public struct ModeTabs: View {
    @Environment(\.bolsilloTheme) var theme
    @State private var selected = 0

    private let tabs = [
        String(localized: "record.mode.keypad", bundle: .module),
        String(localized: "record.mode.text", bundle: .module),
        String(localized: "record.mode.receipt", bundle: .module),
    ]

    public init() {}

    public var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \.offset) { idx, tab in
                Button {
                    if idx == 0 { selected = 0 }
                    // Texto/Recibo inert in 001 (deferred, spec 007)
                } label: {
                    Text(tab)
                        .font(theme.typography.label)
                        .foregroundStyle(selected == idx ? theme.colors.textPrimary : theme.colors.textMuted)
                        .padding(.vertical, theme.spacing.sm)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
                .disabled(idx != 0)
            }
        }
        .overlay(alignment: .bottom) {
            GeometryReader { geo in
                let tabWidth = geo.size.width / CGFloat(tabs.count)
                Rectangle()
                    .fill(theme.colors.primary)
                    .frame(width: 24, height: 2)
                    .offset(x: CGFloat(selected) * tabWidth + (tabWidth - 24) / 2)
            }
            .frame(height: 2)
        }
    }
}
