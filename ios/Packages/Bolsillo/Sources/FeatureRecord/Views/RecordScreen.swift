import SwiftUI
import BolsilloDesignSystem
import BolsilloDomain

public struct RecordScreen: View {
    @Bindable var model: RecordModel
    let currency: Currency
    @Environment(\.bolsilloTheme) var theme

    public init(
        model: RecordModel,
        currency: Currency = Currency(
            code: "USD",
            symbol: "$",
            decimalDigits: 2,
            isEnabled: true,
            isEssential: true
        )
    ) {
        self.model = model
        self.currency = currency
    }

    public var body: some View {
        ZStack(alignment: .bottom) {
            theme.colors.background.ignoresSafeArea()

            ScrollView {
                VStack(spacing: theme.spacing.l) {
                    // Type selector
                    TypeSelector(selection: Binding(
                        get: { model.state.type },
                        set: { model.selectType($0) }
                    ))

                    // Amount display
                    AmountDisplay(
                        digits: model.state.digits,
                        currencySymbol: currency.symbol,
                        currencyCode: currency.code
                    )

                    // Category + Account chips (horizontal row) or transfer row
                    if model.state.type != .transfer {
                        HStack(spacing: theme.spacing.sm) {
                            categoryChip
                            accountChip
                        }
                    } else {
                        TransferAccountsRow(
                            sourceAccountId: Binding(
                                get: { model.state.accountId },
                                set: { model.selectAccount($0) }
                            ),
                            destAccountId: Binding(
                                get: { model.state.destAccountId },
                                set: { model.selectDestinationAccount($0) }
                            ),
                            accounts: model.accounts,
                            sameAccountError: model.state.sameAccountError
                        )
                    }

                    // AI confidence badge
                    AiConfidenceBadge(confidence: model.state.confidence)

                    // Mode tabs (Teclado active, Texto/Recibo inert)
                    ModeTabs()

                    // Keypad
                    AmountKeypad(
                        onDigit: { model.digitTapped($0) },
                        onBackspace: { model.backspaceTapped() }
                    )

                    // Save button
                    SaveButton(enabled: model.state.canSave) {
                        model.save(currency: currency)
                    }
                }
                .padding(.horizontal, theme.spacing.xxl)
                .padding(.top, theme.spacing.xxl)
                .padding(.bottom, 120)
            }

            // Undo toast overlay
            if model.state.transientEvent == .saved {
                UndoToast(
                    onUndo: { model.undo() },
                    onDismiss: { model.dismissUndo() }
                )
                .padding(.horizontal, theme.spacing.xl)
                .padding(.bottom, theme.spacing.xxl)
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .onAppear { model.onAppear() }
        .onDisappear { model.onDisappear() }
        .animation(.easeInOut(duration: 0.3), value: model.state.transientEvent)
    }

    @ViewBuilder
    private var categoryChip: some View {
        let catId = model.state.categoryId ?? "other"
        let colors = CategoryColorResolver.categoryColor(categoryId: catId, dark: false)
        CategoryChip(
            icon: "ellipsis.circle",
            label: catId,
            fg: colors?.fg ?? theme.colors.textMuted,
            container: colors?.container ?? theme.colors.fill,
            isLowConfidence: model.state.confidence > 0 && model.state.confidence < 0.75
        )
    }

    @ViewBuilder
    private var accountChip: some View {
        let name = model.accounts.first(where: { $0.id == model.state.accountId })?.name
        AccountChip(
            icon: "banknote",
            label: name ?? (model.state.accountId.isEmpty ? "—" : model.state.accountId)
        )
    }
}
