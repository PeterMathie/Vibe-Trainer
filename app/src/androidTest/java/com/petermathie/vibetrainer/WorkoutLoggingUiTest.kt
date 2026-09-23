package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.TrainingRepository
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.WorkoutEditor
import com.petermathie.vibetrainer.ui.emptySet
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutLoggingUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun detailedEntryPersistsStackedBandsUnilateralValuesAndNotes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) }
        }

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Set details for Handstand set 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodes(hasText("Exercise notes") and hasSetTextAction())[0].performTextInput("Shoulders stable")
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().workoutExercises().first().any { it.notes == "Shoulders stable" } }
        }

        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasContentDescription("Set details for Bench press set 1"))
        compose.onNodeWithContentDescription("Set details for Bench press set 1").performClick()
        compose.onNodeWithText("Set details").assertExists()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("50 x 5")
        compose.onAllNodes(isRoot())[0].performTouchInput {
            swipeUp(startY = centerY + 500f, endY = centerY - 500f)
        }
        compose.waitUntil(15_000) {
            compose.onAllNodesWithContentDescription("Use Yellow band", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Use Yellow band", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Use Red band", useUnmergedTree = true).performClick()
        compose.onAllNodes(isRoot())[0].performTouchInput {
            swipeUp(startY = centerY + 500f, endY = centerY - 500f)
        }
        compose.onNode(hasText("Left reps") and hasSetTextAction()).performTextInput("4")
        compose.onNode(hasText("Right reps") and hasSetTextAction()).performTextInput("5")
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(15_000) { runBlocking { database.editorDao().sets().first().any { it.leftReps == 4 && it.rightReps == 5 } } }
        val saved = runBlocking { database.editorDao().sets().first().first { it.leftReps == 4 && it.rightReps == 5 } }
        assertEquals(4, saved.leftReps)
        assertEquals(5, saved.rightReps)
        assertEquals(2, runBlocking { database.editorDao().setBands().first().count { it.setId == saved.id } })
    }

    @Test
    fun holdResultCanBeCorrectedThroughTheSameDetailedEditor() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) } }

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Set details for Handstand set 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Set details for Handstand set 1").performClick()
        compose.onNodeWithContentDescription("Seconds").performTextInput("8")
        compose.onNodeWithText("Save").performClick()
        val handstandId = runBlocking { database.editorDao().workoutExercises().first().first { it.workoutId == workoutId && it.actualExerciseId == "core:handstand" }.id }
        compose.waitUntil(15_000) { runBlocking { database.editorDao().sets().first().any { it.workoutExerciseId == handstandId && it.holdMillis == 8_000L } } }

        compose.onNodeWithContentDescription("Set details for Handstand set 1").performClick()
        compose.onNodeWithContentDescription("Seconds").performTextClearance()
        compose.onNodeWithContentDescription("Seconds").performTextInput("10")
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(15_000) { runBlocking { database.editorDao().sets().first().any { it.workoutExerciseId == handstandId && it.holdMillis == 10_000L } } }
        assertEquals(0, runBlocking { database.editorDao().sets().first().count { it.workoutExerciseId == handstandId && it.holdMillis == 8_000L } })
    }

    @Test
    fun substitutionAfterARecordedSetPreservesOriginalHistory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val bench = runBlocking { database.editorDao().workoutExercises().first().first { it.workoutId == workoutId && it.actualExerciseId == "core:bench-press" } }
        runBlocking { database.editorDao().set(emptySet(bench.id, 1).copy(weightKg = 50.0, reps = 5)) }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) } }

        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasContentDescription("More actions for Bench", substring = true))
        compose.onNodeWithContentDescription("More actions for Bench", substring = true).performClick()
        compose.onNodeWithText("Substitute exercise").performScrollTo().performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Choose exercise").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasText("Name, alias or muscle") and hasSetTextAction()).performTextInput("Dip")
        compose.onNode(hasTextExactly("Dip") and hasClickAction()).performClick()

        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().workoutExercises().first().count { it.workoutId == workoutId } == 7
            }
        }
        assertEquals(2, runBlocking { database.editorDao().workoutExercises().first().count { it.workoutId == workoutId && it.actualExerciseId == "core:dip" } })
        assertEquals(1, runBlocking { database.editorDao().sets().first().count { it.workoutExerciseId == bench.id && it.reps == 5 } })
    }

    @Test
    fun compactLoggerRemainsReachableAtNarrowWidthAndLargeFont() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                VibeTrainerTheme {
                    Box(androidx.compose.ui.Modifier.width(320.dp)) {
                        WorkoutEditor(viewModel, workoutId, {}, {})
                    }
                }
            }
        }

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Set details for Handstand set 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasText("Finish workout"))
        compose.onNodeWithText("Finish workout").assertIsDisplayed()
    }

    @Test
    fun programmedRowsUseSeparateFieldsAndPlusAddsAnExtraSet() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) } }

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Set 1 for Handstand").fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasContentDescription("Set 1 for Bench press"))
        compose.onNodeWithContentDescription("Set 1 for Bench press").assertExists()
        compose.onNodeWithContentDescription("Set 2 for Bench press").assertExists()
        compose.onNodeWithContentDescription("Set 3 for Bench press").assertExists()
        compose.onNodeWithContentDescription("Set 4 for Bench press").assertDoesNotExist()
        compose.onNodeWithText("Add set").assertDoesNotExist()
        compose.onNodeWithContentDescription("Start 120 second rest for Bench press").assertExists()

        compose.onNodeWithContentDescription("Add set for Bench press").performClick()
        compose.onNodeWithContentDescription("Set 4 for Bench press").assertExists()
        compose.onNodeWithContentDescription("Resistance for Bench press set 1").performTextInput("60")
        compose.onNodeWithContentDescription("Reps for Bench press set 1").performTextInput("5")
        compose.onNodeWithContentDescription("RPE for Bench press set 1").performTextInput("8")
        compose.onNodeWithContentDescription("Save set 1 for Bench press").performClick()

        val benchId = runBlocking { database.editorDao().workoutExercises().first().first { it.workoutId == workoutId && it.actualExerciseId == "core:bench-press" }.id }
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().sets().first().any {
                    it.workoutExerciseId == benchId && it.weightKg == 60.0 && it.reps == 5 && it.rpe == 8.0
                }
            }
        }
    }

    private fun startPushWorkout(context: Context) = runBlocking {
        DatabaseSeeder(context, database).seedIfNeeded()
        val repository = TrainingRepository(database, database.programmeDao(), database.workoutDao(), database.trackerDao())
        repository.startWorkout(repository.observeProgrammeDays().first().first { it.id == "demo-day-push" }.id)
    }
}
