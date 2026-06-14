import SwiftUI
import CoreText

public struct BolsilloTypography {
    public let displayBalance: Font
    public let displayAmount: Font
    public let moneyXL: Font
    public let moneyL: Font
    public let keypadDigit: Font
    public let titleXL: Font
    public let titleL: Font
    public let titleM: Font
    public let button: Font
    public let bodyStrong: Font
    public let amountRow: Font
    public let body: Font
    public let bodyValue: Font
    public let label: Font
    public let labelSmall: Font
    public let caption: Font
    public let badge: Font
    public let navLabel: Font
    public let overline: Font

    public static let shared = BolsilloTypography()

    public init() {
        displayBalance = Self.make(size: 46, weight: .heavy).monospacedDigit()
        displayAmount  = Self.make(size: 52, weight: .heavy).monospacedDigit()
        moneyXL        = Self.make(size: 27, weight: .heavy).monospacedDigit()
        moneyL         = Self.make(size: 26, weight: .heavy).monospacedDigit()
        keypadDigit    = Self.make(size: 23, weight: .bold)
        titleXL        = Self.make(size: 21, weight: .heavy)
        titleL         = Self.make(size: 18, weight: .heavy)
        titleM         = Self.make(size: 17, weight: .bold)
        button         = Self.make(size: 16, weight: .heavy)
        bodyStrong     = Self.make(size: 15.5, weight: .bold)
        amountRow      = Self.make(size: 15.5, weight: .heavy).monospacedDigit()
        body           = Self.make(size: 14.5, weight: .semibold)
        bodyValue      = Self.make(size: 14.5, weight: .bold)
        label          = Self.make(size: 13, weight: .semibold)
        labelSmall     = Self.make(size: 12.5, weight: .semibold)
        caption        = Self.make(size: 12, weight: .medium)
        badge          = Self.make(size: 11.5, weight: .bold)
        navLabel       = Self.make(size: 11, weight: .medium)
        overline       = Self.make(size: 12.5, weight: .bold)
    }

    private static func make(size: CGFloat, weight: Font.Weight) -> Font {
        let name = "PlusJakartaSans-\(weightName(weight))"
        #if canImport(UIKit)
        if UIFont(name: name, size: size) != nil {
            return Font.custom(name, size: size)
        }
        #elseif canImport(AppKit)
        if NSFont(name: name, size: size) != nil {
            return Font.custom(name, size: size)
        }
        #endif
        return Font.system(size: size, weight: weight, design: .rounded)
    }

    private static func weightName(_ weight: Font.Weight) -> String {
        switch weight {
        case .medium:    return "Medium"
        case .semibold:  return "SemiBold"
        case .bold:      return "Bold"
        case .heavy:     return "ExtraBold"
        default:         return "Regular"
        }
    }

    public static func registerFonts() {
        let names = [
            "PlusJakartaSans-Regular",
            "PlusJakartaSans-Medium",
            "PlusJakartaSans-SemiBold",
            "PlusJakartaSans-Bold",
            "PlusJakartaSans-ExtraBold",
        ]
        let bundle = Bundle.module
        for name in names {
            guard let url = bundle.url(forResource: name, withExtension: "ttf") else { continue }
            var error: Unmanaged<CFError>?
            CTFontManagerRegisterFontsForURL(url as CFURL, .process, &error)
        }
    }
}
