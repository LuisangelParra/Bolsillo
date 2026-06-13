import FeatureRecord
import SwiftUI

struct ContentView: View {
    var body: some View {
        // Strings resolve from the app's String Catalog (Localizable.xcstrings),
        // default language Spanish (es), switchable to English (en).
        RecordView(
            title: String(localized: "record.title"),
            saveTitle: String(localized: "record.save")
        )
    }
}

#Preview {
    ContentView()
}
