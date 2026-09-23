package com.petermathie.vibetrainer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EditorDao {
    @Query("SELECT * FROM exercises WHERE id=:id") suspend fun exerciseById(id:String):ExerciseEntity?
    @Query("SELECT * FROM exercise_muscles WHERE exerciseId=:id") suspend fun muscleMappings(id:String):List<ExerciseMuscleEntity>
    @Query("SELECT * FROM programme_exercises WHERE programmeDayId=:day ORDER BY position") suspend fun programmeEntries(day:String):List<ProgrammeExerciseEntity>
    @Query("DELETE FROM workout_muscles WHERE workoutExerciseId=:id") suspend fun clearWorkoutMuscles(id:String)
    @Upsert suspend fun workoutMuscles(rows:List<WorkoutMuscleEntity>)
    @Query("SELECT * FROM programmes WHERE isArchived = 0 ORDER BY position,name") fun programmes(): Flow<List<ProgrammeEntity>>
    @Query("SELECT * FROM programme_days ORDER BY position") fun days(): Flow<List<ProgrammeDayEntity>>
    @Query("SELECT * FROM programme_exercises ORDER BY position") fun entries(): Flow<List<ProgrammeExerciseEntity>>
    @Query("SELECT * FROM exercises ORDER BY canonicalName") fun exercises(): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM muscles ORDER BY displayName") fun muscles(): Flow<List<MuscleEntity>>
    @Query("SELECT * FROM exercise_muscles") fun mappings(): Flow<List<ExerciseMuscleEntity>>
    @Query("SELECT * FROM exercise_aliases") fun aliases(): Flow<List<ExerciseAliasEntity>>
    @Query("SELECT * FROM exercise_variations ORDER BY progressionRank") fun variations(): Flow<List<ExerciseVariationEntity>>
    @Query("SELECT * FROM exercise_variations WHERE id=:id") suspend fun variationById(id: String): ExerciseVariationEntity?
    @Upsert suspend fun programme(row: ProgrammeEntity)
    @Upsert suspend fun day(row: ProgrammeDayEntity)
    @Upsert suspend fun entry(row: ProgrammeExerciseEntity)
    @Upsert suspend fun exercise(row: ExerciseEntity)
    @Upsert suspend fun variation(row: ExerciseVariationEntity)
    @Query("DELETE FROM programmes WHERE id = :id") suspend fun deleteProgramme(id: String)
    @Query("DELETE FROM programme_days WHERE id = :id") suspend fun deleteDay(id: String)
    @Query("DELETE FROM programme_exercises WHERE id = :id") suspend fun deleteEntry(id: String)
    @Query("DELETE FROM exercise_aliases WHERE exerciseId = :id") suspend fun clearAliases(id: String)
    @Query("DELETE FROM exercise_muscles WHERE exerciseId = :id") suspend fun clearMappings(id: String)
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC") fun workouts(): Flow<List<WorkoutEntity>>
    @Query("SELECT * FROM workout_exercises ORDER BY position") fun workoutExercises(): Flow<List<WorkoutExerciseEntity>>
    @Query("SELECT * FROM workout_sets ORDER BY ordinal") fun sets(): Flow<List<WorkoutSetEntity>>
    @Query("SELECT * FROM workout_set_bands ORDER BY ordinal") fun setBands(): Flow<List<WorkoutSetBandEntity>>
    @Query("SELECT * FROM bands ORDER BY widthCentimetres") fun bands(): Flow<List<BandEntity>>
    @Query("SELECT * FROM bands WHERE id=:id") suspend fun bandById(id: String): BandEntity?
    @Upsert suspend fun workout(row: WorkoutEntity)
    @Upsert suspend fun workoutExercise(row: WorkoutExerciseEntity)
    @Upsert suspend fun set(row: WorkoutSetEntity)
    @Upsert suspend fun setBands(rows: List<WorkoutSetBandEntity>)
    @Query("DELETE FROM workout_sets WHERE id = :id") suspend fun deleteSet(id: String)
    @Query("DELETE FROM workout_set_bands WHERE setId = :id") suspend fun clearBands(id: String)
    @Query("SELECT * FROM workout_entry_drafts WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun entryDraft(workoutExerciseId: String): WorkoutEntryDraftEntity?
    @Upsert suspend fun entryDraft(row: WorkoutEntryDraftEntity)
    @Query("SELECT EXISTS(SELECT 1 FROM workout_sets WHERE id = :setId)")
    suspend fun hasSet(setId: String): Boolean
    @Query("SELECT EXISTS(SELECT 1 FROM workout_exercises we JOIN workouts w ON w.id = we.workoutId WHERE we.id = :workoutExerciseId AND w.status = 'DRAFT')")
    suspend fun belongsToDraftWorkout(workoutExerciseId: String): Boolean
    @Transaction
    suspend fun persistEntryDraft(row: WorkoutEntryDraftEntity) {
        if (!hasSet(row.setId) && belongsToDraftWorkout(row.workoutExerciseId)) entryDraft(row)
    }
    @Query("DELETE FROM workout_entry_drafts WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteEntryDraft(workoutExerciseId: String)
    @Query("DELETE FROM workout_entry_drafts WHERE workoutExerciseId IN (SELECT id FROM workout_exercises WHERE workoutId = :workoutId)")
    suspend fun deleteEntryDraftsForWorkout(workoutId: String)
    @Transaction
    suspend fun consumeEntryDraft(row: WorkoutSetEntity, bands: List<WorkoutSetBandEntity>) {
        set(row)
        clearBands(row.id)
        setBands(bands)
        deleteEntryDraft(row.workoutExerciseId)
    }
    @Transaction
    suspend fun saveSetWithSnapshots(row: WorkoutSetEntity, bandIds: List<String>, consumeDraft: Boolean) {
        val saved = row.copy(variationRankSnapshot = row.variationId?.let { variationById(it)?.progressionRank })
        set(saved)
        clearBands(saved.id)
        setBands(bandIds.mapIndexedNotNull { index, id ->
            bandById(id)?.let { WorkoutSetBandEntity(saved.id, id, index, it.name, it.widthCentimetres) }
        })
        if (consumeDraft) deleteEntryDraft(saved.workoutExerciseId)
    }
    @Query("DELETE FROM workouts WHERE id = :id") suspend fun deleteWorkout(id: String)
    @Query("SELECT * FROM trackers WHERE isArchived = 0 ORDER BY name") fun trackers(): Flow<List<TrackerEntity>>
    @Query("SELECT * FROM tracker_fields ORDER BY position") fun fields(): Flow<List<TrackerFieldEntity>>
    @Query("SELECT * FROM tracker_daily_values") fun values(): Flow<List<TrackerDailyValueEntity>>
    @Upsert suspend fun tracker(row: TrackerEntity)
    @Upsert suspend fun field(row: TrackerFieldEntity)
    @Query("SELECT * FROM tracker_fields WHERE id = :id") suspend fun fieldById(id: String): TrackerFieldEntity?
    @Query("DELETE FROM tracker_daily_values WHERE fieldId = :id AND epochDay = :day") suspend fun clearValue(id: String, day: Long)
    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC") fun measurements(): Flow<List<BodyMeasurementEntity>>
    @Upsert suspend fun measurement(row: BodyMeasurementEntity)
    @Query("DELETE FROM body_measurements WHERE id = :id") suspend fun deleteMeasurement(id: String)
}
