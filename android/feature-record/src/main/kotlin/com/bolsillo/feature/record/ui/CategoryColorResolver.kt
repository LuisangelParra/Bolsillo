package com.bolsillo.feature.record.ui

import com.bolsillo.designsystem.theme.CategoryColor
import com.bolsillo.designsystem.theme.categoryColor

/**
 * Resolves a taxonomy `categoryId` (from `shared-assets/taxonomy/category-taxonomy.json`)
 * → palette `colorToken` (from `shared-assets/design/tokens.json` → category)
 * → designsystem [categoryColor].
 *
 * Article VI: never key the palette directly off a taxonomy id.
 *
 * The mapping mirrors the taxonomy's `colorToken` field exactly; keep them in
 * lockstep when the taxonomy changes.
 */
object CategoryColorResolver {
    private val tokenForCategory: Map<String, String> =
        mapOf(
            "food" to "restaurantes",
            "food.groceries" to "mercado",
            "food.restaurants" to "restaurantes",
            "food.coffee" to "cafe",
            "transport" to "transporte",
            "transport.fuel" to "transporte",
            "transport.taxi" to "transporte",
            "transport.transit" to "transporte",
            "housing" to "vivienda",
            "housing.rent" to "vivienda",
            "utilities" to "servicios",
            "utilities.electricity" to "servicios",
            "utilities.internet" to "servicios",
            "health" to "salud",
            "leisure" to "ocio",
            "education" to "educacion",
            "shopping" to "ropa",
            "income" to "ingreso",
            "transfer" to "otros",
            "other" to "otros",
        )

    fun color(
        categoryId: String?,
        dark: Boolean,
    ): CategoryColor {
        val token = tokenForCategory[categoryId] ?: "otros"
        return categoryColor(token, dark)
    }
}
