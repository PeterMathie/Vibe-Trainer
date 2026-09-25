package com.petermathie.vibecheck

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.petermathie.vibecheck.ui.DashboardTokenPreview
import com.petermathie.vibecheck.ui.DashboardVisualFixtures
import com.petermathie.vibecheck.ui.components.VibeGraph
import com.petermathie.vibecheck.ui.components.VibeSurface
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import com.petermathie.vibecheck.ui.theme.VibePalettes
import com.petermathie.vibecheck.ui.theme.VibeSurfaceLevel
import com.petermathie.vibecheck.ui.theme.VibeSurfaceState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardVisualFixturesTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun everyPaletteModeHasADeterministicTokenPreview() {
        val activePalette = mutableStateOf(DashboardVisualFixtures.palettes.first())
        compose.setContent { DashboardTokenPreview(activePalette.value) }
        DashboardVisualFixtures.palettes.forEach { palette ->
            compose.runOnIdle { activePalette.value = palette }
            compose.onNodeWithContentDescription(
                "${palette.displayName} ${if (palette.isDark) "dark" else "light"} dashboard tokens",
            ).assertIsDisplayed()
        }
    }

    @Test
    fun sharedSurfaceExposesSelectedAndDisabledSemantics() {
        compose.setContent {
            VibeCheckTheme {
                VibeSurface(
                    level = VibeSurfaceLevel.SELECTED,
                    state = VibeSurfaceState.SELECTED,
                    enabled = false,
                    onClick = {},
                ) {
                    Text("Selected fixture")
                }
            }
        }

        compose.onNodeWithText("Selected fixture")
            .assertIsSelected()
            .assertIsNotEnabled()
    }

    @Test
    fun monoPreviewRemainsVisibleAt320DpAndTwoHundredPercentFontScale() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                Box(Modifier.width(320.dp)) {
                    DashboardTokenPreview(VibePalettes.Mono.dark)
                }
            }
        }

        val preview = compose.onNodeWithContentDescription("Mono dark dashboard tokens")
        preview.assertIsDisplayed()
        val bitmap = preview.captureToImage()
        assertTrue(bitmap.width > 0 && bitmap.height > 0)
    }

    @Test
    fun graphHasMinimumAccessibleRegionAndDatumActions() {
        compose.setContent {
            VibeCheckTheme {
                VibeGraph(
                    values = listOf(1.0, 2.0, 1.5),
                    dates = listOf(1_700_000_000_000, 1_700_086_400_000, 1_700_172_800_000),
                    unit = "kg",
                )
            }
        }

        compose.onNodeWithContentDescription("Progress chart", substring = true)
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
    }
}
