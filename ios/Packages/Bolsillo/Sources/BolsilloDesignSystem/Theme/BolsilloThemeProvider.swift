import SwiftUI

public struct BolsilloThemeProvider<Content: View>: View {
    @Environment(\.colorScheme) private var colorScheme
    private let content: Content

    public init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    public var body: some View {
        let theme: BolsilloTheme = colorScheme == .dark ? .dark : .light
        content
            .environment(\.bolsilloTheme, theme)
    }
}
