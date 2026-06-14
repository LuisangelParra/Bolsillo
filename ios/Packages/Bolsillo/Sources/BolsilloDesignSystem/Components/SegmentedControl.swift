import SwiftUI

/// Custom segmented control that matches Bolsillo's visual language.
/// Renders a pill-thumb sliding over a track, with animated selection.
public struct SegmentedControl<T: Hashable>: View {
    public let options: [(label: String, value: T)]
    @Binding public var selection: T

    @Environment(\.bolsilloTheme) private var theme
    @Namespace private var thumbNamespace

    public init(options: [(label: String, value: T)], selection: Binding<T>) {
        self.options    = options
        self._selection = selection
    }

    public var body: some View {
        HStack(spacing: 0) {
            ForEach(options.indices, id: \.self) { index in
                let option = options[index]
                let isSelected = option.value == selection

                Button {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                        selection = option.value
                    }
                } label: {
                    Text(option.label)
                        .font(isSelected ? theme.typography.bodyStrong : theme.typography.body)
                        .foregroundStyle(isSelected ? theme.colors.textPrimary : theme.colors.textMuted)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, theme.spacing.sm)
                        .background {
                            if isSelected {
                                RoundedRectangle(cornerRadius: theme.radius.control, style: .continuous)
                                    .fill(theme.colors.surface)
                                    .elevate(theme.elevation.segmentedThumb)
                                    .matchedGeometryEffect(id: "thumb", in: thumbNamespace)
                            }
                        }
                }
                .buttonStyle(.plain)
            }
        }
        .padding(theme.spacing.xs)
        .background(
            RoundedRectangle(cornerRadius: theme.radius.control + theme.spacing.xs, style: .continuous)
                .fill(theme.colors.track)
        )
    }
}
