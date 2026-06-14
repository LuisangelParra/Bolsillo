import Foundation

public extension Transaction {
    /// Signed amount for ledger arithmetic. Expense amounts are negative, income positive,
    /// transfer source negative, destination positive (Constitution Article III — Invariant 1).
    /// Amounts are already stored with correct sign; this property is the canonical accessor.
    var signedAmount: Money { amount }

    /// Magnitude for display (always positive). Use this in UI — never negate for presentation.
    var displayAmount: Money { Money(minorUnits: abs(amount.minorUnits)) }
}
