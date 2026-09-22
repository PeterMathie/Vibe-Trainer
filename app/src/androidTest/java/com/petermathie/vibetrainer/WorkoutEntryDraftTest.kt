package com.petermathie.vibetrainer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.TrainingRepository
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.local.WorkoutEntryDraftEntity
import com.petermathie.vibetrainer.data.local.WorkoutSetEntity
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.domain.model.TrainingMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutEntryDraftTest {
    private val databaseName = "workout-entry-draft-test"
    private lateinit var context: Context
    private lateinit var database: VibeDatabase
    private lateinit var repository: TrainingRepository

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
        openDatabase()
        DatabaseSeeder(context, database).seedIfNeeded()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test(timeout = 120_000)
    fun unfinishedInputSurvivesDatabaseAndViewModelRecreation() = runBlocking {
        val (_, exerciseId) = startWorkout()
        val draft = pendingDraft(exerciseId).copy(
            performance = "72.5 x 6",
            rpe = "8",
            detailsOpen = true,
            warmUp = true,
            bandIds = "[\"band:red\"]",
            leftValue = "6",
        )
        database.editorDao().entryDraft(draft)

        database.close()
        openDatabase()

        val recovered = database.editorDao().entryDraft(exerciseId)
        assertEquals(draft, recovered)
    }

    @Test(timeout = 120_000)
    fun submissionConsumesDraftAndRetryDoesNotDuplicateSet() = runBlocking {
        val (_, exerciseId) = startWorkout()
        val draft = pendingDraft(exerciseId).copy(performance = "5", rpe = "7")
        val set = pendingSet(draft, reps = 5)
        val dao = database.editorDao()
        dao.entryDraft(draft)

        dao.consumeEntryDraft(set, emptyList())
        dao.consumeEntryDraft(set, emptyList())

        assertNull(dao.entryDraft(exerciseId))
        assertEquals(1, dao.sets().first().count { it.id == draft.setId })
    }

    @Test(timeout = 120_000)
    fun cancellationFinishAndDeletionClearPendingInputWithoutDerivedHistory() = runBlocking {
        val (workoutId, exerciseId) = startWorkout()
        val dao = database.editorDao()
        dao.entryDraft(pendingDraft(exerciseId).copy(performance = "10"))
        dao.deleteEntryDraft(exerciseId)
        assertNull(dao.entryDraft(exerciseId))

        dao.entryDraft(pendingDraft(exerciseId).copy(performance = "11"))
        val recencyBeforeFinish = repository.observeMuscleRecency(TrainingMode.STRENGTH).first()
        repository.finishWorkout(workoutId)
        assertNull(dao.entryDraft(exerciseId))
        dao.persistEntryDraft(pendingDraft(exerciseId).copy(performance = "stale write"))
        assertNull(dao.entryDraft(exerciseId))
        assertEquals(recencyBeforeFinish, repository.observeMuscleRecency(TrainingMode.STRENGTH).first())

        val (nextWorkoutId, nextExerciseId) = startWorkout()
        dao.entryDraft(pendingDraft(nextExerciseId).copy(performance = "12"))
        dao.deleteWorkout(nextWorkoutId)
        assertNull(dao.entryDraft(nextExerciseId))
    }

    private suspend fun startWorkout(): Pair<String, String> {
        val day = repository.observeProgrammeDays().first { it.isNotEmpty() }.first()
        val workoutId = repository.startWorkout(day.id)
        val exerciseId = requireNotNull(repository.observeDraft().first { it != null }).exercises.first().id
        return workoutId to exerciseId
    }

    private fun pendingDraft(exerciseId: String) = WorkoutEntryDraftEntity(
        workoutExerciseId = exerciseId,
        setId = "pending-set-$exerciseId",
        ordinal = 1,
        performance = "",
        rpe = "",
        detailsOpen = false,
        warmUp = false,
        failed = false,
        bandIds = "[]",
        variationId = null,
        leftValue = "",
        rightValue = "",
        addedWeight = "",
        assistance = "",
        romValue = "",
        romUnit = "cm",
        updatedAt = 1,
    )

    private fun pendingSet(draft: WorkoutEntryDraftEntity, reps: Int) = WorkoutSetEntity(
        id = draft.setId,
        workoutExerciseId = draft.workoutExerciseId,
        ordinal = draft.ordinal,
        setType = "WORKING",
        result = "COMPLETED",
        variationId = null,
        weightKg = null,
        reps = reps,
        holdMillis = null,
        leftReps = null,
        rightReps = null,
        leftHoldMillis = null,
        rightHoldMillis = null,
        addedWeightKg = null,
        assistanceKg = null,
        rpe = 7.0,
        romValue = null,
        romUnit = null,
        notes = "",
        loggedAt = 1,
        updatedAt = 1,
    )

    private fun openDatabase() {
        database = Room.databaseBuilder(context, VibeDatabase::class.java, databaseName)
            .addMigrations(com.petermathie.vibetrainer.data.local.MIGRATION_1_2, com.petermathie.vibetrainer.data.local.MIGRATION_2_3)
            .build()
        repository = TrainingRepository(database, database.programmeDao(), database.workoutDao(), database.trackerDao())
    }
}
