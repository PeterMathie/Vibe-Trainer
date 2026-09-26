package com.petermathie.vibecheck

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibecheck.data.local.TrackerEntity
import com.petermathie.vibecheck.data.local.VibeDatabase
import com.petermathie.vibecheck.ui.EditorViewModel
import com.petermathie.vibecheck.ui.TrackerScreen
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
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

    @Test(timeout = 120_000)
    fun configureAndRecordChoiceField() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        runBlocking {
            database.trackerDao().insertTrackers(listOf(TrackerEntity("tracker", "Wellbeing", false)))
            database.trackerDao().insertFields(
                listOf(
                    com.petermathie.vibecheck.data.local.TrackerFieldEntity(
                        id = "mood",
                        trackerId = "tracker",
                        name = "Feeling",
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
        compose.setContent { VibeCheckTheme { TrackerScreen(viewModel) } }

        compose.onNodeWithText("Date (YYYY-MM-DD)").assertDoesNotExist()
        compose.onNodeWithContentDescription("Wellbeing colour").assertDoesNotExist()
        compose.onNodeWithContentDescription("Set Wellbeing colour 2").assertDoesNotExist()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithContentDescription("Edit Wellbeing settings", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Edit Wellbeing settings", useUnmergedTree = true).performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Change colour").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Set Wellbeing icon Mood").assertDoesNotExist()
        compose.onNodeWithText("Choose icon").performClick()
        compose.onNodeWithContentDescription("Set Wellbeing icon Mood").performScrollTo().performClick()
        compose.onNodeWithText("Change colour").performClick()
        compose.onNodeWithContentDescription("Set Wellbeing colour 2").performClick()
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().trackers().first().single().let {
                    it.colourArgb == 0xFF42A5F5L && it.iconName == "mood"
                }
            }
        }
        compose.onNodeWithContentDescription("Edit Wellbeing settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Choose from a list").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Choice shade scale").assertDoesNotExist()
        compose.onNodeWithText("Drag a choice across either line to change its shade.").assertDoesNotExist()
        compose.onNodeWithText("What would you like to track?").assertDoesNotExist()
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Choose…").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Choose…").performClick()
        compose.onNodeWithContentDescription("Sad, light shade").assertExists()
        compose.onNodeWithContentDescription("Okay, medium shade").assertExists()
        compose.onNodeWithContentDescription("Happy, dark shade").assertExists()
        compose.onNodeWithText("Happy").performClick()
        compose.onNodeWithText("Save daily total").assertDoesNotExist()

        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().values().first().any { it.textValue == "Happy" } }
        }
        val field = runBlocking { database.editorDao().fields().first().single() }
        assertEquals("Sad\nOkay\nHappy", field.choiceOptions)
        assertEquals(0, field.choiceLightThrough)
        assertEquals(2, field.choiceDarkFrom)
        compose.onNodeWithContentDescription("Edit Wellbeing settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Choose from a list").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Choice 3").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("What would you like to track?").assertDoesNotExist()
        compose.onNodeWithText("Heat-map intensity").assertDoesNotExist()
        compose.onNodeWithText("Archive habit").assertDoesNotExist()
        val actions: List<androidx.compose.ui.semantics.CustomAccessibilityAction> =
            compose.onNodeWithContentDescription("Reorder Happy")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals(true, actions.first { it.label == "Move earlier" }.action())
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Add choice").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Choice 3").performTextInput("Great")
        val greatActions: List<androidx.compose.ui.semantics.CustomAccessibilityAction> =
            compose.onNodeWithContentDescription("Reorder Great")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals(true, greatActions.first { it.label == "Move later" }.action())
        compose.onNodeWithContentDescription("Low to medium boundary").assertExists()
        compose.onNodeWithContentDescription("Medium to strong boundary").assertExists()
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().fields().first().single().let {
                    it.choiceOptions == "Sad\nHappy\nOkay\nGreat" &&
                        it.choiceLightThrough == 0 &&
                        it.choiceDarkFrom == 3
                }
            }
        }
        compose.onNodeWithContentDescription("Edit Wellbeing settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Delete habit permanently").assertDoesNotExist()
        compose.onNodeWithText("Archive").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Archived habits").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Restore").performClick()
        compose.onNodeWithText("Wellbeing").assertIsDisplayed()
    }

    @Test(timeout = 120_000)
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
        compose.setContent { VibeCheckTheme { TrackerScreen(viewModel) } }

        compose.waitUntil(15_000) {
            compose.onAllNodesWithContentDescription("Edit Disposable settings", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Edit Disposable settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Delete habit permanently").assertDoesNotExist()
        compose.onNodeWithText("Archive").performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Archived habits").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Delete").performClick()
        compose.onNodeWithText("Delete permanently").performClick()
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().trackers().first().isEmpty() }
        }
        compose.onNodeWithText("Disposable").assertDoesNotExist()
    }
}
