package com.petermathie.vibetrainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ProgrammeDayRow(
    val id: String,
    val name: String,
    val mode: String,
    val exerciseCount: Int,
)

data class ProgrammeExerciseRow(
    val id: String,
    val exerciseId: String,
    val canonicalName: String,
    val trackingType: String,
    val position: Int,
    val restSeconds: Int,
    val notes: String,
    val supersetGroup: String?,
)

data class WorkoutExerciseRow(
    val workoutExerciseId: String,
    val plannedExerciseId: String,
    val actualExerciseId: String,
    val canonicalName: String,
    val trackingType: String,
    val position: Int,
    val notes: String,
    val restSeconds: Int,
)

data class CompletedMuscleSetRow(
    val workoutId: String,
    val finishedAt: Long,
    val mode: String,
    val exerciseName: String,
    val muscleId: String,
    val role: String,
)

data class ActivityWorkoutRow(val epochMillis: Long)
data class ActivityTrackerRow(val epochDay: Long, val trackerId: String, val targetMet: Boolean)
data class HistoryTrackerRow(
    val epochDay: Long,
    val trackerName: String,
    val fieldName: String,
    val numericValue: Double?,
    val booleanValue: Boolean?,
    val textValue: String?,
    val unit: String?,
    val notes: String,
)

