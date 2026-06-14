import SwiftUI
import BolsilloDesignSystem

/// Entry point for the fast-expense-recording feature (001).
/// Wraps `RecordScreen` in the theme provider so callers need only inject a `RecordModel`.
public struct RecordView: View {
    @State var model: RecordModel

    public init(model: RecordModel) {
        self.model = model
    }

    public var body: some View {
        BolsilloThemeProvider {
            RecordScreen(model: model)
        }
    }
}
