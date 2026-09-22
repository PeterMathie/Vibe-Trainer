package com.petermathie.vibetrainer

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.petermathie.vibetrainer.data.local.MIGRATION_2_3
import com.petermathie.vibetrainer.data.local.VibeDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VibeDatabaseMigrationTest {
    private val databaseName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        VibeDatabase::class.java,
    )

    @Test
    fun migrate2To3PreservesWorkoutAndCreatesEntryDraftStorage() {
        helper.createDatabase(databaseName, 2).apply {
            execSQL(
                """
                INSERT INTO workouts
                    (id, programmeDayId, name, mode, status, startedAt, finishedAt, notes, bodyweightKg, isDemo)
                VALUES ('existing-workout', NULL, 'Existing', 'STRENGTH', 'DRAFT', 123, NULL, 'keep me', NULL, 0)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(databaseName, 3, true, MIGRATION_2_3).use { migrated ->
            migrated.query("SELECT name, notes FROM workouts WHERE id = 'existing-workout'").use {
                assertEquals(true, it.moveToFirst())
                assertEquals("Existing", it.getString(0))
                assertEquals("keep me", it.getString(1))
            }
            migrated.query("SELECT COUNT(*) FROM workout_entry_drafts").use {
                assertEquals(true, it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        }
    }
}
