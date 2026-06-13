import Foundation

/// A currency in the catalog. `isEssential` currencies (USD, COP) are preloaded and
/// cannot be removed (Constitution Article VII). `decimalDigits` drives minor-unit handling.
public struct Currency: Equatable, Hashable, Sendable {
    public let code: String
    public let symbol: String
    public let decimalDigits: Int
    public let isEnabled: Bool
    public let isEssential: Bool

    public init(code: String, symbol: String, decimalDigits: Int, isEnabled: Bool, isEssential: Bool) {
        self.code = code
        self.symbol = symbol
        self.decimalDigits = decimalDigits
        self.isEnabled = isEnabled
        self.isEssential = isEssential
    }
}
