import SwiftUI
import BolsilloDesignSystem
import BolsilloDomain

public struct TransferAccountsRow: View {
    @Binding var sourceAccountId: String
    @Binding var destAccountId: String
    let accounts: [Account]
    let sameAccountError: Bool
    @Environment(\.bolsilloTheme) var theme

    public init(
        sourceAccountId: Binding<String>,
        destAccountId: Binding<String>,
        accounts: [Account],
        sameAccountError: Bool
    ) {
        self._sourceAccountId = sourceAccountId
        self._destAccountId = destAccountId
        self.accounts = accounts
        self.sameAccountError = sameAccountError
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: theme.spacing.sm) {
            HStack(spacing: theme.spacing.sm) {
                accountPicker(
                    label: String(localized: "record.transfer.source", bundle: .module),
                    selection: $sourceAccountId
                )
                Image(systemName: "arrow.right")
                    .foregroundStyle(theme.colors.textMuted)
                accountPicker(
                    label: String(localized: "record.transfer.destination", bundle: .module),
                    selection: $destAccountId
                )
            }
            if sameAccountError {
                Text(String(localized: "record.transfer.sameAccountError", bundle: .module))
                    .font(theme.typography.caption)
                    .foregroundStyle(theme.colors.danger)
            }
        }
    }

    @ViewBuilder
    private func accountPicker(label: String, selection: Binding<String>) -> some View {
        Menu {
            ForEach(accounts) { account in
                Button(account.name) { selection.wrappedValue = account.id }
            }
        } label: {
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(theme.typography.caption)
                    .foregroundStyle(theme.colors.textMuted)
                Text(accounts.first(where: { $0.id == selection.wrappedValue })?.name ?? "—")
                    .font(theme.typography.bodyValue)
                    .foregroundStyle(theme.colors.textPrimary)
            }
            .padding(.horizontal, theme.spacing.base)
            .padding(.vertical, theme.spacing.sm)
            .background(theme.colors.surface)
            .overlay(
                RoundedRectangle(cornerRadius: theme.radius.chip)
                    .stroke(theme.colors.outline, lineWidth: 1)
            )
            .cornerRadius(theme.radius.chip)
        }
        .frame(maxWidth: .infinity)
    }
}
