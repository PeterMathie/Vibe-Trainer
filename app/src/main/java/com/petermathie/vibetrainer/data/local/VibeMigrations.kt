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
