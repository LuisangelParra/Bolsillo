package com.bolsillo.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Palette keyed by token id from shared-assets/design/tokens.json → category.
// A transaction's categoryId is a TAXONOMY id; the taxonomy carries the
// `colorToken` that resolves into this palette (see CategoryColorResolver in
// :feature-record). Never key this palette directly off a taxonomy id.

@Immutable
data class CategoryColor(
    val foreground: Color,
    val container: Color,
)

object CategoryPalette {
    private val tokenIds =
        listOf(
            "cafe", "mercado", "restaurantes", "transporte", "ropa", "ocio",
            "salud", "vivienda", "servicios", "educacion", "viajes", "otros", "ingreso",
        )

    val all: Set<String> = tokenIds.toSet()

    fun light(token: String): CategoryColor =
        when (token) {
            "cafe" -> CategoryColor(Color(0xFFE0A12B), Color(0xFFFBEFD6))
            "mercado" -> CategoryColor(Color(0xFF16B364), Color(0xFFDCF5E9))
            "restaurantes" -> CategoryColor(Color(0xFFF2843B), Color(0xFFFCEBDD))
            "transporte" -> CategoryColor(Color(0xFF3B82F6), Color(0xFFE3EFFE))
            "ropa" -> CategoryColor(Color(0xFF7C5CF0), Color(0xFFEDE7FE))
            "ocio" -> CategoryColor(Color(0xFF2BA4D9), Color(0xFFDEF1FB))
            "salud" -> CategoryColor(Color(0xFFF0425A), Color(0xFFFDE6EC))
            "vivienda" -> CategoryColor(Color(0xFF12A89A), Color(0xFFDAF3F0))
            "servicios" -> CategoryColor(Color(0xFF7C5CF0), Color(0xFFEDE7FE))
            "educacion" -> CategoryColor(Color(0xFF3B82F6), Color(0xFFE3EFFE))
            "viajes" -> CategoryColor(Color(0xFF2BA4D9), Color(0xFFDEF1FB))
            "ingreso" -> CategoryColor(Color(0xFF16B364), Color(0xFFDCF5E9))
            else -> CategoryColor(Color(0xFF75757F), Color(0xFFECECEF))
        }

    fun dark(token: String): CategoryColor =
        when (token) {
            "cafe" -> CategoryColor(Color(0xFFE0A12B), Color(0xFF3A2E14))
            "mercado" -> CategoryColor(Color(0xFF16B364), Color(0xFF123226))
            "restaurantes" -> CategoryColor(Color(0xFFF2843B), Color(0xFF3A2614))
            "transporte" -> CategoryColor(Color(0xFF3B82F6), Color(0xFF14233A))
            "ropa" -> CategoryColor(Color(0xFF7C5CF0), Color(0xFF2E2748))
            "ocio" -> CategoryColor(Color(0xFF2BA4D9), Color(0xFF102E3A))
            "salud" -> CategoryColor(Color(0xFFF0425A), Color(0xFF3A1620))
            "vivienda" -> CategoryColor(Color(0xFF12A89A), Color(0xFF0E3330))
            "servicios" -> CategoryColor(Color(0xFF7C5CF0), Color(0xFF2E2748))
            "educacion" -> CategoryColor(Color(0xFF3B82F6), Color(0xFF14233A))
            "viajes" -> CategoryColor(Color(0xFF2BA4D9), Color(0xFF102E3A))
            "ingreso" -> CategoryColor(Color(0xFF16B364), Color(0xFF123226))
            else -> CategoryColor(Color(0xFF75757F), Color(0xFF2A2833))
        }
}

fun categoryColor(
    token: String,
    dark: Boolean,
): CategoryColor = if (dark) CategoryPalette.dark(token) else CategoryPalette.light(token)
