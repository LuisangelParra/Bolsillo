package com.bolsillo.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfidenceVisualTest {
    @Test fun `waiting state at and below zero`() {
        assertEquals(ConfidenceState.Waiting, confidenceVisual(0.0, 0.75, LightBolsilloColors).state)
        assertEquals(ConfidenceState.Waiting, confidenceVisual(-0.1, 0.75, LightBolsilloColors).state)
    }

    @Test fun `low state just below threshold`() {
        assertEquals(ConfidenceState.Low, confidenceVisual(0.74, 0.75, LightBolsilloColors).state)
        assertEquals(ConfidenceState.Low, confidenceVisual(0.01, 0.75, LightBolsilloColors).state)
    }

    @Test fun `high state at and above threshold`() {
        assertEquals(ConfidenceState.High, confidenceVisual(0.75, 0.75, LightBolsilloColors).state)
        assertEquals(ConfidenceState.High, confidenceVisual(1.0, 0.75, LightBolsilloColors).state)
    }

    @Test fun `low state carries the warning border`() {
        val v = confidenceVisual(0.5, 0.75, LightBolsilloColors)
        assertEquals(LightBolsilloColors.warningBorder, v.chipBorder)
    }
}
