import Testing
import SwiftUI
import BolsilloDesignSystem

// Note: ConfidenceState already conforms to Equatable in BolsilloDesignSystem.

@Suite struct ThemeTests {

    // MARK: Light / dark palette distinction

    @Test func lightAndDarkBackgroundsAreDistinct() {
        // Spot-check that the two palettes are not identical
        #expect(BolsilloTheme.light.colors.background != BolsilloTheme.dark.colors.background,
                "Light and dark backgrounds must differ")
    }

    @Test func lightAndDarkPrimaryColorsAreDistinct() {
        #expect(BolsilloTheme.light.colors.primary != BolsilloTheme.dark.colors.primary,
                "Light and dark primary colors must differ")
    }

    @Test func lightAndDarkSuccessColorsAreDistinct() {
        #expect(BolsilloTheme.light.colors.success != BolsilloTheme.dark.colors.success,
                "Light and dark success colors must differ")
    }

    // MARK: confidenceState() boundary conditions (Constitution Article V)

    @Test func confidenceZeroIsWaiting() {
        #expect(confidenceState(0.0) == .waiting, "confidence = 0 → .waiting")
    }

    @Test func confidenceBelowZeroIsWaiting() {
        #expect(confidenceState(-1.0) == .waiting, "confidence < 0 → .waiting")
    }

    @Test func confidenceSlightlyAboveZeroIsLow() {
        #expect(confidenceState(0.1) == .low, "confidence = 0.1 → .low")
    }

    @Test func confidenceJustBelowThresholdIsLow() {
        // Default threshold is 0.75
        #expect(confidenceState(0.749) == .low, "confidence just below threshold → .low")
    }

    @Test func confidenceAtThresholdIsConfident() {
        #expect(confidenceState(0.75) == .confident, "confidence = 0.75 (at threshold) → .confident")
    }

    @Test func confidenceAboveThresholdIsConfident() {
        #expect(confidenceState(1.0) == .confident, "confidence = 1.0 → .confident")
    }

    @Test func confidenceCustomThreshold() {
        // Verify custom threshold overrides the default
        #expect(confidenceState(0.5, threshold: 0.4) == .confident,
                "confidence = 0.5, threshold = 0.4 → .confident")
        #expect(confidenceState(0.3, threshold: 0.4) == .low,
                "confidence = 0.3, threshold = 0.4 → .low")
    }

    // MARK: Category color palette coverage

    @Test func categoryColorCoversAll13Tokens() {
        let tokens = [
            "cafe", "mercado", "restaurantes", "transporte", "ropa",
            "ocio", "salud", "vivienda", "servicios", "educacion",
            "viajes", "otros", "ingreso"
        ]
        for token in tokens {
            let color = CategoryColors.categoryColor(token: token, dark: false)
            #expect(color != nil, "Token '\(token)' must have a light-mode category color")
        }
    }

    @Test func categoryColorDarkPaletteCoversAll13Tokens() {
        let tokens = [
            "cafe", "mercado", "restaurantes", "transporte", "ropa",
            "ocio", "salud", "vivienda", "servicios", "educacion",
            "viajes", "otros", "ingreso"
        ]
        for token in tokens {
            let color = CategoryColors.categoryColor(token: token, dark: true)
            #expect(color != nil, "Token '\(token)' must have a dark-mode category color")
        }
    }

    @Test func unknownTokenReturnsNil() {
        let color = CategoryColors.categoryColor(token: "nonexistent-token", dark: false)
        #expect(color == nil, "Unknown token must return nil from categoryColor")
    }

    @Test func categoryColorLightAndDarkContainersDiffer() {
        // The light and dark containers for the same token must be different colors
        let light = CategoryColors.categoryColor(token: "cafe", dark: false)
        let dark  = CategoryColors.categoryColor(token: "cafe", dark: true)
        #expect(light != nil && dark != nil)
        #expect(light!.container != dark!.container,
                "Light and dark container colors for 'cafe' must differ")
    }

    // MARK: ConfidenceVisual consistency

    @Test func waitingVisualHasCorrectIcon() {
        let visual = confidenceVisual(for: .waiting)
        #expect(visual.icon == "sparkles", ".waiting visual must use 'sparkles' icon")
    }

    @Test func lowVisualHasCorrectIcon() {
        let visual = confidenceVisual(for: .low)
        #expect(visual.icon == "info.circle", ".low visual must use 'info.circle' icon")
    }

    @Test func confidentVisualHasCorrectIcon() {
        let visual = confidenceVisual(for: .confident)
        #expect(visual.icon == "checkmark.circle.fill", ".confident visual must use 'checkmark.circle.fill' icon")
    }

    @Test func lowVisualHasChipBorder() {
        let visual = confidenceVisual(for: .low)
        #expect(visual.chipBorder != nil, ".low confidence must have a chip border for visual warning")
    }

    @Test func waitingAndConfidentHaveNoChipBorder() {
        #expect(confidenceVisual(for: .waiting).chipBorder == nil,
                ".waiting confidence must not have a chip border")
        #expect(confidenceVisual(for: .confident).chipBorder == nil,
                ".confident confidence must not have a chip border")
    }
}
