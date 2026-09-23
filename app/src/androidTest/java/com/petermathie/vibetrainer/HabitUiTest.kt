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
        runBlocking {
            database.trackerDao().insertTrackers(listOf(TrackerEntity("tracker", "Wellbeing", false)))
            database.trackerDao().insertFields(
                listOf(
                    com.petermathie.vibetrainer.data.local.TrackerFieldEntity(
                        id = "mood",
                        trackerId = "tracker",
                        name = "Mood",
                        valueType = "CHOICE",
                        unit = null,
                        targetComparison = null,
                        targetValue = null,
                        position = 0,
                        choiceOptions = "Sad\nOkay\nHappy",
                        choiceLightThrough = 0,
                        choiceDarkFrom = 2,
                    ),
                ),
            )
        }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { TrackerScreen(viewModel) } }

        compose.onNodeWithText("Date (YYYY-MM-DD)").assertDoesNotExist()
        compose.onNodeWithContentDescription("Wellbeing colour").assertDoesNotExist()
        compose.onNodeWithContentDescription("Set Wellbeing colour 2").assertDoesNotExist()
        compose.onNodeWithContentDescription("Edit Wellbeing settings").performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Change colour").fetchSemanticsNodes().isNotEmpty()
        }
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
        compose.onNodeWithText("Choice shade scale").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("What would you like to track?").assertDoesNotExist()
        compose.onNodeWithText("Save settings").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Choose…").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Choose…").performClick()
        compose.onNodeWithText("Happy").performClick()
        compose.onNodeWithText("Save daily total").assertDoesNotExist()

        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().values().first().any { it.textValue == "Happy" } }
        }
        val field = runBlocking { database.editorDao().fields().first().single() }
        assertEquals("Sad\nOkay\nHappy", field.choiceOptions)
        assertEquals(0, field.choiceLightThrough)
        assertEquals(2, field.choiceDarkFrom)
        compose.onNodeWithContentDescription("Edit Wellbeing settings").performClick()
        compose.onNodeWithText("Choice shade scale").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Choice 3").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("What would you like to track?").assertDoesNotExist()
        compose.onNodeWithText("Choices map from light to dark in the order configured.").assertExists()
        val actions: List<androidx.compose.ui.semantics.CustomAccessibilityAction> =
            compose.onNodeWithContentDescription("Reorder Happy")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals(true, actions.first { it.label == "Move earlier" }.action())
        compose.waitForIdle()
        compose.onNodeWithText("Add choice").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Choice 3").performTextInput("Great")
        val greatActions: List<androidx.compose.ui.semantics.CustomAccessibilityAction> =
            compose.onNodeWithContentDescription("Reorder Great")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals(true, greatActions.first { it.label == "Move later" }.action())
        compose.onNodeWithContentDescription("Light to medium boundary").assertExists()
        compose.onNodeWithContentDescription("Medium to dark boundary").assertExists()
        compose.onNodeWithText("Save settings").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().fields().first().single().let {
                    it.choiceOptions == "Sad\nHappy\nOkay\nGreat" &&
                        it.choiceLightThrough == 0 &&
                        it.choiceDarkFrom == 3
                }
            }
        }
        compose.onNodeWithContentDescription("Edit Wellbeing settings").performClick()
        compose.onNodeWithText("Delete habit permanently").assertDoesNotExist()
        compose.onNodeWithText("Archive habit").performScrollTo().performClick()
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
