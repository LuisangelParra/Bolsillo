import CoreGraphics

public struct BolsilloRadius {
    public let iconTileSm: CGFloat  = 11
    public let iconTile: CGFloat    = 13
    public let iconTileLg: CGFloat  = 17
    public let control: CGFloat     = 16
    public let chip: CGFloat        = 16
    public let card: CGFloat        = 18
    public let cardLarge: CGFloat   = 20
    public let cardXL: CGFloat      = 22
    public let nav: CGFloat         = 26
    public let sheet: CGFloat       = 30
    public let frame: CGFloat       = 46
    // For truly pill-shaped views, use SwiftUI's Capsule() shape directly.
    public let full: CGFloat        = 9999

    public static let shared = BolsilloRadius()
    public init() {}
}
