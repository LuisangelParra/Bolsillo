import SwiftUI

public struct ShadowConfig {
    public let color: Color
    public let radius: CGFloat
    public let x: CGFloat
    public let y: CGFloat

    public init(color: Color, radius: CGFloat, x: CGFloat, y: CGFloat) {
        self.color  = color
        self.radius = radius
        self.x      = x
        self.y      = y
    }
}

public struct BolsilloElevation {
    public let e1: ShadowConfig
    public let key: ShadowConfig
    public let e2: ShadowConfig
    public let e2b: ShadowConfig
    public let e3: ShadowConfig
    public let e4: ShadowConfig
    public let nav: ShadowConfig
    public let fab: ShadowConfig
    public let buttonPrimary: ShadowConfig
    public let segmentedThumb: ShadowConfig
    public let toast: ShadowConfig

    public static let light = BolsilloElevation(
        e1:             ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.05),  radius: 10, x: 0, y: 3),
        key:            ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.05),  radius: 9,  x: 0, y: 3),
        e2:             ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.04),  radius: 14, x: 0, y: 4),
        e2b:            ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.04),  radius: 16, x: 0, y: 5),
        e3:             ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.05),  radius: 18, x: 0, y: 6),
        e4:             ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.06),  radius: 22, x: 0, y: 8),
        nav:            ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.10),  radius: 30, x: 0, y: 10),
        fab:            ShadowConfig(color: Color(red: 124/255, green: 92/255, blue: 240/255).opacity(0.50), radius: 26, x: 0, y: 10),
        buttonPrimary:  ShadowConfig(color: Color(red: 124/255, green: 92/255, blue: 240/255).opacity(0.35), radius: 24, x: 0, y: 10),
        segmentedThumb: ShadowConfig(color: Color(red: 28/255, green: 20/255, blue: 60/255).opacity(0.07),  radius: 6,  x: 0, y: 2),
        toast:          ShadowConfig(color: Color.black.opacity(0.30), radius: 30, x: 0, y: 12)
    )

    public static let dark = BolsilloElevation(
        e1:             ShadowConfig(color: Color.black.opacity(0.40), radius: 10, x: 0, y: 3),
        key:            ShadowConfig(color: Color.black.opacity(0.40), radius: 9,  x: 0, y: 3),
        e2:             ShadowConfig(color: Color.black.opacity(0.40), radius: 14, x: 0, y: 4),
        e2b:            ShadowConfig(color: Color.black.opacity(0.42), radius: 16, x: 0, y: 5),
        e3:             ShadowConfig(color: Color.black.opacity(0.45), radius: 18, x: 0, y: 6),
        e4:             ShadowConfig(color: Color.black.opacity(0.48), radius: 22, x: 0, y: 8),
        nav:            ShadowConfig(color: Color.black.opacity(0.55), radius: 30, x: 0, y: 10),
        fab:            ShadowConfig(color: Color(red: 124/255, green: 92/255, blue: 240/255).opacity(0.55), radius: 26, x: 0, y: 10),
        buttonPrimary:  ShadowConfig(color: Color(red: 124/255, green: 92/255, blue: 240/255).opacity(0.45), radius: 24, x: 0, y: 10),
        segmentedThumb: ShadowConfig(color: Color.black.opacity(0.40), radius: 6,  x: 0, y: 2),
        toast:          ShadowConfig(color: Color.black.opacity(0.60), radius: 30, x: 0, y: 12)
    )
}

public extension View {
    func elevate(_ config: ShadowConfig) -> some View {
        shadow(color: config.color, radius: config.radius, x: config.x, y: config.y)
    }
}
