import Foundation

/// Converts a raw digit string from the keypad into a `Money` value.
///
/// Uses integer math only — there is no Double/Float path (Constitution Article III).
/// The caller is responsible for supplying only digit characters; non-digit characters
/// are stripped defensively.
///
/// ## Minor-unit semantics
/// The digit string represents an amount already expressed in minor units.
/// For a 2-decimal-digit currency: "1234" → `Money(minorUnits: 1234)` → 12.34 display.
/// The display layer formats the value using `Currency.decimalDigits`.
public struct MoneyParser: Sendable {
    public init() {}

    /// Parse a string of digit characters into `Money`.
    ///
    /// - Parameters:
    ///   - digits: Raw keypad digits (e.g. "1234"). Non-digit characters are stripped.
    ///   - currency: The currency that determines decimal placement at display time.
    /// - Returns: `Money(minorUnits:)` for the parsed integer, or `.zero` on empty / invalid input.
    public func parse(digits: String, currency: Currency) -> Money {
        let raw = digits.filter(\.isNumber)
        guard !raw.isEmpty, let value = Int(raw) else { return .zero }
        return Money(minorUnits: value)
    }
}
