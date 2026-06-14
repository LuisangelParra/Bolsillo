import Foundation

/// A transaction category. `nameKey` is an i18n key resolved at display time —
/// never a raw user-facing string (Constitution Article VI — no hard-coded strings).
/// `colorToken` references a palette token id from `category-taxonomy.json`.
public struct Category: Equatable, Sendable, Identifiable {
    public let id: String
    public let nameKey: String      // localized i18n key (NOT raw text)
    public let icon: String
    public let colorToken: String   // palette token id from category-taxonomy.json

    public init(id: String, nameKey: String, icon: String, colorToken: String) {
        self.id = id
        self.nameKey = nameKey
        self.icon = icon
        self.colorToken = colorToken
    }
}
