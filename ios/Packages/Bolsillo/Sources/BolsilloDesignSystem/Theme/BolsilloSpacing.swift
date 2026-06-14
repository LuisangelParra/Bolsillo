import CoreGraphics

public struct BolsilloSpacing {
    public let xxs: CGFloat = 2
    public let xs: CGFloat  = 4
    public let s: CGFloat   = 6
    public let sm: CGFloat  = 8
    public let m: CGFloat   = 10
    public let base: CGFloat = 12
    public let ml: CGFloat  = 14
    public let l: CGFloat   = 16
    public let xl: CGFloat  = 18
    public let xxl: CGFloat = 20
    public let x3l: CGFloat = 22
    public let x4l: CGFloat = 24
    public let x5l: CGFloat = 26
    public let x6l: CGFloat = 30

    public let screenPaddingX: CGFloat = 18
    public let sheetPaddingX: CGFloat  = 20

    public static let shared = BolsilloSpacing()
    public init() {}
}
