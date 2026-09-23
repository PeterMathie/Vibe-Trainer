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

        compose.onNodeWithText("Date (YYYY-MM-DD)").assertDoesNotExist()
        compose.onNodeWithContentDescription("Wellbeing colour").assertExists()
        compose.onNodeWithContentDescription("Set Wellbeing colour 2").assertDoesNotExist()
        compose.onNodeWithContentDescription("Edit Wellbeing settings").performClick()
        compose.onNodeWithText("Change colour").performClick()
        compose.onNodeWithContentDescription("Set Wellbeing colour 2").performClick()
        compose.onNodeWithContentDescription("Set Wellbeing icon Mood").performClick()
        compose.onNodeWithText("Save settings").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().trackers().first().single().let {
                    it.colourArgb == 0xFF42A5F5L && it.iconName == "mood"
                }
            }
        }
        compose.onNodeWithContentDescription("Edit Wellbeing settings").performClick()
        compose.onNodeWithText("Add measurement").performScrollTo().performClick()
        compose.onNodeWithText("What would you like to track?").assertIsDisplayed()
        compose.onNodeWithContentDescription("Name, for example Minutes or Protein").performTextInput("Mood")
        compose.onNodeWithText("Count").assertDoesNotExist()
        compose.onNodeWithText("Duration").assertDoesNotExist()
        compose.onNodeWithText("Rating").assertDoesNotExist()
        compose.onNodeWithText("Date and time").assertDoesNotExist()
        compose.onNodeWithText("Choose from a list").performClick()
        compose.onNode(hasText("Light") and hasSetTextAction()).performTextInput("Sad")
        compose.onNode(hasText("Medium") and hasSetTextAction()).performTextInput("Okay")
        compose.onNode(hasText("Dark") and hasSetTextAction()).performTextInput("Happy")
        val actions: List<androidx.compose.ui.semantics.CustomAccessibilityAction> =
            compose.onNodeWithContentDescription("Reorder Happy")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals(true, actions.first { it.label == "Move earlier" }.action())
        compose.waitForIdle()
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(15_000) { compose.onAllNodesWithText("Choose…").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Choose…").performClick()
        compose.onNodeWithText("Happy").performClick()
        compose.onNodeWithText("Save daily total").performClick()

        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().values().first().any { it.textValue == "Happy" } }
        }
        val field = runBlocking { database.editorDao().fields().first().single() }
        assertEquals("Sad\nHappy\nOkay", field.choiceOptions)
        assertEquals(0, field.choiceLightThrough)
        assertEquals(2, field.choiceDarkFrom)
        compose.onNodeWithContentDescription("Edit Wellbeing settings").performClick()
        compose.onNodeWithText("Choices map from light to dark in the order configured.").assertExists()
        compose.onNodeWithText("Delete habit permanently").assertDoesNotExist()
        compose.onNodeWithText("Archive habit").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Archived habits").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Restore").performClick()
        compose.onNodeWithText("Wellbeing").assertIsDisplayed()
    }

    @Test
    fun permanentlyDeletesEmptyHabit() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        runBlocking {
            database.trackerDao().insertTrackers(
                listOf(TrackerEntity("disposable", "Disposable", false)),
            )
        }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { TrackerScreen(viewModel) } }

        compose.onNodeWithContentDescription("Edit Disposable settings").performClick()
        compose.onNodeWithText("Delete habit permanently").performScrollTo().performClick()
        compose.onNodeWithText("Delete permanently").performClick()
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().trackers().first().isEmpty() }
        }
        compose.onNodeWithText("Disposable").assertDoesNotExist()
    }
}
