package com.petermathie.vibecheck

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibecheck.ui.StyleScreen
import com.petermathie.vibecheck.ui.rememberVibePalette
import com.petermathie.vibecheck.data.BackupPreferences
import com.petermathie.vibecheck.ui.theme.LocalVibeReducedMotion
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class StyleUiTest {
    @get:Rule
    val compose = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences = context.getSharedPreferences("settings", 0)

    @After
    fun clearPreferences() {
        preferences.edit().clear().commit()
    }

    @Test
    fun customPaletteAndReducedMotionRefreshImmediatelyWithContrastProtection() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupPreferences.validate(
                """{"preferences":{"palette":"custom","accent":-1,"background":-1,"surface":-1}}""",
            )
        }
        preferences.edit().clear().commit()
        compose.setContent {
            val palette = rememberVibePalette(preferences)
            VibeCheckTheme(palette) {
                Column {
                    Text("Active accent ${palette.accent.toArgb()}")
                    Text("Reduced motion ${LocalVibeReducedMotion.current}")
                    StyleScreen(palette.id, { preferences.edit().putString("palette", it).apply() }, {})
                }
            }
        }
        compose.onNodeWithText(
            "Habit indicators and muscle-map colours stay independent.",
            substring = true,
        ).assertExists()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Custom palette"))

        replaceField("Accent hex", "#FFFF00")
        replaceField("Background hex", "#000000")
        replaceField("Surface hex", "#111111")
        compose.onNodeWithText("Apply custom palette").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Active accent -256").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Custom palette applied").assertExists()

        preferences.edit().putBoolean("reducedMotion", true).apply()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Reduced motion true").fetchSemanticsNodes().isNotEmpty() }

        compose.runOnIdle {
            BackupPreferences.restore(
                JSONObject("""{"palette":"custom","accent":-65281,"background":-16777216,"surface":-15658735}"""),
                preferences,
            )
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Active accent -65281").fetchSemanticsNodes().isNotEmpty() }

        replaceField("Background hex", "#FFFFFF")
        compose.onNodeWithText("Apply custom palette").performClick()
        compose.onNodeWithText("Background needs more contrast", substring = true).assertExists()
        compose.onNodeWithText("Active accent -65281").assertExists()
    }

    private fun replaceField(label: String, value: String) {
        compose.onNode(hasText(label) and hasSetTextAction()).performTextClearance()
        compose.onNode(hasText(label) and hasSetTextAction()).performTextInput(value)
    }
}
