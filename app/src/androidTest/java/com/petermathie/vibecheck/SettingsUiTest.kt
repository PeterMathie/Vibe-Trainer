package com.petermathie.vibecheck

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.petermathie.vibecheck.data.DemoRemovalSummary
import com.petermathie.vibecheck.data.local.VibeDatabase
import com.petermathie.vibecheck.ui.EditorViewModel
import com.petermathie.vibecheck.ui.SettingsScreen
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@android.annotation.SuppressLint("ViewModelConstructorInComposable")
class SettingsUiTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun permissionsUseSwitchesAndNoticesRenderMarkdown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        compose.setContent {
            VibeCheckTheme {
                SettingsScreen(EditorViewModel(database), onStyle = {}, onRemoveDemo = {})
            }
        }

        compose.onNodeWithText("Timer notifications").assertExists()
        compose.onNodeWithText("Precise background timers").assertExists()
        assertTrue(compose.onAllNodes(isToggleable()).fetchSemanticsNodes().size >= 7)
        compose.onNodeWithText("Enable timer notifications").assertDoesNotExist()
        compose.onNodeWithText("Allow precise background timers").assertDoesNotExist()

        compose.onNodeWithText("Open-source asset notices").performScrollTo().performClick()
        compose.onNodeWithText("Third-party notices").assertExists()
        compose.onNodeWithText("MuscleMap anatomy vectors").assertExists()
        compose.onNodeWithText("# Third-party notices").assertDoesNotExist()
        compose.onNodeWithText("**MuscleMap**", substring = true).assertDoesNotExist()
    }

    @Test
    fun demoRemovalRequiresConfirmationAndReportsExactResult() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        var invoked = false
        compose.setContent {
            VibeCheckTheme {
                SettingsScreen(
                    EditorViewModel(database),
                    onStyle = {},
                    onRemoveDemo = { report ->
                        invoked = true
                        report(Result.success(DemoRemovalSummary(2, 1, 3, 4, 5)))
                    },
                )
            }
        }

        compose.onNodeWithText("Remove demo data").performScrollTo().performClick()
        compose.onNodeWithText("Remove all demo personal data?").assertExists()
        compose.onNodeWithText("Your exercise catalogue and your own records stay intact.", substring = true).assertExists()
        compose.runOnIdle { assertFalse(invoked) }

        compose.onNode(hasText("Remove demo data") and hasAnyAncestor(isDialog())).performClick()

        compose.runOnIdle { assertTrue(invoked) }
        compose.onNodeWithText(
            "Removed demo personal data: 2 workouts, 1 programmes, 3 habits, 4 body entries and 5 photos.",
        ).assertExists()
    }
}
