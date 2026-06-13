import Foundation

/// Monetary amount in **integer minor units** (e.g. cents). The number of minor
/// units a currency uses is defined by `Currency.decimalDigits`.
///
/// Constitution Article III: money is NEVER represented as Double/Float. This type
/// exposes no floating-point storage or accessor on purpose.
public struct Money: Equatable, Hashable, Sendable {
    public let minorUnits: Int

    public init(minorUnits: Int) {
        self.minorUnits = minorUnits
    }

    public static let zero = Money(minorUnits: 0)

    public var isZero: Bool { minorUnits == 0 }

    public static func + (lhs: Money, rhs: Money) -> Money {
        Money(minorUnits: lhs.minorUnits + rhs.minorUnits)
    }

    public static func - (lhs: Money, rhs: Money) -> Money {
        Money(minorUnits: lhs.minorUnits - rhs.minorUnits)
    }

    public static prefix func - (value: Money) -> Money {
        Money(minorUnits: -value.minorUnits)
    }
}
