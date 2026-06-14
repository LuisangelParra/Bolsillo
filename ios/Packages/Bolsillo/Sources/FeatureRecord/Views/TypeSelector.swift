import SwiftUI
import BolsilloDesignSystem

public struct TypeSelector: View {
    @Binding var selection: RecordEntryType
    @Environment(\.bolsilloTheme) var theme

    private let options: [RecordEntryType] = [.expense, .income, .transfer]

    public init(selection: Binding<RecordEntryType>) {
        self._selection = selection
    }

    public var body: some View {
        HStack(spacing: 0) {
            ForEach(options, id: \.self) { option in
                Button {
                    selection = option
                } label: {
                    Text(label(for: option))
                        .font(theme.typography.label)
                        .foregroundStyle(selection == option ? theme.colors.textPrimary : theme.colors.textMuted)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, theme.spacing.sm)
                        .background(
                            selection == option
                                ? theme.colors.surface.cornerRadius(theme.radius.control)
                                : Color.clear.cornerRadius(theme.radius.control)
                        )
                }
                .buttonStyle(.plain)
            }
        }
        .padding(theme.spacing.xs)
        .background(theme.colors.track)
        .cornerRadius(theme.radius.control)
    }

    private func label(for type: RecordEntryType) -> String {
        switch type {
        case .expense:  return String(localized: "record.expense", bundle: .module)
        case .income:   return String(localized: "record.income", bundle: .module)
        case .transfer: return String(localized: "record.transfer", bundle: .module)
        }
    }
}
