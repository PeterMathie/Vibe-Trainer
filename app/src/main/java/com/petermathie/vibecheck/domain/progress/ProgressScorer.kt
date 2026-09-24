package com.petermathie.vibecheck.domain.progress

import kotlin.math.max

/** Pure scoring functions. Raw performance is always retained beside the derived score. */
object ProgressScorer {
    fun weightedReps(weightKg: Double, reps: Int): Double =
        weightKg * (1.0 + max(0, reps) / 30.0)

    /**
     * Band width is a relative assistance proxy because the user's bands share material,
     * thickness and length. Scores are comparable only within one exercise variation.
     */
    fun assistedReps(reps: Int, totalBandWidthCm: Double): Double =
        assistanceDifficulty(totalBandWidthCm) * (1.0 + max(0, reps) / 30.0)

    fun assistedHold(holdMillis: Long, totalBandWidthCm: Double): Double =
        assistanceDifficulty(totalBandWidthCm) * max(0L, holdMillis) / 1_000.0

    fun assistanceDifficulty(totalBandWidthCm: Double): Double =
        1.0 / (1.0 + max(0.0, totalBandWidthCm))

    fun progressIndex(sessionScore: Double, firstThreeSessionScores: List<Double>): Double? {
        if (firstThreeSessionScores.size < 3) return null
        val baseline = firstThreeSessionScores.take(3).average()
        return if (baseline <= 0.0) null else sessionScore / baseline * 100.0
    }
}
