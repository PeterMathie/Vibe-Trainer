package com.petermathie.vibecheck

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibecheck.data.TrainingRepository
import com.petermathie.vibecheck.data.local.BodyMeasurementEntity
import com.petermathie.vibecheck.data.local.ExerciseEntity
import com.petermathie.vibecheck.data.local.ProgrammeEntity
import com.petermathie.vibecheck.data.local.TrackerEntity
import com.petermathie.vibecheck.data.local.VibeDatabase
import com.petermathie.vibecheck.data.local.WorkoutEntity
import com.petermathie.vibecheck.data.seed.DatabaseSeeder
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoDataRemovalTest {
    private lateinit var database: VibeDatabase
    private lateinit var filesRoot: File
    private lateinit var context: Context
    private lateinit var repository: TrainingRepository

    @Before
    fun setUp() = runBlocking {
        val application = ApplicationProvider.getApplicationContext<Context>()
        filesRoot = File(application.cacheDir, "demo-removal-${UUID.randomUUID()}").also {
            check(it.mkdirs())
        }
        context = object : ContextWrapper(application) {
            override fun getFilesDir(): File = filesRoot
        }
        database = Room.inMemoryDatabaseBuilder(application, VibeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseSeeder(context, database).seedIfNeeded()
        repository = TrainingRepository(
            database,
            database.programmeDao(),
            database.workoutDao(),
            database.trackerDao(),
            context,
        )
    }

    @After
    fun tearDown() {
        database.close()
        filesRoot.deleteRecursively()
    }

    @Test(timeout = 120_000)
    fun removesOnlyDemoPersonalDataAndDoesNotReseedIt() = runBlocking {
        insertRealRecords()
        val catalogueBefore = catalogueCounts()
        val metadataBefore = count("seed_metadata")
        val realPhoto = File(filesRoot, "progress-photos/real-user-photo.jpg").also {
            check(it.parentFile?.let { parent -> parent.mkdirs() || parent.isDirectory } == true)
            it.writeText("real")
        }

        val summary = repository.removeDemoData()

        assertTrue(summary.workouts > 0)
        assertTrue(summary.programmes > 0)
        assertTrue(summary.trackers > 0)
        assertTrue(summary.measurements > 0)
        assertTrue(summary.photos > 0)
        assertEquals(0, count("workouts", "isDemo = 1"))
        assertEquals(0, count("programmes", "isDemo = 1"))
        assertEquals(0, count("trackers", "isDemo = 1"))
        assertEquals(0, count("body_measurements", "isDemo = 1"))
        assertEquals(1, count("workouts", "id = 'real-workout'"))
        assertEquals(1, count("programmes", "id = 'real-programme'"))
        assertEquals(1, count("trackers", "id = 'real-tracker'"))
        assertEquals(1, count("body_measurements", "id = 'real-measurement'"))
        assertEquals(catalogueBefore, catalogueCounts())
        assertEquals(metadataBefore, count("seed_metadata"))
        assertTrue(realPhoto.isFile)
        assertFalse(File(filesRoot, "demo-progress-photo-ownership").exists())

        DatabaseSeeder(context, database).seedIfNeeded()

        assertEquals(0, count("workouts", "isDemo = 1"))
        assertEquals(0, count("programmes", "isDemo = 1"))
        assertEquals(0, count("trackers", "isDemo = 1"))
        assertEquals(0, count("body_measurements", "isDemo = 1"))
        assertEquals(catalogueBefore, catalogueCounts())
        assertTrue(realPhoto.isFile)
    }

    @Test(timeout = 120_000)
    fun photoDeletionFailureLeavesDemoDatabaseRowsAndReportsFailure() = runBlocking {
        val photos = File(filesRoot, "progress-photos").also { check(it.mkdirs() || it.isDirectory) }
        val markers = File(filesRoot, "demo-progress-photo-ownership").also {
            check(it.mkdirs() || it.isDirectory)
        }
        val blockedPhoto = File(photos, "blocked.jpg").also { check(it.mkdir()) }
        File(blockedPhoto, "prevents-delete").writeText("blocked")
        File(markers, blockedPhoto.name).createNewFile()
        val demoWorkoutsBefore = count("workouts", "isDemo = 1")

        var failure: Throwable? = null
        try {
            repository.removeDemoData()
        } catch (error: Throwable) {
            failure = error
        }

        assertTrue(failure?.message.orEmpty().contains("Could not remove generated demo photo"))
        assertEquals(demoWorkoutsBefore, count("workouts", "isDemo = 1"))
        assertTrue(count("body_measurements", "isDemo = 1") > 0)
    }

    private suspend fun insertRealRecords() {
        database.editorDao().exercise(
            ExerciseEntity(
                id = "real-exercise",
                canonicalName = "Real custom movement",
                tag = "STRENGTH",
                trackingType = "REPS",
                equipment = null,
                instructions = null,
                source = "user",
                isCustom = true,
            ),
        )
        database.editorDao().programme(
            ProgrammeEntity("real-programme", "My programme", "GYM", isDemo = false),
        )
        database.editorDao().workout(
            WorkoutEntity(
                id = "real-workout",
                programmeDayId = null,
                name = "My workout",
                mode = "GYM",
                status = "FINISHED",
                startedAt = 1L,
                finishedAt = 2L,
                notes = "",
                bodyweightKg = null,
                isDemo = false,
            ),
        )
        database.editorDao().tracker(
            TrackerEntity("real-tracker", "My habit", isDemo = false),
        )
        database.editorDao().measurement(
            BodyMeasurementEntity(
                "real-measurement",
                1L,
                "Bodyweight",
                75.0,
                "kg",
                "",
                isDemo = false,
            ),
        )
    }

    private fun catalogueCounts(): List<Int> = listOf(
        count("exercises"),
        count("exercise_aliases"),
        count("exercise_muscles"),
        count("exercise_variations"),
        count("bands"),
        count("muscles"),
    )

    private fun count(table: String, where: String? = null): Int {
        val suffix = where?.let { " WHERE $it" }.orEmpty()
        return database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table$suffix").use {
            check(it.moveToFirst())
            it.getInt(0)
        }
    }
}
