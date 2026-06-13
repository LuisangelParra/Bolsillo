package com.bolsillo.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CategoryPaletteTest {
    @Test fun `palette covers all 13 token ids`() {
        assertEquals(13, CategoryPalette.all.size)
        CategoryPalette.all.forEach { token ->
            val light = categoryColor(token, dark = false)
            val dark = categoryColor(token, dark = true)
            // Each token resolves to a distinct fg+container in both modes (the
            // fallback path returns the otros palette).
            assertNotEquals(light.container, dark.container)
        }
    }

    @Test fun `unknown token falls back to neutral otros palette`() {
        val fallback = categoryColor("does-not-exist", dark = false)
        val otros = categoryColor("otros", dark = false)
        assertEquals(otros, fallback)
    }
}
