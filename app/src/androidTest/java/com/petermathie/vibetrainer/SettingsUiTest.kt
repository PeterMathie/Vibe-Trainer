package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.SettingsScreen
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

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
            VibeTrainerTheme {
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
}
