import SwiftUI

public struct BolsilloTheme {
    public let colors: BolsilloColors
    public let typography: BolsilloTypography
    public let spacing: BolsilloSpacing
    public let radius: BolsilloRadius
    public let elevation: BolsilloElevation
    public let gradients: BolsilloGradients

    public init(
        colors: BolsilloColors,
        typography: BolsilloTypography,
        spacing: BolsilloSpacing,
        radius: BolsilloRadius,
        elevation: BolsilloElevation,
        gradients: BolsilloGradients
    ) {
        self.colors     = colors
        self.typography = typography
        self.spacing    = spacing
        self.radius     = radius
        self.elevation  = elevation
        self.gradients  = gradients
    }

    public static let light = BolsilloTheme(
        colors:     .light,
        typography: .shared,
        spacing:    .shared,
        radius:     .shared,
        elevation:  .light,
        gradients:  .shared
    )

    public static let dark = BolsilloTheme(
        colors:     .dark,
        typography: .shared,
        spacing:    .shared,
        radius:     .shared,
        elevation:  .dark,
        gradients:  .shared
    )
}
