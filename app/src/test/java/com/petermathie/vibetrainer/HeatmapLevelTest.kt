package com.petermathie.vibetrainer

import com.petermathie.vibetrainer.ui.heatmapLevel
import com.petermathie.vibetrainer.ui.formatBodyweight
import org.junit.Assert.assertEquals
import org.junit.Test

class HeatmapLevelTest {
    @Test
    fun thresholdsMapValuesToLightMediumAndDark() {
        assertEquals(1, heatmapLevel(6.9, 7.0, 15.0))
        assertEquals(2, heatmapLevel(7.0, 7.0, 15.0))
        assertEquals(2, heatmapLevel(14.9, 7.0, 15.0))
        assertEquals(3, heatmapLevel(15.0, 7.0, 15.0))
        assertEquals(1, heatmapLevel(139.0, 140.0, 160.0))
        assertEquals(3, heatmapLevel(160.0, 140.0, 160.0))
    }

    @Test
    fun bodyweightAlwaysDisplaysTwoDecimalPlaces() {
        assertEquals("78.00", formatBodyweight(78.0))
        assertEquals("74.35", formatBodyweight(74.345))
    }
}
