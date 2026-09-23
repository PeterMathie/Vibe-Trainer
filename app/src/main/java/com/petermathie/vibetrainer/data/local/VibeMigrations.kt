package com.petermathie.vibetrainer.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE programmes ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE workout_exercises ADD COLUMN exerciseName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE workout_exercises ADD COLUMN trackingType TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE workout_exercises ADD COLUMN targets TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE workout_exercises SET exerciseName=(SELECT canonicalName FROM exercises WHERE id=actualExerciseId),trackingType=(SELECT trackingType FROM exercises WHERE id=actualExerciseId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS workout_muscles (workoutExerciseId TEXT NOT NULL, muscleId TEXT NOT NULL, role TEXT NOT NULL, PRIMARY KEY(workoutExerciseId,muscleId), FOREIGN KEY(workoutExerciseId) REFERENCES workout_exercises(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_muscles_workoutExerciseId ON workout_muscles(workoutExerciseId)")
        db.execSQL("INSERT INTO workout_muscles SELECT we.id,em.muscleId,em.role FROM workout_exercises we JOIN exercise_muscles em ON em.exerciseId=we.actualExerciseId")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_entry_drafts (
                workoutExerciseId TEXT NOT NULL,
                setId TEXT NOT NULL,
                ordinal INTEGER NOT NULL,
                performance TEXT NOT NULL,
                rpe TEXT NOT NULL,
                detailsOpen INTEGER NOT NULL,
                warmUp INTEGER NOT NULL,
                failed INTEGER NOT NULL,
                bandIds TEXT NOT NULL,
                variationId TEXT,
                leftValue TEXT NOT NULL,
                rightValue TEXT NOT NULL,
                addedWeight TEXT NOT NULL,
                assistance TEXT NOT NULL,
                romValue TEXT NOT NULL,
                romUnit TEXT NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(workoutExerciseId),
                FOREIGN KEY(workoutExerciseId) REFERENCES workout_exercises(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracker_fields ADD COLUMN choiceOptions TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE tracker_fields ADD COLUMN targetMaxValue REAL")
        db.execSQL("ALTER TABLE tracker_fields ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workout_sets ADD COLUMN variationRankSnapshot INTEGER")
            db.execSQL("UPDATE workout_sets SET variationRankSnapshot=(SELECT progressionRank FROM exercise_variations WHERE id=variationId) WHERE variationId IS NOT NULL")
            db.execSQL("ALTER TABLE workout_set_bands ADD COLUMN nameSnapshot TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE workout_set_bands ADD COLUMN widthCentimetresSnapshot REAL NOT NULL DEFAULT 0")
            db.execSQL("UPDATE workout_set_bands SET nameSnapshot=(SELECT name FROM bands WHERE id=bandId),widthCentimetresSnapshot=(SELECT widthCentimetres FROM bands WHERE id=bandId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS tracker_day_outcomes (
                    trackerId TEXT NOT NULL,
                    epochDay INTEGER NOT NULL,
                    targetMet INTEGER NOT NULL,
                    PRIMARY KEY(trackerId, epochDay),
                    FOREIGN KEY(trackerId) REFERENCES trackers(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tracker_day_outcomes_trackerId ON tracker_day_outcomes(trackerId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tracker_day_outcomes_epochDay ON tracker_day_outcomes(epochDay)")
            db.execSQL(
                """
                INSERT INTO tracker_day_outcomes
                SELECT days.trackerId, days.epochDay,
                  CASE WHEN EXISTS (SELECT 1 FROM tracker_fields f WHERE f.trackerId=days.trackerId AND f.isArchived=0 AND f.targetComparison IS NOT NULL)
                  THEN NOT EXISTS (
                    SELECT 1 FROM tracker_fields f LEFT JOIN tracker_daily_values v ON v.fieldId=f.id AND v.epochDay=days.epochDay
                    WHERE f.trackerId=days.trackerId AND f.isArchived=0 AND f.targetComparison IS NOT NULL AND (
                      v.numericValue IS NULL OR
                      CASE f.targetComparison WHEN 'AT_LEAST' THEN v.numericValue < f.targetValue
                        WHEN 'AT_MOST' THEN v.numericValue > f.targetValue
                        WHEN 'EXACTLY' THEN v.numericValue != f.targetValue
                        WHEN 'RANGE' THEN v.numericValue < f.targetValue OR v.numericValue > f.targetMaxValue
                        ELSE 1 END
                    )
                  ) ELSE 1 END
                FROM (SELECT DISTINCT f.trackerId,v.epochDay FROM tracker_daily_values v JOIN tracker_fields f ON f.id=v.fieldId) days
                """.trimIndent(),
            )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE workout_entry_drafts_new (
                    workoutExerciseId TEXT NOT NULL,
                    setId TEXT NOT NULL,
                    ordinal INTEGER NOT NULL,
                    performance TEXT NOT NULL,
                    rpe TEXT NOT NULL,
                    detailsOpen INTEGER NOT NULL,
                    warmUp INTEGER NOT NULL,
                    failed INTEGER NOT NULL,
                    bandIds TEXT NOT NULL,
                    variationId TEXT,
                    leftValue TEXT NOT NULL,
                    rightValue TEXT NOT NULL,
                    addedWeight TEXT NOT NULL,
                    assistance TEXT NOT NULL,
                    romValue TEXT NOT NULL,
                    romUnit TEXT NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(workoutExerciseId, ordinal),
                    FOREIGN KEY(workoutExerciseId) REFERENCES workout_exercises(id)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("INSERT INTO workout_entry_drafts_new SELECT * FROM workout_entry_drafts")
            db.execSQL("DROP TABLE workout_entry_drafts")
            db.execSQL("ALTER TABLE workout_entry_drafts_new RENAME TO workout_entry_drafts")
            db.execSQL("CREATE INDEX index_workout_entry_drafts_workoutExerciseId ON workout_entry_drafts(workoutExerciseId)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE programme_exercises ADD COLUMN inputConfig TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE workout_exercises ADD COLUMN inputConfig TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE workout_sets ADD COLUMN timeUnderTensionMillis INTEGER")
        db.execSQL("ALTER TABLE workout_sets ADD COLUMN bandResistance INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE workout_entry_drafts ADD COLUMN timeHeld TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE workout_entry_drafts ADD COLUMN timeUnderTension TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE exercises ADD COLUMN inputConfig TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE exercises ADD COLUMN targetSets INTEGER")
            db.execSQL("ALTER TABLE exercises ADD COLUMN targetRepsMin INTEGER")
            db.execSQL("ALTER TABLE exercises ADD COLUMN targetRepsMax INTEGER")
            db.execSQL("ALTER TABLE exercises ADD COLUMN targetRpe REAL")
            db.execSQL("ALTER TABLE exercises ADD COLUMN restSeconds INTEGER NOT NULL DEFAULT 120")
            db.execSQL(
                """
                UPDATE exercises
                SET inputConfig = COALESCE((
                        SELECT pe.inputConfig FROM programme_exercises pe
                        WHERE pe.exerciseId = exercises.id AND pe.inputConfig != ''
                        ORDER BY pe.position LIMIT 1
                    ), ''),
                    targetSets = (SELECT pe.targetSets FROM programme_exercises pe WHERE pe.exerciseId = exercises.id ORDER BY pe.position LIMIT 1),
                    targetRepsMin = (SELECT pe.targetRepsMin FROM programme_exercises pe WHERE pe.exerciseId = exercises.id ORDER BY pe.position LIMIT 1),
                    targetRepsMax = (SELECT pe.targetRepsMax FROM programme_exercises pe WHERE pe.exerciseId = exercises.id ORDER BY pe.position LIMIT 1),
                    targetRpe = (SELECT pe.targetRpe FROM programme_exercises pe WHERE pe.exerciseId = exercises.id ORDER BY pe.position LIMIT 1),
                    restSeconds = COALESCE((SELECT pe.restSeconds FROM programme_exercises pe WHERE pe.exerciseId = exercises.id ORDER BY pe.position LIMIT 1), 120)
                """.trimIndent(),
            )
            db.execSQL(
                """
                UPDATE exercises
                SET inputConfig = 'weightUnit=;bandResistance=false;timeHeld=true;timeUnderTension=true;reps=false'
                WHERE id = 'core:handstand' AND inputConfig = ''
                """.trimIndent(),
            )
            db.execSQL("UPDATE programme_days SET name = 'Stretching', position = 0 WHERE id = 'demo-day-front-splits' AND programmeId = 'demo-programme-stretch'")
            db.execSQL(
                """
                UPDATE programme_exercises
                SET programmeDayId = 'demo-day-front-splits',
                    position = CASE exerciseId
                        WHEN 'core:front-split' THEN 0
                        WHEN 'core:forward-fold' THEN 1
                        WHEN 'core:side-split' THEN 2
                        WHEN 'core:bridge' THEN 3
                        ELSE position
                    END
                WHERE programmeDayId IN (
                    'demo-day-front-splits',
                    'demo-day-forward-fold',
                    'demo-day-side-splits',
                    'demo-day-bridge'
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                DELETE FROM programme_days
                WHERE id IN ('demo-day-forward-fold', 'demo-day-side-splits', 'demo-day-bridge')
                  AND programmeId = 'demo-programme-stretch'
                """.trimIndent(),
            )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE trackers ADD COLUMN colourArgb INTEGER NOT NULL DEFAULT 4283215696")
    }
}
