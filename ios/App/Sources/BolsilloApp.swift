import SwiftUI
import BolsilloDesignSystem
import FeatureRecord

@main
struct BolsilloApp: App {
    @State private var root: CompositionRoot? = nil
    @State private var initError: Error? = nil

    var body: some Scene {
        WindowGroup {
            BolsilloThemeProvider {
                Group {
                    if let root {
                        RecordView(model: RecordModel(
                            suggestCategoryAndAccount: root.suggestCategoryAndAccount,
                            recordTransaction: root.recordTransaction,
                            recordTransfer: root.recordTransfer,
                            undoLastRecord: root.undoLastRecord,
                            observeAccountBalances: root.observeAccountBalances,
                            editTransaction: root.editTransaction,
                            softDeleteTransaction: root.softDeleteTransaction,
                            restoreTransaction: root.restoreTransaction
                        ))
                    } else {
                        Color(hex: "#F4F3F6")
                            .ignoresSafeArea()
                            .onAppear { bootstrapApp() }
                    }
                }
            }
        }
    }

    @MainActor
    private func bootstrapApp() {
        do {
            root = try CompositionRoot()
        } catch {
            initError = error
        }
    }
}

private extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >>  8) & 0xFF) / 255
        let b = Double(int         & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}
