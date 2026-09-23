package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.local.ExerciseEntity
import com.petermathie.vibetrainer.data.local.WorkoutEntity
import com.petermathie.vibetrainer.data.local.WorkoutExerciseEntity
import com.petermathie.vibetrainer.data.local.WorkoutSetEntity
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.ProgressScreen
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun chartExposesAxesSkillExplanationAndExplicitRecords() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        runBlocking {
            DatabaseSeeder(context, database).seedIfNeeded()
            database.editorDao().exercise(
                ExerciseEntity("no-history", "No history exercise", "STRENGTH", "WEIGHT_REPS", null, null, "custom", true),
            )
            assertEquals(52, database.editorDao().workouts().first().count { it.id.startsWith("demo-progress-") })
        }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { ProgressScreen(viewModel) } }

        compose.onNodeWithText("Choose exercise").performClick()
        compose.onNode(hasText("Name, alias or muscle") and hasSetTextAction()).performTextInput("No history exercise")
        compose.onNode(hasText("No history exercise") and !hasSetTextAction()).assertDoesNotExist()
        compose.onNode(hasText("Name, alias or muscle") and hasSetTextAction()).performTextClearance()
        compose.onNode(hasText("Name, alias or muscle") and hasSetTextAction()).performTextInput("Handstand")
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Handstand").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasText("Handstand") and !hasSetTextAction()).performClick()

        compose.onNodeWithText("Variations").performClick()
        compose.onNodeWithText("Wall handstand").performClick()
        compose.onNodeWithText("Wall handstand").assertIsDisplayed()
        compose.onNodeWithText("Wall handstand").performClick()
        compose.onNodeWithText("All variations").performClick()
        compose.onNodeWithText("Variations").assertIsDisplayed()
        compose.onAllNodes(hasContentDescription("Progress chart", substring = true)).assertCountEquals(2)
        compose.onNodeWithText("Personal records").assertExists()
        compose.onNodeWithText("Best performance").assertExists()
        compose.onNodeWithText("Longest hold").assertExists()
        compose.onNodeWithText("Heaviest weight").assertDoesNotExist()
        compose.onNodeWithText("Estimated 1RM").assertDoesNotExist()
        compose.onNodeWithText("Repetition PR", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Calculated performance PR", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Skill index is a heuristic", substring = true).assertDoesNotExist()
        compose.onNodeWithContentDescription("How progress works").performClick()
        compose.onNodeWithText("Skill index is a heuristic", substring = true).assertExists()
        compose.onNodeWithText("Close").performClick()

        compose.onNodeWithText("Handstand").performClick()
        compose.onNode(hasText("Name, alias or muscle") and hasSetTextAction()).performTextInput("Bench press")
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Bench press").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasText("Bench press") and !hasSetTextAction()).performClick()
        compose.onNodeWithText("Heaviest weight").assertExists()
        compose.onNodeWithText("Estimated 1RM").assertExists()
        compose.onNodeWithText("Longest hold").assertDoesNotExist()
    }

    @Test
    fun eligiblePickerScrollsPastOneHundredExercises() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        runBlocking {
            val dao = database.editorDao()
            dao.workout(WorkoutEntity("workout", null, "Progress", "STRENGTH", "FINISHED", 1, 2, "", null, false))
            repeat(105) { index ->
                val suffix = index.toString().padStart(3, '0')
                val exerciseId = "exercise-$suffix"
                val rowId = "row-$suffix"
                dao.exercise(ExerciseEntity(exerciseId, "Eligible $suffix", "STRENGTH", "WEIGHT_REPS", null, null, "custom", true))
                dao.workoutExercise(WorkoutExerciseEntity(rowId, "workout", exerciseId, exerciseId, index, "", 60, null))
                dao.set(
                    WorkoutSetEntity(
                        id = "set-$suffix",
                        workoutExerciseId = rowId,
                        ordinal = 1,
                        setType = "WORKING",
                        result = "COMPLETED",
                        variationId = null,
                        weightKg = 10.0,
                        reps = 5,
                        holdMillis = null,
                        leftReps = null,
                        rightReps = null,
                        leftHoldMillis = null,
                        rightHoldMillis = null,
                        addedWeightKg = null,
                        assistanceKg = null,
                        rpe = null,
                        romValue = null,
                        romUnit = null,
                        notes = "",
                        loggedAt = 1,
                        updatedAt = 1,
                    ),
                )
            }
        }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { ProgressScreen(viewModel) } }
        compose.waitUntil(15_000) {
            runCatching {
                compose.onNodeWithText("Choose exercise").assertIsEnabled()
                true
            }.getOrDefault(false)
        }

        compose.onNodeWithText("Choose exercise").performClick()
        compose.onNodeWithContentDescription("Eligible progress exercises")
            .performScrollToNode(hasText("Eligible 104"))
        compose.onNodeWithText("Eligible 104").assertIsDisplayed()
    }
}
