package com.petermathie.vibetrainer.domain.programme

import com.petermathie.vibetrainer.data.local.ProgrammeExerciseEntity

data class ProgrammeEntryForm(
    val sets: String,
    val minimumReps: String,
    val maximumReps: String,
    val restSeconds: String,
    val targetRpe: String,
    val notes: String,
    val weightUnit: String?,
    val bandResistance: Boolean,
    val timeHeld: Boolean,
    val timeUnderTension: Boolean,
) {
    fun applyTo(entry: ProgrammeExerciseEntity): ProgrammeExerciseEntity = entry.copy(
        targetSets = sets.toIntOrNull(),
        targetRepsMin = minimumReps.toIntOrNull(),
        targetRepsMax = maximumReps.toIntOrNull(),
        restSeconds = restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: DEFAULT_REST_SECONDS,
        targetRpe = targetRpe.toDoubleOrNull()?.coerceIn(MIN_RPE, MAX_RPE),
        notes = notes,
        inputConfig = ExerciseInputConfig(
            weightUnit = weightUnit,
            bandResistance = bandResistance,
            timeHeld = timeHeld,
            timeUnderTension = timeUnderTension,
            reps = minimumReps.isNotBlank() || maximumReps.isNotBlank(),
        ).encode(),
    )

    companion object {
        private const val DEFAULT_REST_SECONDS = 120
        private const val MIN_RPE = 0.0
        private const val MAX_RPE = 10.0

        fun from(entry: ProgrammeExerciseEntity, trackingType: String) = ProgrammeEntryForm(
            sets = entry.targetSets?.toString().orEmpty(),
            minimumReps = entry.targetRepsMin?.toString().orEmpty(),
            maximumReps = entry.targetRepsMax?.toString().orEmpty(),
            restSeconds = entry.restSeconds.toString(),
            targetRpe = entry.targetRpe?.toString().orEmpty(),
            notes = entry.notes,
            weightUnit = ExerciseInputConfig.decode(entry.inputConfig, trackingType).weightUnit,
            bandResistance = ExerciseInputConfig.decode(entry.inputConfig, trackingType).bandResistance,
            timeHeld = ExerciseInputConfig.decode(entry.inputConfig, trackingType).timeHeld,
            timeUnderTension = ExerciseInputConfig.decode(entry.inputConfig, trackingType).timeUnderTension,
        )
    }
}
