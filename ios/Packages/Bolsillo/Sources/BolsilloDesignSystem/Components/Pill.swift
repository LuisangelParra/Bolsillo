import SwiftUI

/// Badge pill — compact rounded label used for tags, status chips, and count badges.
public struct Pill: View {
    public let text: String
    public let fg: Color
    public let background: Color

    @Environment(\.bolsilloTheme) private var theme

    public init(text: String, fg: Color, background: Color) {
        self.text       = text
        self.fg         = fg
        self.background = background
    }

    public var body: some View {
        Text(text)
            .font(theme.typography.badge)
            .foregroundStyle(fg)
            .padding(.horizontal, theme.spacing.sm)
            .padding(.vertical, theme.spacing.xs)
            .background(background, in: Capsule())
    }
}
