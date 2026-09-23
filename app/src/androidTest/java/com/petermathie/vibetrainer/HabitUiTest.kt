package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.TrackerEntity
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.TrackerScreen
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun configureAndRecordChoiceField() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        runBlocking { database.trackerDao().insertTrackers(listOf(TrackerEntity("tracker", "Wellbeing", false))) }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { TrackerScreen(viewModel) } }

        compose.onNodeWithContentDescription("Set Wellbeing colour 2").performClick()
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().trackers().first().single().colourArgb == 0xFF42A5F5L }
        }
        compose.onNode(hasText("Light below") and hasSetTextAction()).performTextClearance()
        compose.onNode(hasText("Light below") and hasSetTextAction()).performTextInput("8")
        compose.onNode(hasText("Medium below") and hasSetTextAction()).performTextClearance()
        compose.onNode(hasText("Medium below") and hasSetTextAction()).performTextInput("20")
        compose.onNodeWithText("Save heat-map shades").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().trackers().first().single().let {
                    it.heatmapLightBelow == 8.0 && it.heatmapMediumBelow == 20.0
                }
            }
        }
        compose.onNodeWithText("Add measurement").performScrollTo().performClick()
        compose.onNodeWithText("What would you like to track?").assertIsDisplayed()
        compose.onNodeWithContentDescription("Name, for example Duration or Protein").performTextInput("Mood")
        compose.onNodeWithText("Choose from a list").performClick()
        compose.onNodeWithContentDescription("Choices, separated by commas").performTextInput("Good, Bad")
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(15_000) { compose.onAllNodesWithText("Choose…").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Choose…").performClick()
        compose.onNodeWithText("Good").performClick()
        compose.onNodeWithText("Save daily total").performClick()

        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().values().first().any { it.textValue == "Good" } }
        }
        val field = runBlocking { database.editorDao().fields().first().single() }
        assertEquals("Good\nBad", field.choiceOptions)
    }
}
