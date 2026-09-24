package com.petermathie.vibecheck

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibecheck.data.local.ExerciseEntity
import com.petermathie.vibecheck.data.local.ProgrammeDayEntity
import com.petermathie.vibecheck.data.local.ProgrammeEntity
import com.petermathie.vibecheck.data.local.ProgrammeExerciseEntity
import com.petermathie.vibecheck.data.local.VibeDatabase
import com.petermathie.vibecheck.data.local.WorkoutEntity
import com.petermathie.vibecheck.domain.model.TrainingMode
import com.petermathie.vibecheck.ui.EditorViewModel
import com.petermathie.vibecheck.ui.ProgrammeEditor
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
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
        setProgrammeContent(onModeChange = { selectedMode = it }) { day, _ -> startedDay = day }

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
        compose.onNodeWithContentDescription("Start Alpha").performClick()
        assertEquals("day-a", startedDay)
        compose.onNodeWithText("Bench press").assertDoesNotExist()
        compose.onNodeWithText("Alpha").performClick()
        compose.onNodeWithContentDescription("Read-only exercises for Alpha").assertIsDisplayed()
        compose.onNodeWithText("Bench press").assertIsDisplayed()
        compose.onNodeWithText("3 sets · 5–8 reps · 120s rest").assertIsDisplayed()
        compose.onNodeWithContentDescription("Edit targets for Bench press").assertDoesNotExist()
        compose.onNodeWithContentDescription("Edit programme Alpha").performClick()

        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
        compose.onNodeWithText("Back").assertDoesNotExist()
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
        compose.onNodeWithContentDescription("Edit targets for Bench press").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reorder Bench press").assertDoesNotExist()
        compose.onNodeWithText("Exercise settings").assertDoesNotExist()

        compose.onNodeWithText("Duplicate").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Delete").assertIsDisplayed()
        compose.onNodeWithText("Archive").assertIsDisplayed()
    }

    @Test
    fun draggingProgrammesDaysAndExercisesPersistsTheirOrder() {
        seedProgramme(includeSecondRows = true)
        setProgrammeContent { _, _ -> }

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
    fun stretchingIsOneWorkoutWithIndividuallyReorderableExercises() {
        seedStretchProgramme()
        setProgrammeContent(mode = TrainingMode.STRETCHING) { _, _ -> }

        compose.onNodeWithText("Stretching").performClick()
        compose.onNodeWithContentDescription("Read-only exercises for Stretching").assertIsDisplayed()
        compose.onNodeWithText("Front split").assertIsDisplayed()
        compose.onNodeWithText("Forward fold").assertIsDisplayed()

        compose.onNodeWithContentDescription("Edit programme Stretching").performClick()
        compose.onNodeWithText("Stretching").assertIsDisplayed()
        compose.onNodeWithText("Forward Fold").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reorder Front split").assertIsDisplayed()
        dragDown("Reorder Front split")
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().entries().first()
                    .filter { it.programmeDayId == "day-stretch" }
                    .sortedBy { it.position }
                    .map { it.id }
            } == listOf("entry-fold", "entry-split")
        }
    }

    @Test
    fun createRenameDuplicateArchiveAndDeleteRemainInEditContext() {
        createDatabase()
        runBlocking {
            database.editorDao().exercise(
                ExerciseEntity("new-exercise", "New exercise", "STRENGTH", "WEIGHT_REPS", null, null, "custom", true),
            )
        }
        setProgrammeContent { _, _ -> }

        compose.onNodeWithContentDescription("Add programme").performScrollTo().performClick()
        compose.onNode(hasSetTextAction()).performTextInput("My gym plan")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "My gym plan" } }

        val programmeId = activeProgrammes().single().id
        val dayId = runBlocking {
            database.editorDao().days().first().single { it.programmeId == programmeId }.id
        }
        compose.onNodeWithContentDescription("Add exercise").performClick()
        compose.onNodeWithText("New exercise").performClick()
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().entries().first().any {
                    it.programmeDayId == dayId && it.exerciseId == "new-exercise"
                }
            }
        }
        runBlocking {
            database.editorDao().workout(
                WorkoutEntity("historical", dayId, "Workout", "STRENGTH", "FINISHED", 1, 2, "", null, false),
            )
        }

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
            dao.exercise(
                ExerciseEntity(
                    "exercise-a", "Bench press", "STRENGTH", "WEIGHT_REPS", null, null, "custom", true,
                    targetSets = 3, targetRepsMin = 5, targetRepsMax = 8,
                ),
            )
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

    private fun seedStretchProgramme() {
        createDatabase()
        runBlocking {
            val dao = database.editorDao()
            dao.exercise(ExerciseEntity("stretch-split", "Front split", "STRETCHING", "ROM_MEASUREMENT", null, null, "custom", true))
            dao.exercise(ExerciseEntity("stretch-fold", "Forward fold", "STRETCHING", "ROM_MEASUREMENT", null, null, "custom", true))
            dao.programme(ProgrammeEntity("programme-stretch", "Stretching", "STRETCHING", false, position = 0))
            dao.day(ProgrammeDayEntity("day-stretch", "programme-stretch", "Stretching", 0))
            dao.entry(ProgrammeExerciseEntity("entry-split", "day-stretch", "stretch-split", 0, 3, null, null, null, 60, null, "", null))
            dao.entry(ProgrammeExerciseEntity("entry-fold", "day-stretch", "stretch-fold", 1, 3, null, null, null, 60, null, "", null))
        }
    }

    private fun setProgrammeContent(
        mode: TrainingMode = TrainingMode.STRENGTH,
        onModeChange: (TrainingMode) -> Unit = {},
        onStart: (String, Boolean) -> Unit,
    ) {
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeCheckTheme {
                ProgrammeEditor(viewModel, mode, onModeChange, onStart)
            }
        }
        compose.waitUntil(15_000) {
            val activeForMode = activeProgrammes().filter { it.mode == mode.name }
            activeForMode.isEmpty() || activeForMode.any { programme ->
                compose.onAllNodesWithText(programme.name).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }

    private fun activeProgrammes() = runBlocking {
        database.editorDao().programmes().first().filterNot { it.isArchived }
    }
}
