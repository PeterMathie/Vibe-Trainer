package com.petermathie.vibetrainer.data

import androidx.room.withTransaction
import com.petermathie.vibetrainer.data.local.ProgrammeDao
import com.petermathie.vibetrainer.data.local.TrackerDao
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.local.WorkoutDao
import com.petermathie.vibetrainer.data.local.WorkoutEntity
import com.petermathie.vibetrainer.data.local.WorkoutExerciseEntity
import com.petermathie.vibetrainer.data.local.WorkoutSetEntity
import com.petermathie.vibetrainer.domain.model.ActiveWorkout
import com.petermathie.vibetrainer.domain.model.ActivityDay
import com.petermathie.vibetrainer.domain.model.MuscleRecency
import com.petermathie.vibetrainer.domain.model.HistoryActivity
import com.petermathie.vibetrainer.domain.model.HistoryDayDetail
import com.petermathie.vibetrainer.domain.model.MuscleRole
import com.petermathie.vibetrainer.domain.model.ProgrammeDaySummary
import com.petermathie.vibetrainer.domain.model.SetDraft
import com.petermathie.vibetrainer.domain.model.TrackingType
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.domain.model.WorkoutExerciseLog
import com.petermathie.vibetrainer.domain.model.WorkoutSetLog
import com.petermathie.vibetrainer.domain.model.WorkoutStatus
import com.petermathie.vibetrainer.domain.recency.RecencyCalculator
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Singleton
class TrainingRepository @Inject constructor(
    private val database: VibeDatabase,
    private val programmeDao: ProgrammeDao,
    private val workoutDao: WorkoutDao,
    private val trackerDao: TrackerDao,
) {
    fun observeProgrammeDays(): Flow<List<ProgrammeDaySummary>> = programmeDao.observeDays().map { rows ->
        rows.map { ProgrammeDaySummary(it.id, it.name, TrainingMode.valueOf(it.mode), it.exerciseCount) }
    }

    fun observeDraft(): Flow<ActiveWorkout?> =
        combine(
            workoutDao.observeDraft(),
            workoutDao.observeSetCount(),
            workoutDao.observeWorkoutExerciseChanges(),
        ) { draft, _, _ -> draft }
            .flatMapLatest { draft -> flow { emit(draft?.let { loadWorkout(it) }) } }

    suspend fun startWorkout(dayId: String, replaceExisting: Boolean = false): String = database.withTransaction {
        val existing = workoutDao.draft()
        if (existing?.programmeDayId == dayId) return@withTransaction existing.id
        if (existing != null && !replaceExisting) return@withTransaction existing.id
        if (existing != null) {
            database.editorDao().deleteEntryDraftsForWorkout(existing.id)
            workoutDao.updateWorkout(existing.copy(status = WorkoutStatus.DISCARDED.name))
        }
        val day = requireNotNull(programmeDao.day(dayId))
        val mode = requireNotNull(programmeDao.modeForDay(dayId))
        val exercises = programmeDao.exercisesForDay(dayId)
        val workoutId = UUID.randomUUID().toString()
        workoutDao.insertWorkout(
            WorkoutEntity(
                id = workoutId,
                programmeDayId = dayId,
                name = day.name,
                mode = mode,
                status = WorkoutStatus.DRAFT.name,
                startedAt = System.currentTimeMillis(),
                finishedAt = null,
                notes = "",
                bodyweightKg = null,
                isDemo = false,
            ),
        )
        exercises.forEach { row ->
            val snapshot=WorkoutExerciseEntity(
                id = UUID.randomUUID().toString(),
                workoutId = workoutId,
                plannedExerciseId = row.exerciseId,
                actualExerciseId = row.exerciseId,
                position = row.position,
                notes = row.notes,
                restSeconds = row.restSeconds,
                supersetGroup = row.supersetGroup,
                exerciseName = row.canonicalName,
                trackingType = row.trackingType,
                targets = "${row.targetSets ?: 3} sets · ${row.targetRepsMin ?: 0} reps · RPE ${row.targetRpe ?: "—"}",
                inputConfig = row.inputConfig,
            )
            workoutDao.insertWorkoutExercises(listOf(snapshot))
            database.editorDao().workoutMuscles(database.editorDao().muscleMappings(row.exerciseId).map { com.petermathie.vibetrainer.data.local.WorkoutMuscleEntity(snapshot.id,it.muscleId,it.role) })
        }
        workoutId
    }

    suspend fun addSet(workoutExerciseId: String, draft: SetDraft) {
        val now = System.currentTimeMillis()
        workoutDao.insertSet(
            WorkoutSetEntity(
                id = UUID.randomUUID().toString(),
                workoutExerciseId = workoutExerciseId,
                ordinal = workoutDao.nextSetOrdinal(workoutExerciseId),
                setType = draft.type.name,
                result = draft.result.name,
                variationId = null,
                weightKg = draft.weightKg,
                reps = draft.reps,
                holdMillis = draft.holdMillis,
                leftReps = null,
                rightReps = null,
                leftHoldMillis = null,
                rightHoldMillis = null,
                addedWeightKg = null,
                assistanceKg = null,
                rpe = draft.rpe,
                romValue = null,
                romUnit = null,
                notes = draft.notes,
                loggedAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateExerciseNotes(workoutExerciseId: String, notes: String) =
        workoutDao.updateExerciseNotes(workoutExerciseId, notes)

    suspend fun finishWorkout(workoutId: String) = database.withTransaction {
        database.editorDao().deleteEntryDraftsForWorkout(workoutId)
        workoutDao.finish(workoutId, System.currentTimeMillis())
    }

    fun observeMuscleRecency(mode: TrainingMode, atMillis: Long = System.currentTimeMillis()): Flow<List<MuscleRecency>> =
        workoutDao.observeCompletedMuscleSets().map { rows ->
            rows.filter { it.mode == mode.name && it.finishedAt <= atMillis }
                .groupBy { it.muscleId }
                .map { (muscleId, muscleRows) ->
                    val last = muscleRows.maxOfOrNull { it.finishedAt }
                    val recentWindowStart = atMillis - 7 * 86_400_000L
                    val recentRows = muscleRows.filter { it.finishedAt >= recentWindowStart }
                    MuscleRecency(
                        muscleId = muscleId,
                        lastTrainedAt = last,
                        band = RecencyCalculator.band(last, atMillis),
                        contributingExerciseNames = muscleRows.filter { it.finishedAt == last }.map { it.exerciseName }.distinct(),
                        setEquivalents = recentRows.sumOf { MuscleRole.valueOf(it.role).setEquivalent },
                    )
                }
        }

    fun observeActivityHeatmap(zoneId: ZoneId = ZoneId.systemDefault()): Flow<List<ActivityDay>> =
        combine(workoutDao.observeFinishedWorkoutTimes(), trackerDao.observeActivityValues()) { workouts, trackerRows ->
            val counts = mutableMapOf<Long, Int>()
            workouts.forEach { row ->
                val epochDay = Instant.ofEpochMilli(row.epochMillis).atZone(zoneId).toLocalDate().toEpochDay()
                counts[epochDay] = counts.getOrDefault(epochDay, 0) + 1
            }
            trackerRows.groupBy { it.epochDay to it.trackerId }.filterValues { rows -> rows.all { it.targetMet } }.keys.forEach { (day, _) ->
                counts[day] = counts.getOrDefault(day, 0) + 1
            }
            counts.map { ActivityDay(it.key, it.value) }.sortedBy { it.epochDay }
        }

    fun observeHistoryDay(epochDay: Long, zoneId: ZoneId = ZoneId.systemDefault()): Flow<HistoryDayDetail> =
        combine(workoutDao.observeFinishedWorkouts(), trackerDao.observeHistoryValues()) { workouts, values ->
            val workoutActivities = workouts.filter { row ->
                row.finishedAt?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate().toEpochDay() == epochDay } == true
            }.map { row ->
                HistoryActivity(
                    title = row.name,
                    detail = if (row.mode == TrainingMode.STRETCHING.name) "Stretching session" else "Strength workout",
                    notes = row.notes,
                )
            }
            val trackerActivities = values.filter { it.epochDay == epochDay }.groupBy { it.trackerName }.map { (name, rows) ->
                val detail = rows.joinToString(" · ") { value ->
                    when {
                        value.numericValue != null -> "${value.fieldName}: ${value.numericValue}${value.unit?.let { " $it" } ?: ""}"
                        value.booleanValue != null -> "${value.fieldName}: ${if (value.booleanValue) "done" else "not done"}"
                        else -> "${value.fieldName}: ${value.textValue.orEmpty()}"
                    }
                }
                HistoryActivity(name, detail, rows.map { it.notes }.firstOrNull { it.isNotBlank() }.orEmpty())
            }
            HistoryDayDetail(epochDay, workoutActivities + trackerActivities)
        }

    suspend fun removeDemoData() = database.withTransaction {
        workoutDao.deleteDemoWorkouts()
        programmeDao.deleteDemoProgrammes()
        trackerDao.deleteDemoTrackers()
    }

    private suspend fun loadWorkout(row: WorkoutEntity): ActiveWorkout {
        val exercises = workoutDao.exercises(row.id).map { exercise ->
            val sets = workoutDao.sets(exercise.workoutExerciseId).map { set ->
                WorkoutSetLog(
                    id = set.id,
                    ordinal = set.ordinal,
                    weightKg = set.weightKg,
                    reps = set.reps,
                    holdMillis = set.holdMillis,
                    rpe = set.rpe,
                    notes = set.notes,
                    type = com.petermathie.vibetrainer.domain.model.SetType.valueOf(set.setType),
                    result = com.petermathie.vibetrainer.domain.model.SetResult.valueOf(set.result),
                )
            }
            WorkoutExerciseLog(
                id = exercise.workoutExerciseId,
                plannedExerciseId = exercise.plannedExerciseId,
                actualExerciseId = exercise.actualExerciseId,
                exerciseName = exercise.canonicalName,
                trackingType = TrackingType.valueOf(exercise.trackingType),
                notes = exercise.notes,
                restSeconds = exercise.restSeconds,
                sets = sets,
            )
        }
        return ActiveWorkout(row.id, row.name, TrainingMode.valueOf(row.mode), row.startedAt, row.notes, exercises)
    }
}
