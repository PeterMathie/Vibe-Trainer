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
import com.petermathie.vibetrainer.domain.programme.ExerciseInputConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun workoutLoggerHidesSetConfigurationAndPersistsExerciseNotes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) }
        }

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Set 1 for Handstand").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Workout notes").assertDoesNotExist()
        compose.onNodeWithText("Start hold timer").assertDoesNotExist()
        compose.onNodeWithContentDescription("Save set 1 for Handstand").assertDoesNotExist()
        compose.onAllNodes(hasText("Exercise notes") and hasSetTextAction())[0].performTextInput("Shoulders stable")
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().workoutExercises().first().any { it.notes == "Shoulders stable" } }
        }

        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasContentDescription("Set 1 for Bench press"))
        compose.onNodeWithContentDescription("Set details for Bench press set 1").assertDoesNotExist()
        compose.onNodeWithText("Set details").assertDoesNotExist()
        assertTrue(compose.onAllNodesWithText("kg").fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithText("Resistance kg").assertDoesNotExist()
    }

    @Test
    fun holdResultCanBeCorrectedThroughTheSameWorkoutRow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) } }

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Time Under Tension for Handstand set 1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").performTextInput("8")
        val handstandId = runBlocking { database.editorDao().workoutExercises().first().first { it.workoutId == workoutId && it.actualExerciseId == "core:handstand" }.id }
        compose.waitUntil(15_000) { runBlocking { database.editorDao().sets().first().any { it.workoutExerciseId == handstandId && it.holdMillis == 8_000L } } }

        compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").performTextClearance()
        compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").performTextInput("10")

        compose.waitUntil(15_000) { runBlocking { database.editorDao().sets().first().any { it.workoutExerciseId == handstandId && it.holdMillis == 10_000L } } }
        assertEquals(0, runBlocking { database.editorDao().sets().first().count { it.workoutExerciseId == handstandId && it.holdMillis == 8_000L } })
    }

    @Test
    fun handstandCanTrackFreestandingAndTotalTensionTime() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        runBlocking {
            DatabaseSeeder(context, database).seedIfNeeded()
            val handstand = database.editorDao().programmeEntries("demo-day-push")
                .first { it.exerciseId == "core:handstand" }
            database.editorDao().entry(
                handstand.copy(
                    inputConfig = ExerciseInputConfig(
                        timeHeld = true,
                        timeUnderTension = true,
                    ).encode(),
                ),
            )
        }
        val workoutId = runBlocking {
            TrainingRepository(database, database.programmeDao(), database.workoutDao(), database.trackerDao(), context)
                .startWorkout("demo-day-push")
        }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) } }

        compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").performTextInput("12")
        compose.onNodeWithContentDescription("Total Time for Handstand set 1").performTextInput("30")
        compose.onNodeWithContentDescription("Open Total Time stopwatch for set 1").performClick()
        compose.onNodeWithText("Total Time stopwatch").assertExists()
        compose.onNodeWithText("Reset").performClick()
        compose.onNodeWithText("Start").performClick()
        Thread.sleep(150)
        compose.onNodeWithText("Stop").performClick()
        compose.onNodeWithText("Apply").performClick()
        compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").assertTextContains("TUT s")
        compose.onNodeWithContentDescription("Total Time for Handstand set 1").assertTextContains("Total s")
        compose.onNodeWithText("Freestanding sec").assertDoesNotExist()
        compose.onNodeWithText("Total wall sec").assertDoesNotExist()
        compose.onNodeWithContentDescription("RPE for Handstand set 1").performTextInput("99")
        compose.onNodeWithContentDescription("RPE for Handstand set 1").performTextInput("8")
        val heldBounds = compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").fetchSemanticsNode().boundsInRoot
        val tensionBounds = compose.onNodeWithContentDescription("Total Time for Handstand set 1").fetchSemanticsNode().boundsInRoot
        val rpeBounds = compose.onNodeWithContentDescription("RPE for Handstand set 1").fetchSemanticsNode().boundsInRoot
        assertTrue(kotlin.math.abs(heldBounds.center.y - tensionBounds.center.y) < 2f)
        assertTrue(kotlin.math.abs(heldBounds.center.y - rpeBounds.center.y) < 2f)

        val handstandId = runBlocking {
            database.editorDao().workoutExercises().first()
                .first { it.workoutId == workoutId && it.actualExerciseId == "core:handstand" }.id
        }
        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().sets().first().any {
                    it.workoutExerciseId == handstandId &&
                        it.holdMillis == 12_000L &&
                        it.timeUnderTensionMillis != null &&
                        it.timeUnderTensionMillis!! >= 100L &&
                        it.rpe == 8.0
                }
            }
        }
    }

    @Test
    fun handstandVariationSwitchesInputsAndSnapshotsItsConfiguration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        runBlocking {
            DatabaseSeeder(context, database).seedIfNeeded()
            database.editorDao().variations().first()
                .filter { it.exerciseId == "core:handstand" }
                .forEach { database.editorDao().variation(it.copy(targetSets = 1)) }
        }
        val workoutId = startPushWorkout(context)
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) } }

        compose.onNodeWithText("Wall handstand").performClick()
        compose.onNodeWithText("Freestanding handstand", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Time Under Tension for Handstand set 1").assertDoesNotExist()
        compose.onNodeWithContentDescription("Total Time for Handstand set 1").performTextInput("15")

        compose.waitUntil(15_000) {
            runBlocking {
                database.editorDao().sets().first().singleOrNull {
                    it.variationId == "handstand-free" &&
                        it.timeUnderTensionMillis == 15_000L &&
                        it.variationNameSnapshot == "Freestanding handstand" &&
                        it.variationInputConfigSnapshot.contains("timeHeld=false")
                } != null
            }
        }
        assertEquals(
            1,
            runBlocking { database.editorDao().sets().first().count { it.variationId == "handstand-free" } },
        )
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

        compose.waitUntil(15_000) { compose.onAllNodesWithContentDescription("Set 1 for Handstand").fetchSemanticsNodes().isNotEmpty() }
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

        compose.onNodeWithContentDescription("Add set for Bench press").performScrollTo().performClick()
        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasContentDescription("Set 4 for Bench press"))
        compose.onNodeWithContentDescription("Set 4 for Bench press").assertExists()
        compose.onNodeWithContentDescription("Resistance for Bench press set 1").performTextInput("60")
        compose.onNodeWithContentDescription("Reps for Bench press set 1").performTextInput("5")
        compose.onNodeWithContentDescription("RPE for Bench press set 1").performTextInput("8")
        compose.onNodeWithContentDescription("Save set 1 for Bench press").assertDoesNotExist()

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
        val repository = TrainingRepository(database, database.programmeDao(), database.workoutDao(), database.trackerDao(), context)
        repository.startWorkout(repository.observeProgrammeDays().first().first { it.id == "demo-day-push" }.id)
    }
}
