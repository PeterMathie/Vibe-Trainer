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
import com.petermathie.vibecheck.domain.tracker.HabitChoiceIntensity
import com.petermathie.vibecheck.domain.tracker.decodeHabitChoices
import com.petermathie.vibecheck.domain.tracker.encodeHabitChoices
import com.petermathie.vibecheck.domain.tracker.HabitChoiceOption
import com.petermathie.vibecheck.ui.HabitFieldDialog
import androidx.compose.ui.graphics.Color
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
        compose.onNodeWithText("Light · 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Medium · 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Dark · 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("What would you like to track?").assertDoesNotExist()
        compose.onNodeWithText("Archive habit").assertDoesNotExist()
        val actions: List<androidx.compose.ui.semantics.CustomAccessibilityAction> =
            compose.onNodeWithContentDescription("Happy intensity controls")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals(true, actions.first { it.label == "Move to Medium" }.action())
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Add choice").performScrollTo().performClick()
        compose.onNodeWithContentDescription("light choice 2").performTextInput("Great")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().fields().first().single().let {
                    val choices = decodeHabitChoices(it)
                    choices.first { option -> option.label == "Happy" }.intensity == HabitChoiceIntensity.MEDIUM &&
                        choices.first { option -> option.label == "Great" }.intensity == HabitChoiceIntensity.LIGHT
                }
            }
        }
        val historical = runBlocking { database.editorDao().values().first().single() }
        assertEquals("DARK", historical.choiceIntensity)
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

    @Test(timeout = 120_000)
    fun deletingChoicesAcrossBucketsAllowsEmptyGroupsWithoutStaleKeys() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        val options = listOf(
            HabitChoiceOption("light-only", "Light only", HabitChoiceIntensity.LIGHT, 0),
            HabitChoiceOption("medium-only", "Medium only", HabitChoiceIntensity.MEDIUM, 0),
            HabitChoiceOption("dark-a", "Dark A", HabitChoiceIntensity.DARK, 0),
            HabitChoiceOption("dark-b", "Dark B", HabitChoiceIntensity.DARK, 1),
            HabitChoiceOption("dark-c", "Dark C", HabitChoiceIntensity.DARK, 2),
        )
        val field = com.petermathie.vibecheck.data.local.TrackerFieldEntity(
            id = "choices",
            trackerId = "tracker",
            name = "Feeling",
            valueType = "CHOICE",
            unit = null,
            targetComparison = null,
            targetValue = null,
            position = 0,
            choiceOptions = options.joinToString("\n", transform = HabitChoiceOption::label),
            choiceOptionsJson = encodeHabitChoices(options),
        )
        runBlocking {
            database.trackerDao().insertTrackers(listOf(TrackerEntity("tracker", "Mood", false)))
            database.trackerDao().insertFields(listOf(field))
        }
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeCheckTheme {
                HabitFieldDialog(viewModel, field, Color(0xFF42A5F5), {})
            }
        }

        compose.onNodeWithContentDescription("Remove Light only").performScrollTo().performClick()
        compose.onNodeWithText("Light · 0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Remove Medium only").performScrollTo().performClick()
        compose.onNodeWithText("Medium · 0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Remove Dark A").performScrollTo().performClick()
        compose.onNodeWithText("Dark · 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(15_000) {
            runBlocking {
                decodeHabitChoices(database.editorDao().fields().first().single()).map {
                    Triple(it.id, it.intensity, it.position)
                }
            } == listOf(
                Triple("dark-b", HabitChoiceIntensity.DARK, 0),
                Triple("dark-c", HabitChoiceIntensity.DARK, 1),
            )
        }
    }
}
