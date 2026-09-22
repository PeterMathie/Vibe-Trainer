package com.petermathie.vibetrainer.domain.programme

import com.petermathie.vibetrainer.data.local.ProgrammeExerciseEntity

data class ProgrammeEntryForm(
    val sets: String,
    val minimumReps: String,
    val maximumReps: String,
    val holdSeconds: String,
    val restSeconds: String,
    val targetRpe: String,
    val group: String,
    val notes: String,
) {
    fun applyTo(entry: ProgrammeExerciseEntity): ProgrammeExerciseEntity = entry.copy(
        targetSets = sets.toIntOrNull(),
        targetRepsMin = minimumReps.toIntOrNull(),
        targetRepsMax = maximumReps.toIntOrNull(),
        targetHoldSeconds = holdSeconds.toIntOrNull(),
        restSeconds = restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: DEFAULT_REST_SECONDS,
        targetRpe = targetRpe.toDoubleOrNull()?.coerceIn(MIN_RPE, MAX_RPE),
        supersetGroup = group.takeIf { it.isNotBlank() },
        notes = notes,
    )

    companion object {
        private const val DEFAULT_REST_SECONDS = 120
        private const val MIN_RPE = 0.0
        private const val MAX_RPE = 10.0

        fun from(entry: ProgrammeExerciseEntity) = ProgrammeEntryForm(
            sets = entry.targetSets?.toString().orEmpty(),
            minimumReps = entry.targetRepsMin?.toString().orEmpty(),
            maximumReps = entry.targetRepsMax?.toString().orEmpty(),
            holdSeconds = entry.targetHoldSeconds?.toString().orEmpty(),
            restSeconds = entry.restSeconds.toString(),
            targetRpe = entry.targetRpe?.toString().orEmpty(),
            group = entry.supersetGroup.orEmpty(),
            notes = entry.notes,
        )
    }
}
