import BolsilloDomain
import SwiftUI

/// Placeholder for the fast-expense-recording screen (feature 001).
/// User-facing strings are injected by the caller (resolved from the app's String Catalog),
/// so this view holds no hard-coded user text (Constitution Article VII).
public struct RecordView: View {
    private let title: String
    private let saveTitle: String

    public init(title: String, saveTitle: String) {
        self.title = title
        self.saveTitle = saveTitle
    }

    public var body: some View {
        VStack(spacing: 16) {
            Text(title)
                .font(.title2)
            Button(saveTitle) {
                // TODO: wire to a RecordViewModel backed by a TransactionRepository port.
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
