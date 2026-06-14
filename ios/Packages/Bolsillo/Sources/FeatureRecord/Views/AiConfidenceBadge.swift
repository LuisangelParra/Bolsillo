import SwiftUI
import BolsilloDesignSystem

public struct AiConfidenceBadge: View {
    let confidence: Double
    @Environment(\.bolsilloTheme) var theme

    public init(confidence: Double) { self.confidence = confidence }

    public var body: some View {
        let state = confidenceState(confidence)
        let visual = confidenceVisual(for: state)
        let fg = theme.colors[keyPath: visual.fg]
        let bg = theme.colors[keyPath: visual.container]

        HStack(spacing: theme.spacing.xs) {
            Image(systemName: visual.icon)
                .font(theme.typography.badge)
                .foregroundStyle(fg)
            Text(String(localized: String.LocalizationValue(visual.labelKey), bundle: .module))
                .font(theme.typography.badge)
                .foregroundStyle(fg)
        }
        .padding(.horizontal, theme.spacing.sm)
        .padding(.vertical, theme.spacing.xxs)
        .background(bg)
        .cornerRadius(theme.radius.full)
    }
}
