package com.petermathie.vibecheck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.material3.Text
import com.petermathie.vibecheck.ui.components.VibeSurface
import com.petermathie.vibecheck.ui.DashboardTokenPreview
import com.petermathie.vibecheck.ui.DashboardVisualFixtures
import org.junit.Rule
import org.junit.Test
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import com.petermathie.vibecheck.ui.theme.VibeSurfaceLevel
import com.petermathie.vibecheck.ui.theme.VibeSurfaceState

class DashboardVisualFixturesTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun everyPaletteModeHasADeterministicTokenPreview() {
        DashboardVisualFixtures.palettes.forEach { palette ->
            compose.setContent { DashboardTokenPreview(palette) }
            compose.onNodeWithContentDescription(
                "${palette.displayName} ${if (palette.isDark) "dark" else "light"} dashboard tokens",
            ).assertIsDisplayed()
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
    }
}
