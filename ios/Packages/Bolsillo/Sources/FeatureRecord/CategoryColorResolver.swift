import BolsilloDesignSystem

/// Maps domain taxonomy category IDs (from shared-assets/taxonomy/category-taxonomy.json)
/// to BolsilloDesignSystem color tokens. Lives in FeatureRecord so it can import both
/// BolsilloDesignSystem and (optionally) BolsilloDomain without polluting the design system
/// with any domain knowledge (Constitution Article VIII).
public struct CategoryColorResolver {
    // MARK: Taxonomy ID → color token
    private static let tokenMap: [String: String] = [
        "food":               "restaurantes",
        "food.groceries":     "mercado",
        "food.restaurants":   "restaurantes",
        "food.coffee":        "cafe",
        "transport":          "transporte",
        "transport.fuel":     "transporte",
        "transport.taxi":     "transporte",
        "transport.transit":  "transporte",
        "housing":            "vivienda",
        "housing.rent":       "vivienda",
        "utilities":          "servicios",
        "utilities.electricity": "servicios",
        "utilities.internet": "servicios",
        "health":             "salud",
        "leisure":            "ocio",
        "education":          "educacion",
        "shopping":           "ropa",
        "income":             "ingreso",
        "transfer":           "otros",
        "other":              "otros",
    ]

    public static func categoryColor(categoryId: String, dark: Bool = false) -> CategoryColor? {
        guard let token = tokenMap[categoryId] else { return nil }
        return CategoryColors.categoryColor(token: token, dark: dark)
    }
}
