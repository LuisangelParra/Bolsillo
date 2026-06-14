import BolsilloDomain

/// Seed catalog. USD and COP are essential (non-removable) and enabled by default
/// (Constitution Article VII). Other ISO 4217 currencies can be added later.
/// decimalDigits follow ISO 4217 (USD=2, COP=2).
public enum CurrencySeed {
    public static let usd = Currency(code: "USD", symbol: "$", decimalDigits: 2, isEnabled: true, isEssential: true)
    public static let cop = Currency(code: "COP", symbol: "$", decimalDigits: 2, isEnabled: true, isEssential: true)

    public static let essentials: [Currency] = [usd, cop]
}
