import SwiftUI

public struct BolsilloColors {
    public let background: Color
    public let surface: Color
    public let surfaceAlt: Color
    public let surfaceInverse: Color
    public let onSurfaceInverse: Color

    public let primary: Color
    public let primaryContainer: Color
    public let onPrimary: Color
    public let onPrimaryContainer: Color
    public let primaryAccent: Color

    public let textPrimary: Color
    public let textSecondary: Color
    public let textMuted: Color
    public let textDisabled: Color

    public let outline: Color
    public let divider: Color
    public let track: Color
    public let fill: Color
    public let sheetHandle: Color

    public let success: Color
    public let successContainer: Color
    public let onSuccessContainer: Color

    public let danger: Color
    public let dangerContainer: Color
    public let onDangerContainer: Color

    public let warning: Color
    public let warningContainer: Color
    public let warningBorder: Color
    public let onWarningContainer: Color

    public let info: Color
    public let infoContainer: Color

    public let amountPositive: Color
    public let amountNegative: Color
    public let notificationDot: Color
    public let caret: Color

    public static let light = BolsilloColors(
        background:           Color(hex: "#F4F3F6"),
        surface:              Color(hex: "#FFFFFF"),
        surfaceAlt:           Color(hex: "#F7F6FA"),
        surfaceInverse:       Color(hex: "#16161B"),
        onSurfaceInverse:     Color(hex: "#FFFFFF"),
        primary:              Color(hex: "#7C5CF0"),
        primaryContainer:     Color(hex: "#EDE7FE"),
        onPrimary:            Color(hex: "#FFFFFF"),
        onPrimaryContainer:   Color(hex: "#6B4FD8"),
        primaryAccent:        Color(hex: "#B79CFF"),
        textPrimary:          Color(hex: "#16161C"),
        textSecondary:        Color(hex: "#56565F"),
        textMuted:            Color(hex: "#9A9AA4"),
        textDisabled:         Color(hex: "#C4C3CC"),
        outline:              Color(hex: "#E7E6EC"),
        divider:              Color(hex: "#F2F1F5"),
        track:                Color(hex: "#F0EFF4"),
        fill:                 Color(hex: "#E7E6EC"),
        sheetHandle:          Color(hex: "#D6D5DC"),
        success:              Color(hex: "#16B364"),
        successContainer:     Color(hex: "#DCF5E9"),
        onSuccessContainer:   Color(hex: "#0E8049"),
        danger:               Color(hex: "#F0425A"),
        dangerContainer:      Color(hex: "#FDE6EC"),
        onDangerContainer:    Color(hex: "#C32A40"),
        warning:              Color(hex: "#E0852B"),
        warningContainer:     Color(hex: "#FBEFD6"),
        warningBorder:        Color(hex: "#F0DBB0"),
        onWarningContainer:   Color(hex: "#B36514"),
        info:                 Color(hex: "#3B82F6"),
        infoContainer:        Color(hex: "#E3EFFE"),
        amountPositive:       Color(hex: "#16B364"),
        amountNegative:       Color(hex: "#F0425A"),
        notificationDot:      Color(hex: "#FF5470"),
        caret:                Color(hex: "#7C5CF0")
    )

    public static let dark = BolsilloColors(
        background:           Color(hex: "#121016"),
        surface:              Color(hex: "#1C1A22"),
        surfaceAlt:           Color(hex: "#242230"),
        surfaceInverse:       Color(hex: "#2A2833"),
        onSurfaceInverse:     Color(hex: "#FFFFFF"),
        primary:              Color(hex: "#9B7BF5"),
        primaryContainer:     Color(hex: "#2E2748"),
        onPrimary:            Color(hex: "#FFFFFF"),
        onPrimaryContainer:   Color(hex: "#CDBEFF"),
        primaryAccent:        Color(hex: "#B79CFF"),
        textPrimary:          Color(hex: "#F2F1F5"),
        textSecondary:        Color(hex: "#B6B5C0"),
        textMuted:            Color(hex: "#82808C"),
        textDisabled:         Color(hex: "#5A5862"),
        outline:              Color(hex: "#2F2D38"),
        divider:              Color(hex: "#26242E"),
        track:                Color(hex: "#2A2833"),
        fill:                 Color(hex: "#2F2D38"),
        sheetHandle:          Color(hex: "#3A3844"),
        success:              Color(hex: "#2FD17E"),
        successContainer:     Color(hex: "#123226"),
        onSuccessContainer:   Color(hex: "#8DEBBC"),
        danger:               Color(hex: "#FF6378"),
        dangerContainer:      Color(hex: "#3A1620"),
        onDangerContainer:    Color(hex: "#FFB3BF"),
        warning:              Color(hex: "#F0A94E"),
        warningContainer:     Color(hex: "#3A2E14"),
        warningBorder:        Color(hex: "#5C4A1E"),
        onWarningContainer:   Color(hex: "#FAD79A"),
        info:                 Color(hex: "#5C9CFF"),
        infoContainer:        Color(hex: "#14233A"),
        amountPositive:       Color(hex: "#2FD17E"),
        amountNegative:       Color(hex: "#FF6378"),
        notificationDot:      Color(hex: "#FF5470"),
        caret:                Color(hex: "#9B7BF5")
    )
}

private extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        let scanner = Scanner(string: hex)
        var rgb: UInt64 = 0
        scanner.scanHexInt64(&rgb)
        let r = Double((rgb >> 16) & 0xFF) / 255.0
        let g = Double((rgb >> 8)  & 0xFF) / 255.0
        let b = Double(rgb         & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}
