package com.petermathie.vibecheck.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VibePalettePresetTest {
    @Test
    fun exposesExactlyFourStablePresetIdsAndNormalizesLegacyValues() {
        assertEquals(listOf("ocean", "sunset", "forest", "mono"), VibePalettes.presets.map { it.id })
        listOf(
            null,
            "custom",
            "midnight-lime",
            "graphite-coral",
            "ocean-cyan",
            "plum-orchid",
            "amber-slate",
            "forest-mint",
            "unknown",
        ).forEach { assertEquals("ocean", VibePalettes.normalizeId(it)) }
    }

    @Test
    fun themeModeDefaultsToSystemAndPreservesExplicitChoices() {
        assertEquals(VibeThemeMode.SYSTEM, VibeThemeMode.fromPreference(null))
        assertEquals(VibeThemeMode.SYSTEM, VibeThemeMode.fromPreference("unknown"))
        assertEquals(VibeThemeMode.DARK, VibeThemeMode.fromPreference("dark"))
        assertEquals(VibeThemeMode.LIGHT, VibeThemeMode.fromPreference("light"))
        assertTrue(VibeThemeMode.SYSTEM.useDarkPalette(systemDark = true))
        assertTrue(!VibeThemeMode.SYSTEM.useDarkPalette(systemDark = false))
        assertTrue(VibeThemeMode.DARK.useDarkPalette(systemDark = false))
        assertTrue(!VibeThemeMode.LIGHT.useDarkPalette(systemDark = true))
    }

    @Test
    fun oceanDarkMapsDarkModernCoreRoles() {
        with(VibePalettes.Ocean.dark) {
            assertEquals(Color(0xFF181818), background)
            assertEquals(Color(0xFF252525), surface)
            assertEquals(Color(0xFF222222), surfaceRaised)
            assertEquals(Color(0xFF2B2B2B), border)
            assertEquals(Color(0xFF0078D4), accent)
            assertEquals(Color(0xFFD7D7D7), textPrimary)
            assertEquals(Color(0xFFC6C6C6), textSecondary)
            assertEquals(Color(0xFF868686), textFaint)
            assertEquals(Color(0xFFFF3B3B), danger)
        }
    }

    @Test
    fun lightAndDarkMaterialRolesMeetTextContrast() {
        VibePalettes.presets.flatMap { listOf(it.light, it.dark) }.forEach { palette ->
            assertContrast(palette.textPrimary, palette.background, 4.5)
            assertContrast(palette.textPrimary, palette.surface, 4.5)
            assertContrast(palette.onAccent, palette.accent, 4.5)
            assertContrast(palette.onSecondary, palette.secondary, 4.5)
            assertContrast(palette.onTertiary, palette.tertiary, 4.5)
            assertContrast(palette.onDanger, palette.danger, 4.5)
        }
    }

    private fun assertContrast(foreground: Color, background: Color, minimum: Double) {
        val lighter = maxOf(foreground.luminance(), background.luminance()).toDouble()
        val darker = minOf(foreground.luminance(), background.luminance()).toDouble()
        assertTrue((lighter + 0.05) / (darker + 0.05) >= minimum)
    }
}
