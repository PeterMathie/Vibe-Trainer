package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.TrainingRepository
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.WorkoutEditor
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
        val workoutId = runBlocking {
            DatabaseSeeder(context, database).seedIfNeeded()
            val repository = TrainingRepository(database, database.programmeDao(), database.workoutDao(), database.trackerDao())
            repository.startWorkout(repository.observeProgrammeDays().first().first { it.id == "demo-day-push" }.id)
        }
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme { WorkoutEditor(viewModel, workoutId, {}, {}) }
        }

        compose.waitUntil(15_000) { compose.onAllNodesWithText("Bands / details").fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodesWithText("More")[0].performClick()
        compose.onAllNodes(hasText("Exercise notes") and hasSetTextAction())[0].performTextInput("Shoulders stable")
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().workoutExercises().first().any { it.notes == "Shoulders stable" } }
        }
        compose.onAllNodesWithText("More")[0].performClick()

        compose.onAllNodes(hasScrollAction())[0].performScrollToNode(hasContentDescription("Set details for Bench", substring = true))
        compose.onNodeWithContentDescription("Set details for Bench", substring = true).performClick()
        compose.onNodeWithText("Set details").assertExists()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("50 x 5")
        compose.onNode(hasText("Left reps") and hasSetTextAction()).performTextInput("4")
        compose.onNode(hasText("Right reps") and hasSetTextAction()).performTextInput("5")
        compose.onAllNodes(isToggleable())[2].performClick()
        compose.onAllNodes(isToggleable())[3].performClick()
        compose.onNodeWithText("Save").performClick()

        compose.waitUntil(15_000) { runBlocking { database.editorDao().sets().first().any { it.leftReps == 4 && it.rightReps == 5 } } }
        val saved = runBlocking { database.editorDao().sets().first().first { it.leftReps == 4 && it.rightReps == 5 } }
        assertEquals(4, saved.leftReps)
        assertEquals(5, saved.rightReps)
        assertEquals(2, runBlocking { database.editorDao().setBands().first().count { it.setId == saved.id } })
    }
}
