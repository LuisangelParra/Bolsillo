import Foundation

public struct RecordState {
    public var digits: String = ""
    public var type: RecordEntryType = .expense
    public var categoryId: String? = nil
    public var accountId: String = ""
    public var destAccountId: String = ""
    public var confidence: Double = 0.0
    public var balances: [String: Int] = [:]
    public var transientEvent: TransientEvent? = nil
    public var lastSaved: LastSavedRef? = nil
    public var isSaving: Bool = false
    public var sameAccountError: Bool = false

    public var canSave: Bool {
        guard let intVal = Int(digits), intVal > 0 else { return false }
        if type == .transfer { return !accountId.isEmpty && !destAccountId.isEmpty && !sameAccountError }
        return !accountId.isEmpty
    }

    public var showSameAccountError: Bool { type == .transfer && sameAccountError }
}

public enum RecordEntryType: String, Sendable, CaseIterable, Equatable, Hashable {
    case expense, income, transfer
}

public enum TransientEvent: Sendable, Equatable {
    case saved
    case undone
    case validationError(String)

    public static func == (lhs: TransientEvent, rhs: TransientEvent) -> Bool {
        switch (lhs, rhs) {
        case (.saved, .saved): return true
        case (.undone, .undone): return true
        case (.validationError(let l), .validationError(let r)): return l == r
        default: return false
        }
    }
}

public enum LastSavedRef: Sendable {
    case single(id: String)
    case group(transferGroupId: String)
}
