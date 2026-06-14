import Foundation

/// Banker's rounding (round-half-to-even) policy for money arithmetic.
///
/// For same-currency operations in feature 001 this is always an identity — there
/// are no fractional minor units. The policy and harness exist from day one so that
/// multi-currency cross-rate arithmetic (feature E8) plugs in without changing call
/// sites (Constitution Article III / Article IX).
public enum MoneyRounding {
    /// Round a single minor-unit value to nearest even on exact half.
    /// Same-currency: always identity since no fractional minor units exist.
    public static func round(_ value: Int) -> Int { value }

    /// Integer division with banker's rounding.
    ///
    /// Divides `numerator` by `denominator`, rounding the remainder using
    /// round-half-to-even (banker's rounding):
    /// - remainder < half  → truncate
    /// - remainder > half  → round away from zero
    /// - remainder == half → round to nearest even quotient
    public static func divide(_ numerator: Int, by denominator: Int) -> Int {
        guard denominator != 0 else { return 0 }
        let quotient  = numerator / denominator
        let remainder = abs(numerator % denominator)
        let absDenom  = abs(denominator)
        // Half-point in absolute minor units (×2 comparison avoids floating division)
        let doubleRemainder = remainder * 2
        if doubleRemainder < absDenom { return quotient }
        if doubleRemainder > absDenom {
            return quotient + (numerator.signum() * denominator.signum() > 0 ? 1 : -1)
        }
        // Exactly half → round to even
        if quotient % 2 == 0 { return quotient }
        return quotient + (numerator.signum() * denominator.signum() > 0 ? 1 : -1)
    }
}
