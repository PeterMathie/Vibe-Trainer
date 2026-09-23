package com.petermathie.vibetrainer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.TrainingRepository
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.domain.model.SetDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutWorkflowTest {
    private lateinit var database: VibeDatabase
    private lateinit var repository: TrainingRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseSeeder(context, database).seedIfNeeded()
        repository = TrainingRepository(
            database,
            database.programmeDao(),
            database.workoutDao(),
            database.trackerDao(),
        )
    }

    @After
    fun tearDown() = database.close()

    @Test(timeout = 120_000)
    fun programmeToFinishedWorkoutUpdatesDerivedRecency() = runBlocking {
        val day = repository.observeProgrammeDays().first { it.isNotEmpty() }.first()
        val workoutId = repository.startWorkout(day.id)
        val draft = repository.observeDraft().first { it != null }
        assertNotNull(draft)

        val firstExercise = requireNotNull(draft).exercises.first()
        repository.addSet(firstExercise.id, SetDraft(reps = 5, notes = "Emulator workflow"))

        val autosaved = repository.observeDraft().first { active ->
            active?.exercises?.firstOrNull()?.sets?.isNotEmpty() == true
        }
        assertTrue(requireNotNull(autosaved).exercises.first().sets.isNotEmpty())

        repository.finishWorkout(workoutId)
        assertNull(repository.observeDraft().first())
        assertTrue(repository.observeMuscleRecency(day.mode).first { it.isNotEmpty() }.isNotEmpty())
    }

    @Test(timeout = 120_000)
    fun startingAnotherProgrammeRequiresExplicitReplacementAndUsesItsName() = runBlocking {
        val days = repository.observeProgrammeDays().first { it.size >= 3 }
        assertTrue(days.none { Regex("^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\b").containsMatchIn(it.name) })
        val push = days.first { it.id == "demo-day-push" }
        val legs = days.first { it.id == "demo-day-legs" }

        val pushId = repository.startWorkout(push.id)
        assertEquals(pushId, repository.startWorkout(legs.id))

        val legsId = repository.startWorkout(legs.id, replaceExisting = true)
        val active = requireNotNull(repository.observeDraft().first { it?.id == legsId })
        assertEquals("Legs + Mobility", active.name)
        assertTrue(active.exercises.any { it.exerciseName == "Back squat" })
        assertTrue(active.exercises.none { it.exerciseName == "Planche" })
    }
}
