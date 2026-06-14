import SwiftUI

public enum ConfidenceState {
    case waiting
    case low
    case confident
}

public struct ConfidenceVisual {
    public let icon: String
    public let fg: KeyPath<BolsilloColors, Color>
    public let container: KeyPath<BolsilloColors, Color>
    public let labelKey: String
    public let chipBorder: KeyPath<BolsilloColors, Color>?

    public init(
        icon: String,
        fg: KeyPath<BolsilloColors, Color>,
        container: KeyPath<BolsilloColors, Color>,
        labelKey: String,
        chipBorder: KeyPath<BolsilloColors, Color>? = nil
    ) {
        self.icon       = icon
        self.fg         = fg
        self.container  = container
        self.labelKey   = labelKey
        self.chipBorder = chipBorder
    }
}

public func confidenceState(_ confidence: Double, threshold: Double = 0.75) -> ConfidenceState {
    if confidence <= 0 { return .waiting }
    if confidence < threshold { return .low }
    return .confident
}

public func confidenceVisual(for state: ConfidenceState) -> ConfidenceVisual {
    switch state {
    case .waiting:
        return ConfidenceVisual(
            icon:      "sparkles",
            fg:        \.textMuted,
            container: \.fill,
            labelKey:  "record.ai.waiting"
        )
    case .low:
        return ConfidenceVisual(
            icon:      "info.circle",
            fg:        \.warning,
            container: \.warningContainer,
            labelKey:  "record.ai.toConfirm",
            chipBorder: \.warningBorder
        )
    case .confident:
        return ConfidenceVisual(
            icon:      "checkmark.circle.fill",
            fg:        \.success,
            container: \.successContainer,
            labelKey:  "record.ai.confident"
        )
    }
}
