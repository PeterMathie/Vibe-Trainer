package com.petermathie.vibetrainer

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.petermathie.vibetrainer.data.local.MIGRATION_2_3
import com.petermathie.vibetrainer.data.local.MIGRATION_3_4
import com.petermathie.vibetrainer.data.local.MIGRATION_4_5
import com.petermathie.vibetrainer.data.local.VibeDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun migrate3To4PreservesFieldsAndAddsRicherConfiguration() {
        helper.createDatabase(databaseName, 3).apply {
            execSQL("INSERT INTO trackers (id, name, isDemo, isArchived) VALUES ('habit', 'Habit', 0, 0)")
            execSQL(
                """
                    INSERT INTO tracker_fields
                        (id, trackerId, name, valueType, unit, targetComparison, targetValue, position)
                    VALUES ('field', 'habit', 'Existing field', 'NUMBER', 'units', 'AT_LEAST', 5, 0)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(databaseName, 4, true, MIGRATION_3_4).use { migrated ->
            migrated.query("SELECT name, choiceOptions, targetMaxValue, isArchived FROM tracker_fields WHERE id='field'").use {
                assertEquals(true, it.moveToFirst())
                assertEquals("Existing field", it.getString(0))
                assertEquals("", it.getString(1))
                assertEquals(true, it.isNull(2))
                assertEquals(0, it.getInt(3))
            }
        }
    }

    @Test
    fun migrate4To5SnapshotsHistoricalDefinitionsAndOutcomes() {
        helper.createDatabase(databaseName, 4).apply {
                    execSQL("INSERT INTO bands (id,name,widthCentimetres,colourArgb) VALUES ('band','Band',1.2,0)")
                    execSQL("INSERT INTO exercises (id,canonicalName,tag,trackingType,equipment,instructions,source,isCustom,isArchived) VALUES ('e','Exercise','STRENGTH','ASSISTED_REPS',NULL,NULL,'USER',1,0)")
                    execSQL("INSERT INTO exercise_variations (id,exerciseId,name,progressionRank,isSeeded) VALUES ('v','e','Variation',3,0)")
                    execSQL("INSERT INTO workouts (id,programmeDayId,name,mode,status,startedAt,finishedAt,notes,bodyweightKg,isDemo) VALUES ('w',NULL,'Workout','STRENGTH','FINISHED',1,2,'',80,0)")
                    execSQL("INSERT INTO workout_exercises (id,workoutId,plannedExerciseId,actualExerciseId,position,notes,restSeconds,supersetGroup,exerciseName,trackingType,targets) VALUES ('we','w','e','e',0,'',60,NULL,'Exercise','ASSISTED_REPS','')")
                    execSQL("INSERT INTO workout_sets (id,workoutExerciseId,ordinal,setType,result,variationId,weightKg,reps,holdMillis,leftReps,rightReps,leftHoldMillis,rightHoldMillis,addedWeightKg,assistanceKg,rpe,romValue,romUnit,notes,loggedAt,updatedAt) VALUES ('s','we',1,'WORKING','COMPLETED','v',NULL,5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',1,1)")
                    execSQL("INSERT INTO workout_set_bands (setId,bandId,ordinal) VALUES ('s','band',0)")
                    execSQL("INSERT INTO trackers (id,name,isDemo,isArchived) VALUES ('t','Tracker',0,0)")
                    execSQL("INSERT INTO tracker_fields (id,trackerId,name,valueType,unit,targetComparison,targetValue,position,choiceOptions,targetMaxValue,isArchived) VALUES ('f','t','Field','NUMBER',NULL,'AT_LEAST',5,0,'',NULL,0)")
                    execSQL("INSERT INTO tracker_daily_values (fieldId,epochDay,numericValue,booleanValue,textValue,notes,updatedAt) VALUES ('f',1,6,NULL,NULL,'',1)")
                    close()
        }

        helper.runMigrationsAndValidate(databaseName, 5, true, MIGRATION_4_5).use { migrated ->
                    migrated.query("SELECT variationRankSnapshot FROM workout_sets WHERE id='s'").use { assertTrue(it.moveToFirst()); assertEquals(3,it.getInt(0)) }
                    migrated.query("SELECT nameSnapshot,widthCentimetresSnapshot FROM workout_set_bands WHERE setId='s'").use { assertTrue(it.moveToFirst()); assertEquals("Band",it.getString(0)); assertEquals(1.2,it.getDouble(1),0.0) }
                    migrated.query("SELECT targetMet FROM tracker_day_outcomes WHERE trackerId='t' AND epochDay=1").use { assertTrue(it.moveToFirst()); assertEquals(1,it.getInt(0)) }
        }
    }
}
