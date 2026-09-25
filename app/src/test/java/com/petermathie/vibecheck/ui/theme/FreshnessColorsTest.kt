package com.petermathie.vibecheck.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.petermathie.vibecheck.domain.model.MuscleRecencyBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshnessColorsTest {
    @Test
    fun interpolationUsesExactEndpointsAndMidpoint() {
        val from = Color(0xFF000000)
        val to = Color(0xFFFFFFFF)

        assertEquals(from, interpolateFreshnessColor(from, to, 0f))
        assertEquals(to, interpolateFreshnessColor(from, to, 1f))
        assertEquals(Color(0xFF636363), interpolateFreshnessColor(from, to, 0.5f))
    }

    @Test
    fun semanticScaleUsesThemeTokensAndMaintainsGraphicalContrast() {
        VibePalettes.presets.flatMap { listOf(it.light, it.dark) }.forEach { palette ->
            val colors = palette.freshnessColors()
            assertEquals(palette.recencyUnder24, colors.forBand(MuscleRecencyBand.UNDER_24_HOURS))
            assertEquals(palette.recency48To72, colors.forBand(MuscleRecencyBand.HOURS_48_TO_72))
            assertEquals(palette.recencyOver7, colors.forBand(MuscleRecencyBand.OVER_7_DAYS))
            assertEquals(palette.recencyNever, colors.forBand(MuscleRecencyBand.NEVER))
            listOf(
                colors.recentlyTrained,
                colors.recovering,
                colors.intermediate,
                colors.rested,
                colors.fresh,
                colors.noData,
            ).forEach { color ->
                assertTrue(contrastRatio(color, palette.surface) >= 3.0)
                assertTrue(contrastRatio(color, colors.selection) >= 3.0)
            }
        }
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = maxOf(first.luminance(), second.luminance()).toDouble()
        val darker = minOf(first.luminance(), second.luminance()).toDouble()
        return (lighter + 0.05) / (darker + 0.05)
    }
}
