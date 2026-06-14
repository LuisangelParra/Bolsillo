import SwiftUI
import BolsilloDesignSystem

public struct UndoToast: View {
    let onUndo: () -> Void
    let onDismiss: () -> Void
    @Environment(\.bolsilloTheme) var theme

    public init(onUndo: @escaping () -> Void, onDismiss: @escaping () -> Void) {
        self.onUndo = onUndo
        self.onDismiss = onDismiss
    }

    public var body: some View {
        HStack(spacing: theme.spacing.base) {
            Image(systemName: "checkmark.circle.fill")
                .foregroundStyle(theme.colors.success)
            Text(String(localized: "record.saved.toast", bundle: .module))
                .font(theme.typography.body)
                .foregroundStyle(theme.colors.onSurfaceInverse)
            Spacer()
            Button(String(localized: "record.undo", bundle: .module), action: onUndo)
                .font(theme.typography.bodyStrong)
                .foregroundStyle(theme.colors.primaryAccent)
        }
        .padding(.horizontal, theme.spacing.xxl)
        .padding(.vertical, theme.spacing.base)
        .background(theme.colors.surfaceInverse)
        .clipShape(Capsule())
        .shadow(
            color: theme.elevation.toast.color,
            radius: theme.elevation.toast.radius,
            x: theme.elevation.toast.x,
            y: theme.elevation.toast.y
        )
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) {
                onDismiss()
            }
        }
    }
}
