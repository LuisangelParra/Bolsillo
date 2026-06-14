import SwiftUI

public struct CategoryColor {
    public let fg: Color
    public let container: Color

    public init(fg: Color, container: Color) {
        self.fg        = fg
        self.container = container
    }
}

public enum CategoryColors {
    public static func categoryColor(token: String, dark: Bool = false) -> CategoryColor? {
        dark ? darkMap[token] : lightMap[token]
    }

    // MARK: Light palette
    private static let lightMap: [String: CategoryColor] = [
        "cafe": CategoryColor(
            fg:        Color(hex: "#E0A12B"),
            container: Color(hex: "#FBEFD6")
        ),
        "mercado": CategoryColor(
            fg:        Color(hex: "#16B364"),
            container: Color(hex: "#DCF5E9")
        ),
        "restaurantes": CategoryColor(
            fg:        Color(hex: "#F2843B"),
            container: Color(hex: "#FCEBDD")
        ),
        "transporte": CategoryColor(
            fg:        Color(hex: "#3B82F6"),
            container: Color(hex: "#E3EFFE")
        ),
        "ropa": CategoryColor(
            fg:        Color(hex: "#7C5CF0"),
            container: Color(hex: "#EDE7FE")
        ),
        "ocio": CategoryColor(
            fg:        Color(hex: "#2BA4D9"),
            container: Color(hex: "#DEF1FB")
        ),
        "salud": CategoryColor(
            fg:        Color(hex: "#F0425A"),
            container: Color(hex: "#FDE6EC")
        ),
        "vivienda": CategoryColor(
            fg:        Color(hex: "#12A89A"),
            container: Color(hex: "#DAF3F0")
        ),
        "servicios": CategoryColor(
            fg:        Color(hex: "#7C5CF0"),
            container: Color(hex: "#EDE7FE")
        ),
        "educacion": CategoryColor(
            fg:        Color(hex: "#3B82F6"),
            container: Color(hex: "#E3EFFE")
        ),
        "viajes": CategoryColor(
            fg:        Color(hex: "#2BA4D9"),
            container: Color(hex: "#DEF1FB")
        ),
        "otros": CategoryColor(
            fg:        Color(hex: "#75757F"),
            container: Color(hex: "#ECECEF")
        ),
        "ingreso": CategoryColor(
            fg:        Color(hex: "#16B364"),
            container: Color(hex: "#DCF5E9")
        ),
    ]

    // MARK: Dark palette
    private static let darkMap: [String: CategoryColor] = [
        "cafe": CategoryColor(
            fg:        Color(hex: "#E0A12B"),
            container: Color(hex: "#3A2E14")
        ),
        "mercado": CategoryColor(
            fg:        Color(hex: "#16B364"),
            container: Color(hex: "#123226")
        ),
        "restaurantes": CategoryColor(
            fg:        Color(hex: "#F2843B"),
            container: Color(hex: "#3A2614")
        ),
        "transporte": CategoryColor(
            fg:        Color(hex: "#3B82F6"),
            container: Color(hex: "#14233A")
        ),
        "ropa": CategoryColor(
            fg:        Color(hex: "#7C5CF0"),
            container: Color(hex: "#2E2748")
        ),
        "ocio": CategoryColor(
            fg:        Color(hex: "#2BA4D9"),
            container: Color(hex: "#102E3A")
        ),
        "salud": CategoryColor(
            fg:        Color(hex: "#F0425A"),
            container: Color(hex: "#3A1620")
        ),
        "vivienda": CategoryColor(
            fg:        Color(hex: "#12A89A"),
            container: Color(hex: "#0E3330")
        ),
        "servicios": CategoryColor(
            fg:        Color(hex: "#7C5CF0"),
            container: Color(hex: "#2E2748")
        ),
        "educacion": CategoryColor(
            fg:        Color(hex: "#3B82F6"),
            container: Color(hex: "#14233A")
        ),
        "viajes": CategoryColor(
            fg:        Color(hex: "#2BA4D9"),
            container: Color(hex: "#102E3A")
        ),
        "otros": CategoryColor(
            fg:        Color(hex: "#75757F"),
            container: Color(hex: "#2A2833")
        ),
        "ingreso": CategoryColor(
            fg:        Color(hex: "#16B364"),
            container: Color(hex: "#123226")
        ),
    ]
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
