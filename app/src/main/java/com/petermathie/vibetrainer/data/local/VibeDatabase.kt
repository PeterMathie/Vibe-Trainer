package com.petermathie.vibetrainer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SeedMetadataEntity::class,
        MuscleEntity::class,
        ExerciseEntity::class,
        ExerciseAliasEntity::class,
        ExerciseMuscleEntity::class,
        ExerciseVariationEntity::class,
        BandEntity::class,
        ProgrammeEntity::class,
        ProgrammeDayEntity::class,
        ProgrammeExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        WorkoutSetBandEntity::class,
        TrackerEntity::class,
        TrackerFieldEntity::class,
        TrackerDailyValueEntity::class,
        BodyMeasurementEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class VibeDatabase : RoomDatabase() {
    abstract fun catalogueDao(): CatalogueDao
    abstract fun programmeDao(): ProgrammeDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun trackerDao(): TrackerDao
    abstract fun metadataDao(): MetadataDao
}
