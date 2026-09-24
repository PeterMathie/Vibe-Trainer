package com.petermathie.vibecheck.domain.recency

import com.petermathie.vibecheck.domain.model.MuscleRecencyBand

object RecencyCalculator {
    private const val HOUR = 3_600_000L
    private const val DAY = 24 * HOUR

    fun band(lastTrainedAt: Long?, atMillis: Long): MuscleRecencyBand {
        if (lastTrainedAt == null) return MuscleRecencyBand.NEVER
        val age = (atMillis - lastTrainedAt).coerceAtLeast(0L)
        return when {
            age < DAY -> MuscleRecencyBand.UNDER_24_HOURS
            age < 2 * DAY -> MuscleRecencyBand.HOURS_24_TO_48
            age < 3 * DAY -> MuscleRecencyBand.HOURS_48_TO_72
            age <= 7 * DAY -> MuscleRecencyBand.DAYS_3_TO_7
            else -> MuscleRecencyBand.OVER_7_DAYS
        }
    }
}
