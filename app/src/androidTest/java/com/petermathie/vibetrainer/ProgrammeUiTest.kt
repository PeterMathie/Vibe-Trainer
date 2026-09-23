package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.ExerciseEntity
import com.petermathie.vibetrainer.data.local.ProgrammeDayEntity
import com.petermathie.vibetrainer.data.local.ProgrammeEntity
import com.petermathie.vibetrainer.data.local.ProgrammeExerciseEntity
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.ProgrammeEditor
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgrammeUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun visibleEditOpensCoherentProgrammeEditorAndStartStillWorks() {
        seedProgramme()
        var startedDay: String? = null
        setProgrammeContent { startedDay = it }

        compose.onNodeWithText("Alpha").assertIsDisplayed()
        compose.onNodeWithText("Rename programme").assertDoesNotExist()
        compose.onNodeWithText("Duplicate").assertDoesNotExist()
        compose.onNodeWithText("Archive").assertDoesNotExist()
        compose.onNodeWithContentDescription("Edit programme Alpha").performClick()

        compose.onNodeWithText("Rename programme").assertIsDisplayed()
        compose.onNodeWithText("Duplicate").assertIsDisplayed()
        compose.onNodeWithText("Archive").assertIsDisplayed()
        compose.onNodeWithText("Morning").assertIsDisplayed()
        compose.onNodeWithText("Bench press").assertIsDisplayed()
        compose.onNodeWithText("3 sets · 5–8 reps · 120s rest").assertIsDisplayed()
        compose.onNodeWithText("Start workout").performClick()
        assertEquals("day-a", startedDay)

        compose.onNodeWithContentDescription("Edit workout Morning").performClick()
        compose.onNodeWithText("Rename workout").assertIsDisplayed()
        compose.onNodeWithText("Add exercise").assertIsDisplayed()
        compose.onNodeWithContentDescription("Edit targets for Bench press").assertIsDisplayed()
    }

    @Test
    fun draggingProgrammesDaysAndExercisesPersistsTheirOrder() {
        seedProgramme(includeSecondRows = true)
        setProgrammeContent {}

        dragDown("Reorder Alpha")
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().programmes().first().sortedBy { it.position }.map { it.id } } ==
                listOf("programme-b", "programme-a")
        }

        compose.onNodeWithContentDescription("Edit programme Alpha").performClick()
        dragDown("Reorder Morning")
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().days().first()
                    .filter { it.programmeId == "programme-a" }
                    .sortedBy { it.position }
                    .map { it.id }
            } == listOf("day-b", "day-a")
        }

        compose.onNodeWithContentDescription("Edit workout Morning").performScrollTo().performClick()
        dragDown("Reorder Bench press")
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().entries().first()
                    .filter { it.programmeDayId == "day-a" }
                    .sortedBy { it.position }
                    .map { it.id }
            } == listOf("entry-b", "entry-a")
        }
    }

    @Test
    fun createRenameDuplicateAndArchiveRemainInEditContext() {
        createDatabase()
        setProgrammeContent {}

        compose.onNodeWithText("Create programme").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("My gym plan")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "My gym plan" } }

        compose.onNodeWithContentDescription("Edit programme My gym plan").performClick()
        compose.onNodeWithText("Add workout").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Push")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Push").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("Rename programme").performClick()
        compose.onNode(hasSetTextAction()).performTextClearance()
        compose.onNode(hasSetTextAction()).performTextInput("Renamed plan")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "Renamed plan" } }

        compose.onNodeWithText("Duplicate").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "Renamed plan (copy)" } }
        compose.onNodeWithText("Archive").performClick()
        compose.waitUntil(15_000) { activeProgrammes().none { it.name == "Renamed plan" } }
    }

    private fun dragDown(description: String) {
        compose.onNodeWithContentDescription(description).performScrollTo().performTouchInput {
            down(center)
            advanceEventTime(1_000)
            repeat(5) {
                moveBy(Offset(0f, 60f))
                advanceEventTime(50)
            }
            up()
        }
    }

    private fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
    }

    private fun seedProgramme(includeSecondRows: Boolean = false) {
        createDatabase()
        runBlocking {
            val dao = database.editorDao()
            dao.exercise(ExerciseEntity("exercise-a", "Bench press", "STRENGTH", "WEIGHT_REPS", null, null, "custom", true))
            dao.exercise(ExerciseEntity("exercise-b", "Row", "STRENGTH", "WEIGHT_REPS", null, null, "custom", true))
            dao.programme(ProgrammeEntity("programme-a", "Alpha", "STRENGTH", false, position = 0))
            dao.day(ProgrammeDayEntity("day-a", "programme-a", "Morning", 0))
            dao.entry(ProgrammeExerciseEntity("entry-a", "day-a", "exercise-a", 0, 3, 5, 8, null, 120, null, "", null))
            if (includeSecondRows) {
                dao.programme(ProgrammeEntity("programme-b", "Beta", "STRENGTH", false, position = 1))
                dao.day(ProgrammeDayEntity("day-b", "programme-a", "Evening", 1))
                dao.entry(ProgrammeExerciseEntity("entry-b", "day-a", "exercise-b", 1, 3, 8, 10, null, 90, null, "", null))
            }
        }
    }

    private fun setProgrammeContent(onStart: (String) -> Unit) {
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme {
                ProgrammeEditor(viewModel, TrainingMode.STRENGTH, onStart)
            }
        }
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().isNotEmpty() || activeProgrammes().isEmpty() }
    }

    private fun activeProgrammes() = runBlocking {
        database.editorDao().programmes().first().filterNot { it.isArchived }
    }
}
