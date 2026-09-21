package com.petermathie.vibetrainer.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "seed_metadata", primaryKeys = ["key"])
data class SeedMetadataEntity(val key: String, val version: Int)

@Entity(tableName = "muscles", primaryKeys = ["id"])
data class MuscleEntity(
    val id: String,
    val displayName: String,
    val svgGroupId: String,
)

@Entity(tableName = "exercises", primaryKeys = ["id"], indices = [Index("canonicalName")])
data class ExerciseEntity(
    val id: String,
    val canonicalName: String,
    val tag: String,
    val trackingType: String,
    val equipment: String?,
    val instructions: String?,
    val source: String,
    val isCustom: Boolean,
    val isArchived: Boolean = false,
)

@Entity(
    tableName = "exercise_aliases",
    primaryKeys = ["exerciseId", "normalizedAlias"],
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("exerciseId"), Index("normalizedAlias")],
)
data class ExerciseAliasEntity(
    val exerciseId: String,
    val alias: String,
    val normalizedAlias: String,
)

@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exerciseId", "muscleId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscleId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("exerciseId"), Index("muscleId")],
)
data class ExerciseMuscleEntity(
    val exerciseId: String,
    val muscleId: String,
    val role: String,
)

@Entity(
    tableName = "exercise_variations",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("exerciseId")],
)
data class ExerciseVariationEntity(
    val id: String,
    val exerciseId: String,
    val name: String,
    val progressionRank: Int,
    val isSeeded: Boolean,
)

@Entity(tableName = "bands", primaryKeys = ["id"])
data class BandEntity(
    val id: String,
    val name: String,
    val widthCentimetres: Double,
    val colourArgb: Long,
)

@Entity(tableName = "programmes", primaryKeys = ["id"])
data class ProgrammeEntity(
    val id: String,
    val name: String,
    val mode: String,
    val isDemo: Boolean,
    val isArchived: Boolean = false,
)

@Entity(
    tableName = "programme_days",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = ProgrammeEntity::class,
        parentColumns = ["id"],
        childColumns = ["programmeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("programmeId")],
)
data class ProgrammeDayEntity(
    val id: String,
    val programmeId: String,
    val name: String,
    val position: Int,
)

@Entity(
    tableName = "programme_exercises",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = ProgrammeDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["programmeDayId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("programmeDayId"), Index("exerciseId")],
)
data class ProgrammeExerciseEntity(
    val id: String,
    val programmeDayId: String,
    val exerciseId: String,
    val position: Int,
    val targetSets: Int?,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?,
    val targetHoldSeconds: Int?,
    val restSeconds: Int,
    val targetRpe: Double?,
    val notes: String,
    val supersetGroup: String?,
)

@Entity(
    tableName = "workouts",
    primaryKeys = ["id"],
    indices = [Index("status"), Index("finishedAt"), Index("programmeDayId")],
)
data class WorkoutEntity(
    val id: String,
    val programmeDayId: String?,
    val name: String,
    val mode: String,
    val status: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val notes: String,
    val bodyweightKg: Double?,
    val isDemo: Boolean,
)

@Entity(
    tableName = "workout_exercises",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["plannedExerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["actualExerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("workoutId"), Index("plannedExerciseId"), Index("actualExerciseId")],
)
data class WorkoutExerciseEntity(
    val id: String,
    val workoutId: String,
    val plannedExerciseId: String,
    val actualExerciseId: String,
    val position: Int,
    val notes: String,
    val restSeconds: Int,
    val supersetGroup: String?,
    @androidx.room.ColumnInfo(defaultValue = "''") val exerciseName: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val trackingType: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val targets: String = "",
)

@Entity(tableName="workout_muscles",primaryKeys=["workoutExerciseId","muscleId"],foreignKeys=[ForeignKey(entity=WorkoutExerciseEntity::class,parentColumns=["id"],childColumns=["workoutExerciseId"],onDelete=ForeignKey.CASCADE)],indices=[Index("workoutExerciseId")])
data class WorkoutMuscleEntity(val workoutExerciseId:String,val muscleId:String,val role:String)

@Entity(
    tableName = "workout_sets",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = WorkoutExerciseEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutExerciseId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("workoutExerciseId"), Index("loggedAt")],
)
data class WorkoutSetEntity(
    val id: String,
    val workoutExerciseId: String,
    val ordinal: Int,
    val setType: String,
    val result: String,
    val variationId: String?,
    val weightKg: Double?,
    val reps: Int?,
    val holdMillis: Long?,
    val leftReps: Int?,
    val rightReps: Int?,
    val leftHoldMillis: Long?,
    val rightHoldMillis: Long?,
    val addedWeightKg: Double?,
    val assistanceKg: Double?,
    val rpe: Double?,
    val romValue: Double?,
    val romUnit: String?,
    val notes: String,
    val loggedAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "workout_set_bands",
    primaryKeys = ["setId", "bandId", "ordinal"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BandEntity::class,
            parentColumns = ["id"],
            childColumns = ["bandId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("setId"), Index("bandId")],
)
data class WorkoutSetBandEntity(
    val setId: String,
    val bandId: String,
    val ordinal: Int,
)

@Entity(tableName = "trackers", primaryKeys = ["id"])
data class TrackerEntity(
    val id: String,
    val name: String,
    val isDemo: Boolean,
    val isArchived: Boolean = false,
)

@Entity(
    tableName = "tracker_fields",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = TrackerEntity::class,
        parentColumns = ["id"],
        childColumns = ["trackerId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("trackerId")],
)
data class TrackerFieldEntity(
    val id: String,
    val trackerId: String,
    val name: String,
    val valueType: String,
    val unit: String?,
    val targetComparison: String?,
    val targetValue: Double?,
    val position: Int,
)

@Entity(
    tableName = "tracker_daily_values",
    primaryKeys = ["fieldId", "epochDay"],
    foreignKeys = [ForeignKey(
        entity = TrackerFieldEntity::class,
        parentColumns = ["id"],
        childColumns = ["fieldId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("fieldId"), Index("epochDay")],
)
data class TrackerDailyValueEntity(
    val fieldId: String,
    val epochDay: Long,
    val numericValue: Double?,
    val booleanValue: Boolean?,
    val textValue: String?,
    val notes: String,
    val updatedAt: Long,
)

@Entity(tableName = "body_measurements", primaryKeys = ["id"], indices = [Index("recordedAt")])
data class BodyMeasurementEntity(
    val id: String,
    val recordedAt: Long,
    val metric: String,
    val value: Double,
    val unit: String,
    val notes: String,
    val isDemo: Boolean,
)
