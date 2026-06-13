package com.bolsillo.data.seed

import com.bolsillo.data.db.entity.CategoryEntity

/**
 * Seed mirrors shared-assets/taxonomy/category-taxonomy.json. `colorToken` maps
 * into shared-assets/design/tokens.json → category (Article VI shared taxonomy).
 * New transactions default to `other`.
 */
object CategorySeed {
    const val DEFAULT_CATEGORY_ID = "other"

    val ALL: List<CategoryEntity> =
        listOf(
            cat("food", "category.food", "fork.knife", "restaurantes"),
            cat("food.groceries", "category.food.groceries", "cart", "mercado"),
            cat("food.restaurants", "category.food.restaurants", "takeoutbag.and.cup.and.straw", "restaurantes"),
            cat("food.coffee", "category.food.coffee", "cup.and.saucer", "cafe"),
            cat("transport", "category.transport", "car", "transporte"),
            cat("transport.fuel", "category.transport.fuel", "fuelpump", "transporte"),
            cat("transport.taxi", "category.transport.taxi", "car.side", "transporte"),
            cat("transport.transit", "category.transport.transit", "bus", "transporte"),
            cat("housing", "category.housing", "house", "vivienda"),
            cat("housing.rent", "category.housing.rent", "key", "vivienda"),
            cat("utilities", "category.utilities", "bolt", "servicios"),
            cat("utilities.electricity", "category.utilities.electricity", "bolt.fill", "servicios"),
            cat("utilities.internet", "category.utilities.internet", "wifi", "servicios"),
            cat("health", "category.health", "cross.case", "salud"),
            cat("leisure", "category.leisure", "gamecontroller", "ocio"),
            cat("education", "category.education", "book", "educacion"),
            cat("shopping", "category.shopping", "bag", "ropa"),
            cat("income", "category.income", "arrow.down.circle", "ingreso"),
            cat("transfer", "category.transfer", "arrow.left.arrow.right", "otros"),
            cat("other", "category.other", "ellipsis.circle", "otros"),
        )

    private fun cat(
        id: String,
        nameKey: String,
        icon: String,
        token: String,
    ) = CategoryEntity(id = id, nameKey = nameKey, icon = icon, colorToken = token)
}
