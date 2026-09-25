package com.petermathie.vibecheck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.petermathie.vibecheck.ui.DashboardTokenPreview
import com.petermathie.vibecheck.ui.DashboardVisualFixtures
import org.junit.Rule
import org.junit.Test

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
    }
}