@Dao
interface CatalogueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscles(rows: List<MuscleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(rows: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAliases(rows: List<ExerciseAliasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseMuscles(rows: List<ExerciseMuscleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariations(rows: List<ExerciseVariationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBands(rows: List<BandEntity>)

    @Query("SELECT COUNT(*) FROM exercises WHERE isArchived = 0")
    fun observeExerciseCount(): Flow<Int>

    @Query(
        """
        SELECT DISTINCT e.* FROM exercises e
        LEFT JOIN exercise_aliases a ON a.exerciseId = e.id
        LEFT JOIN exercise_muscles em ON em.exerciseId = e.id
        LEFT JOIN muscles m ON m.id = em.muscleId
        WHERE e.isArchived = 0 AND (
            :query = '' OR
            lower(e.canonicalName) LIKE '%' || lower(:query) || '%' OR
            a.normalizedAlias LIKE '%' || lower(:query) || '%' OR
            lower(m.displayName) LIKE '%' || lower(:query) || '%'
        )
        ORDER BY e.canonicalName
        LIMIT 100
        """,
    )
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>
}

@Dao
interface ProgrammeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammes(rows: List<ProgrammeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(rows: List<ProgrammeDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammeExercises(rows: List<ProgrammeExerciseEntity>)

    @Query(
        """
        SELECT d.id, d.name, p.mode,
            (SELECT COUNT(*) FROM programme_exercises pe WHERE pe.programmeDayId = d.id) AS exerciseCount
        FROM programme_days d
        JOIN programmes p ON p.id = d.programmeId
        WHERE p.isArchived = 0
        ORDER BY p.name, d.position
        """,
    )
    fun observeDays(): Flow<List<ProgrammeDayRow>>

    @Query(
        """
        SELECT pe.id, pe.exerciseId, e.canonicalName, e.trackingType,
               pe.position, pe.restSeconds, pe.notes, pe.supersetGroup
        FROM programme_exercises pe
        JOIN exercises e ON e.id = pe.exerciseId
        WHERE pe.programmeDayId = :dayId
        ORDER BY pe.position
        """,
    )
    suspend fun exercisesForDay(dayId: String): List<ProgrammeExerciseRow>

    @Query("SELECT * FROM programme_days WHERE id = :dayId")
    suspend fun day(dayId: String): ProgrammeDayEntity?

    @Query("SELECT p.mode FROM programmes p JOIN programme_days d ON d.programmeId = p.id WHERE d.id = :dayId")
    suspend fun modeForDay(dayId: String): String?

    @Query("DELETE FROM programmes WHERE isDemo = 1")
    suspend fun deleteDemoProgrammes()
}

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(row: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(rows: List<WorkoutExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(row: WorkoutSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetBands(rows: List<WorkoutSetBandEntity>)

    @Update
    suspend fun updateWorkout(row: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE status = 'DRAFT' ORDER BY startedAt DESC LIMIT 1")
    fun observeDraft(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun workout(workoutId: String): WorkoutEntity?

    @Query(
        """
        SELECT we.id AS workoutExerciseId, we.plannedExerciseId, we.actualExerciseId,
               e.canonicalName, e.trackingType, we.position, we.notes, we.restSeconds
        FROM workout_exercises we
        JOIN exercises e ON e.id = we.actualExerciseId
        WHERE we.workoutId = :workoutId
        ORDER BY we.position
        """,
    )
    suspend fun exercises(workoutId: String): List<WorkoutExerciseRow>

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY ordinal")
    suspend fun sets(workoutExerciseId: String): List<WorkoutSetEntity>

    @Query("SELECT COALESCE(MAX(ordinal), 0) + 1 FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun nextSetOrdinal(workoutExerciseId: String): Int

    @Query("SELECT COUNT(*) FROM workout_sets")
    fun observeSetCount(): Flow<Int>

    @Query("SELECT * FROM workouts WHERE status = 'FINISHED' AND finishedAt IS NOT NULL ORDER BY finishedAt DESC")
    fun observeFinishedWorkouts(): Flow<List<WorkoutEntity>>

    @Query("UPDATE workouts SET status = 'FINISHED', finishedAt = :finishedAt WHERE id = :workoutId")
    suspend fun finish(workoutId: String, finishedAt: Long)

    @Query("DELETE FROM workouts WHERE isDemo = 1")
    suspend fun deleteDemoWorkouts()

    @Query(
        """
        SELECT w.id AS workoutId, w.finishedAt AS finishedAt, w.mode AS mode,
               e.canonicalName AS exerciseName,
               em.muscleId AS muscleId, em.role AS role
        FROM workouts w
        JOIN workout_exercises we ON we.workoutId = w.id
        JOIN workout_sets ws ON ws.workoutExerciseId = we.id
        JOIN exercises e ON e.id = we.actualExerciseId
        JOIN exercise_muscles em ON em.exerciseId = we.actualExerciseId
        WHERE w.status = 'FINISHED' AND w.finishedAt IS NOT NULL
          AND ws.setType = 'WORKING'
          AND ws.result = 'COMPLETED'
          AND (COALESCE(ws.reps, 0) > 0 OR COALESCE(ws.holdMillis, 0) > 0 OR COALESCE(ws.weightKg, 0) > 0)
        ORDER BY w.finishedAt DESC
        """,
    )
    fun observeCompletedMuscleSets(): Flow<List<CompletedMuscleSetRow>>

    @Query("SELECT finishedAt AS epochMillis FROM workouts WHERE status = 'FINISHED' AND finishedAt IS NOT NULL")
    fun observeFinishedWorkoutTimes(): Flow<List<ActivityWorkoutRow>>
}

@Dao
interface TrackerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackers(rows: List<TrackerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFields(rows: List<TrackerFieldEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertValue(row: TrackerDailyValueEntity)

    @Query(
        """
        SELECT v.epochDay, f.trackerId,
          CASE
            WHEN f.targetComparison IS NULL THEN (v.numericValue IS NOT NULL OR v.booleanValue IS NOT NULL OR v.textValue IS NOT NULL)
            WHEN f.targetComparison = 'AT_LEAST' THEN COALESCE(v.numericValue, 0) >= COALESCE(f.targetValue, 0)
            WHEN f.targetComparison = 'AT_MOST' THEN COALESCE(v.numericValue, 0) <= COALESCE(f.targetValue, 0)
            WHEN f.targetComparison = 'EXACTLY' THEN COALESCE(v.numericValue, 0) = COALESCE(f.targetValue, 0)
            ELSE 0
          END AS targetMet
        FROM tracker_daily_values v
        JOIN tracker_fields f ON f.id = v.fieldId
        JOIN trackers t ON t.id = f.trackerId
        WHERE t.isArchived = 0
        """,
    )
    fun observeActivityValues(): Flow<List<ActivityTrackerRow>>

    @Query(
        """
        SELECT v.epochDay, t.name AS trackerName, f.name AS fieldName,
               v.numericValue, v.booleanValue, v.textValue, f.unit, v.notes
        FROM tracker_daily_values v
        JOIN tracker_fields f ON f.id = v.fieldId
        JOIN trackers t ON t.id = f.trackerId
        WHERE t.isArchived = 0
        ORDER BY v.epochDay DESC, t.name, f.position
        """,
    )
    fun observeHistoryValues(): Flow<List<HistoryTrackerRow>>

    @Query("DELETE FROM trackers WHERE isDemo = 1")
    suspend fun deleteDemoTrackers()
}

@Dao
interface MetadataDao {
    @Query("SELECT version FROM seed_metadata WHERE key = :key")
    suspend fun version(key: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(row: SeedMetadataEntity)
}
