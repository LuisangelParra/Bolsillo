package com.bolsillo.domain.model

/**
 * Category. [nameKey] is a localization key (e.g. `category.food`), NEVER raw
 * text (Article VII). UI resolves it through string resources at render time.
 */
data class Category(
    val id: String,
    val nameKey: String,
    val icon: String,
    val colorToken: String,
)
