import SwiftUI

private struct BolsilloThemeKey: EnvironmentKey {
    static let defaultValue = BolsilloTheme.light
}

public extension EnvironmentValues {
    var bolsilloTheme: BolsilloTheme {
        get { self[BolsilloThemeKey.self] }
        set { self[BolsilloThemeKey.self] = newValue }
    }
}
