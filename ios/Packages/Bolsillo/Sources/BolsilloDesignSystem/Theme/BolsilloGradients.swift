import SwiftUI

public struct BolsilloGradients {
    // 162deg linear: #A98CF5 0% → #8862EE 48% → #6E45DF 100%
    public var hero: LinearGradient {
        LinearGradient(
            stops: [
                .init(color: Color(hex: "#A98CF5"), location: 0.00),
                .init(color: Color(hex: "#8862EE"), location: 0.48),
                .init(color: Color(hex: "#6E45DF"), location: 1.00),
            ],
            startPoint: UnitPoint(x: 0.5 - 0.5 * cos(toRad(162)), y: 0.5 - 0.5 * sin(toRad(162))),
            endPoint:   UnitPoint(x: 0.5 + 0.5 * cos(toRad(162)), y: 0.5 + 0.5 * sin(toRad(162)))
        )
    }

    // 135deg linear: #9B7BF5 0% → #7C5CF0 100%
    public var primary: LinearGradient {
        LinearGradient(
            colors: [Color(hex: "#9B7BF5"), Color(hex: "#7C5CF0")],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    // 150deg linear: #9B7BF5 0% → #7C5CF0 100%
    public var fab: LinearGradient {
        LinearGradient(
            colors: [Color(hex: "#9B7BF5"), Color(hex: "#7C5CF0")],
            startPoint: UnitPoint(x: 0.5 - 0.5 * cos(toRad(150)), y: 0.5 - 0.5 * sin(toRad(150))),
            endPoint:   UnitPoint(x: 0.5 + 0.5 * cos(toRad(150)), y: 0.5 + 0.5 * sin(toRad(150)))
        )
    }

    // 135deg linear: #FFD9A8 0% → #F2A65A 100%
    public var avatar: LinearGradient {
        LinearGradient(
            colors: [Color(hex: "#FFD9A8"), Color(hex: "#F2A65A")],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    // to-top: #FFFFFF 62% → transparent 100%
    public var navScrim: LinearGradient {
        LinearGradient(
            stops: [
                .init(color: Color.white, location: 0.62),
                .init(color: Color.white.opacity(0), location: 1.00),
            ],
            startPoint: .bottom,
            endPoint: .top
        )
    }

    public static let shared = BolsilloGradients()
    public init() {}
}

private func toRad(_ degrees: Double) -> Double { degrees * .pi / 180 }

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
