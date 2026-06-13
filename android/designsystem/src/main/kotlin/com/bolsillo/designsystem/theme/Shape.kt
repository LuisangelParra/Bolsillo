package com.bolsillo.designsystem.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Radii sourced from shared-assets/design/tokens.json → radius.

@Immutable
data class BolsilloShapes(
    val iconTileSm: Shape,
    val iconTile: Shape,
    val iconTileLg: Shape,
    val control: Shape,
    val chip: Shape,
    val card: Shape,
    val cardLarge: Shape,
    val cardXL: Shape,
    val nav: Shape,
    val sheet: Shape,
    val frame: Shape,
    val full: Shape,
)

val DefaultBolsilloShapes =
    BolsilloShapes(
        iconTileSm = RoundedCornerShape(11.dp),
        iconTile = RoundedCornerShape(13.dp),
        iconTileLg = RoundedCornerShape(17.dp),
        control = RoundedCornerShape(16.dp),
        chip = RoundedCornerShape(16.dp),
        card = RoundedCornerShape(18.dp),
        cardLarge = RoundedCornerShape(20.dp),
        cardXL = RoundedCornerShape(22.dp),
        nav = RoundedCornerShape(26.dp),
        sheet = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        frame = RoundedCornerShape(46.dp),
        full = RoundedCornerShape(50),
    )

internal fun BolsilloShapes.toMaterialShapes(): Shapes =
    Shapes(
        small = control as CornerBasedShape,
        medium = card as CornerBasedShape,
        large = cardLarge as CornerBasedShape,
    )

val LocalBolsilloShapes = staticCompositionLocalOf { DefaultBolsilloShapes }
