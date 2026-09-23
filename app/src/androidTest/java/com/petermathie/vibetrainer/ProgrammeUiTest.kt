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
import com.petermathie.vibetrainer.data.local.WorkoutEntity
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.ProgrammeEditor
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        var selectedMode: TrainingMode? = null
        setProgrammeContent(onModeChange = { selectedMode = it }) { startedDay = it }

        compose.onNodeWithText("Alpha").assertIsDisplayed()
        val titleBounds = compose.onNodeWithText("Programmes").fetchSemanticsNode().boundsInRoot
        val modeBounds = compose.onNodeWithText("Strength").fetchSemanticsNode().boundsInRoot
        assertTrue(modeBounds.left > titleBounds.left)
        assertTrue(kotlin.math.abs(modeBounds.center.y - titleBounds.center.y) < titleBounds.height)
        compose.onNodeWithText("Stretch").performClick()
        assertEquals(TrainingMode.STRETCHING, selectedMode)
        compose.onNodeWithText("Rename programme").assertDoesNotExist()
        compose.onNodeWithText("Duplicate").assertDoesNotExist()
        compose.onNodeWithText("Archive").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reorder Alpha").assertDoesNotExist()
        compose.onNodeWithContentDescription("Edit programme Alpha").performClick()

        compose.onNodeWithText("Strength").assertDoesNotExist()
        compose.onNodeWithText("Add workout").assertDoesNotExist()
        compose.onNode(hasSetTextAction() and hasText("Alpha")).assertIsDisplayed()
        compose.onNodeWithText("Morning").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reorder Morning").assertDoesNotExist()
        compose.onNodeWithText("Bench press").assertIsDisplayed()
        compose.onNodeWithText("3 sets · 5–8 reps · 120s rest").assertIsDisplayed()
        compose.onNodeWithText("Start workout").performClick()
        assertEquals("day-a", startedDay)

        compose.onNodeWithContentDescription("Edit workout Morning").assertDoesNotExist()
        compose.onNodeWithText("Rename workout").assertDoesNotExist()
        compose.onNodeWithText("Delete workout").assertDoesNotExist()
        compose.onNodeWithText("Add exercise").assertDoesNotExist()
        compose.onNodeWithContentDescription("Add exercise").assertIsDisplayed()
        compose.onNodeWithContentDescription("Edit targets for Bench press").assertIsDisplayed()
        compose.onNodeWithContentDescription("Reorder Bench press").assertDoesNotExist()

        compose.onNodeWithText("Duplicate").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Delete").assertIsDisplayed()
        compose.onNodeWithText("Archive").assertIsDisplayed()
    }

    @Test
    fun draggingProgrammesDaysAndExercisesPersistsTheirOrder() {
        seedProgramme(includeSecondRows = true)
        setProgrammeContent {}

        val handleLeft = compose.onNodeWithContentDescription("Reorder Alpha").fetchSemanticsNode().boundsInRoot.left
        val titleLeft = compose.onNodeWithText("Alpha").fetchSemanticsNode().boundsInRoot.left
        assertTrue(handleLeft < titleLeft)
        dragDown("Reorder Alpha")
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().programmes().first().sortedBy { it.position }.map { it.id } } ==
                listOf("programme-b", "programme-a")
        }

        compose.onNodeWithContentDescription("Edit programme Alpha").performClick()
        val dayHandleLeft = compose.onNodeWithContentDescription("Reorder Morning").fetchSemanticsNode().boundsInRoot.left
        val dayTitleLeft = compose.onNodeWithText("Morning").fetchSemanticsNode().boundsInRoot.left
        assertTrue(dayHandleLeft < dayTitleLeft)
        dragDown("Reorder Morning")
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().days().first()
                    .filter { it.programmeId == "programme-a" }
                    .sortedBy { it.position }
                    .map { it.id }
            } == listOf("day-b", "day-a")
        }

        val entryHandleLeft = compose.onNodeWithContentDescription("Reorder Bench press").fetchSemanticsNode().boundsInRoot.left
        val entryTitleLeft = compose.onNodeWithText("Bench press").fetchSemanticsNode().boundsInRoot.left
        assertTrue(entryHandleLeft < entryTitleLeft)
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
    fun createRenameDuplicateArchiveAndDeleteRemainInEditContext() {
        createDatabase()
        setProgrammeContent {}

        compose.onNodeWithText("Create programme").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("My gym plan")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "My gym plan" } }

        val programmeId = activeProgrammes().single().id
        val dayId = "day-for-history"
        runBlocking {
            database.editorDao().day(ProgrammeDayEntity(dayId, programmeId, "Push", 0))
            database.editorDao().workout(
                WorkoutEntity("historical", dayId, "Push", "STRENGTH", "FINISHED", 1, 2, "", null, false),
            )
        }

        compose.onNodeWithContentDescription("Edit programme My gym plan").performClick()
        compose.onNodeWithText("Add workout").assertDoesNotExist()
        compose.onNode(hasSetTextAction()).performTextClearance()
        compose.onNode(hasSetTextAction()).performTextInput("Renamed plan")
        compose.onNodeWithContentDescription("Save programme name").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "Renamed plan" } }

        compose.onNodeWithText("Duplicate").performScrollTo().performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "Renamed plan (copy)" } }
        compose.onNodeWithText("Archive").performClick()
        compose.waitUntil(15_000) { activeProgrammes().none { it.name == "Renamed plan" } }
        compose.onNodeWithContentDescription("Edit programme Renamed plan (copy)").performClick()
        compose.onNodeWithText("Delete").performScrollTo().performClick()
        compose.waitUntil(15_000) { activeProgrammes().isEmpty() }
        assertEquals(listOf("historical"), runBlocking { database.editorDao().workouts().first().map { it.id } })
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

    private fun setProgrammeContent(
        onModeChange: (TrainingMode) -> Unit = {},
        onStart: (String) -> Unit,
    ) {
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme {
                ProgrammeEditor(viewModel, TrainingMode.STRENGTH, onModeChange, onStart)
            }
        }
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().isNotEmpty() || activeProgrammes().isEmpty() }
    }

    private fun activeProgrammes() = runBlocking {
        database.editorDao().programmes().first().filterNot { it.isArchived }
    }
}
