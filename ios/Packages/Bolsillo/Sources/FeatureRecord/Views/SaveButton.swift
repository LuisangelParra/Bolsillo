import SwiftUI
import BolsilloDesignSystem

public struct SaveButton: View {
    let enabled: Bool
    let action: () -> Void
    @Environment(\.bolsilloTheme) var theme

    public init(enabled: Bool, action: @escaping () -> Void) {
        self.enabled = enabled
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            HStack {
                Text(String(localized: "record.save", bundle: .module))
                    .font(theme.typography.button)
                Image(systemName: "arrow.right")
                    .font(theme.typography.button)
            }
            .foregroundStyle(enabled ? theme.colors.onPrimary : theme.colors.textDisabled)
            .frame(maxWidth: .infinity)
            .padding(.vertical, theme.spacing.l)
            .background(
                Group {
                    if enabled {
                        AnyView(theme.gradients.primary)
                    } else {
                        AnyView(theme.colors.track)
                    }
                }
            )
            .cornerRadius(theme.radius.frame)
            .shadow(
                color: enabled ? theme.elevation.buttonPrimary.color : .clear,
                radius: theme.elevation.buttonPrimary.radius,
                x: theme.elevation.buttonPrimary.x,
                y: theme.elevation.buttonPrimary.y
            )
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}
