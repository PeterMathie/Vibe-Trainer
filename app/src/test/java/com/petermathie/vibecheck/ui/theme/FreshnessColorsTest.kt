package com.petermathie.vibecheck.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.luminance
import com.petermathie.vibecheck.domain.model.MuscleRecencyBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.sqrt

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
            colors.ageStops.forEach { color ->
                assertTrue(
                    "${palette.id} ${if (palette.isDark) "dark" else "light"} ${hex(color)} " +
                        "has insufficient surface contrast ${contrastRatio(color, palette.surface)}",
                    contrastRatio(color, palette.surface) >= 3.0,
                )
                assertTrue(
                    "${palette.id} ${if (palette.isDark) "dark" else "light"} ${hex(color)} " +
                        "has insufficient selection contrast ${contrastRatio(color, colors.selection)}",
                    contrastRatio(color, colors.selection) >= 3.0,
                )
            }
            assertNotEquals(colors.noData, colors.ageStops.first())
            assertNotEquals(colors.noData, colors.ageStops.last())
        }
    }

    @Test
    fun allEightFreshnessScalesHaveDistinctAdjacentStopsAndSeparateNoData() {
        VibePalettes.presets.flatMap { listOf(it.light, it.dark) }.forEach { palette ->
            val colors = palette.freshnessColors()
            colors.ageStops.zipWithNext().forEachIndexed { index, (first, second) ->
                val distance = perceptualDistance(first, second)
                assertTrue(
                    "${palette.id} ${if (palette.isDark) "dark" else "light"} freshness " +
                        "stops $index/${index + 1} are too similar: ${hex(first)} -> ${hex(second)}, delta=$distance",
                    distance >= 0.075f,
                )
            }
            colors.ageStops.forEachIndexed { index, color ->
                val distance = perceptualDistance(color, colors.noData)
                assertTrue(
                    "${palette.id} ${if (palette.isDark) "dark" else "light"} no-data ${hex(colors.noData)} " +
                        "is too close to age stop $index ${hex(color)}: delta=$distance",
                    distance >= 0.065f,
                )
            }
            assertEquals(colors.recentlyTrained, colors.ageStops.first())
            assertEquals(colors.fresh, colors.ageStops.last())
            if (palette.id == "mono") {
                val luminance = colors.ageStops.map(Color::luminance)
                val monotonic = if (palette.isDark) {
                    luminance.zipWithNext().all { (first, second) -> first > second }
                } else {
                    luminance.zipWithNext().all { (first, second) -> first < second }
                }
                assertTrue("${palette.id} ${palette.isDark} Freshness luminance is not monotonic: $luminance", monotonic)
            } else {
                val hues = colors.ageStops.map(::hueDegrees)
                assertTrue(
                    "${palette.id} ${if (palette.isDark) "dark" else "light"} Freshness hues revisit: $hues",
                    hues.zipWithNext().all { (first, second) -> second - first >= 12f },
                )
                colors.ageStops.forEach {
                    val lab = it.convert(ColorSpaces.Oklab)
                    assertTrue("${palette.id} active stop ${hex(it)} is too neutral", sqrt(lab.green * lab.green + lab.blue * lab.blue) >= 0.04f)
                }
            }
        }
    }

    @Test
    fun noDataIsNeverBlendedAsAnAgeStop() {
        val colors = VibePalettes.Ocean.dark.freshnessColors()
        assertEquals(
            colors.noData,
            interpolateFreshnessBandColor(MuscleRecencyBand.NEVER, MuscleRecencyBand.UNDER_24_HOURS, 0.49f, colors),
        )
        assertEquals(
            colors.recentlyTrained,
            interpolateFreshnessBandColor(MuscleRecencyBand.NEVER, MuscleRecencyBand.UNDER_24_HOURS, 0.5f, colors),
        )
    }

    @Test
    fun habitHeatmapsHaveOrderedIntensityInEveryPresetAndMode() {
        VibePalettes.presets.flatMap { listOf(it.light, it.dark) }.forEach { palette ->
            val ramp = palette.habitHeatmapColors()
            val luminance = listOf(ramp.low, ramp.medium, ramp.strong).map(Color::luminance)
            val ordered = if (palette.isDark) {
                luminance[0] < luminance[1] && luminance[1] < luminance[2]
            } else {
                luminance[0] > luminance[1] && luminance[1] > luminance[2]
            }
            assertTrue(
                "${palette.id} ${if (palette.isDark) "dark" else "light"} heat-map ramp is not ordered: " +
                    "${listOf(ramp.low, ramp.medium, ramp.strong).map(::hex)} luminance=$luminance",
                ordered,
            )
            assertTrue(
                "${palette.id} heat-map levels need larger luminance steps: $luminance",
                kotlin.math.abs(luminance[0] - luminance[1]) >= 0.035f &&
                    kotlin.math.abs(luminance[1] - luminance[2]) >= 0.035f,
            )
            listOf(ramp.low, ramp.medium, ramp.strong).forEach {
                assertTrue(
                    "${palette.id} heat-map activity ${hex(it)} is too close to neutral ${hex(ramp.neutral)}",
                    perceptualDistance(it, ramp.neutral) >= 0.055f,
                )
            }
            listOf(ramp.neutral, ramp.low, ramp.medium, ramp.strong).forEach { fill ->
                val outline = palette.heatmapOutlineColor(fill)
                assertTrue(
                    "${palette.id} outline ${hex(outline)} is not visible on ${hex(fill)}",
                    contrastRatio(outline, fill) >= 3.0,
                )
            }
        }
    }

    @Test
    fun unreadableCustomHabitColoursFallBackToThePresetRamp() {
        val dark = VibePalettes.Ocean.dark
        val light = VibePalettes.Ocean.light
        assertEquals(dark.habitHeatmapColors(), dark.habitHeatmapColors(Color.Black))
        assertEquals(light.habitHeatmapColors(), light.habitHeatmapColors(Color.White))

        val custom = dark.habitHeatmapColors(Color(0xFFFFA000))
        assertTrue(custom.low.luminance() < custom.medium.luminance())
        assertTrue(custom.medium.luminance() < custom.strong.luminance())
        assertEquals(Color(0xFFFFA000), custom.strong)
    }

    private fun perceptualDistance(first: Color, second: Color): Float {
        val a = first.convert(ColorSpaces.Oklab)
        val b = second.convert(ColorSpaces.Oklab)
        return sqrt(
            (a.red - b.red) * (a.red - b.red) +
                (a.green - b.green) * (a.green - b.green) +
                (a.blue - b.blue) * (a.blue - b.blue),
        )
    }

    private fun hueDegrees(color: Color): Float {
        val lab = color.convert(ColorSpaces.Oklab)
        val degrees = Math.toDegrees(atan2(lab.blue, lab.green).toDouble()).toFloat()
        return if (degrees < 0f) degrees + 360f else degrees
    }

    private fun hex(color: Color): String = "#%02X%02X%02X".format(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
    )
}
