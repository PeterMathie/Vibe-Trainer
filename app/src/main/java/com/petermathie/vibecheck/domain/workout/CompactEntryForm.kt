package com.petermathie.vibecheck.domain.workout

import com.petermathie.vibecheck.data.local.WorkoutEntryDraftEntity
import com.petermathie.vibecheck.data.local.WorkoutSetEntity

data class CompactEntryForm(
    val performance: String = "",
    val rpe: String = "",
) {
    fun applyTo(draft: WorkoutEntryDraftEntity): WorkoutEntryDraftEntity = draft.copy(
        performance = performance,
        rpe = rpe,
    )

    fun buildSet(
        base: WorkoutSetEntity,
        hold: Boolean,
        weighted: Boolean,
        pounds: Boolean,
    ): WorkoutSetEntity? {
        val parsed = parsePerformance(base, performance, hold, weighted, pounds) ?: return null
        val parsedRpe = quantitativeInput(rpe, MAX_RPE)
        if (rpe.isNotBlank() && !parsedRpe.isValid) return null
        return parsed.copy(rpe = parsedRpe.value)
    }

    companion object {
        private const val MIN_RPE = 0.0
        private const val MAX_RPE = 10.0

        fun from(draft: WorkoutEntryDraftEntity) = CompactEntryForm(
            performance = draft.performance,
            rpe = draft.rpe,
        )
    }
}
