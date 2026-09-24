package com.petermathie.vibecheck.domain.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressScorerTest {
    @Test fun weightedRepsUsesEpleyFormula() {
        assertEquals(79.333, ProgressScorer.weightedReps(70.0, 4), 0.001)
    }

    @Test fun widerBandAlwaysReducesComparableScore() {
        val yellow = ProgressScorer.assistedReps(reps = 6, totalBandWidthCm = 0.6)
        val purple = ProgressScorer.assistedReps(reps = 6, totalBandWidthCm = 3.1)
        assert(yellow > purple)
    }

    @Test fun stackedBandWidthsUseTheirSum() {
        val explicit = ProgressScorer.assistedHold(8_000, 0.6 + 2.2 + 3.1)
        val summed = ProgressScorer.assistedHold(8_000, 5.9)
        assertEquals(explicit, summed, 0.00001)
    }

    @Test fun baselineWaitsForThreeSessions() {
        assertNull(ProgressScorer.progressIndex(12.0, listOf(10.0, 11.0)))
        assertEquals(120.0, ProgressScorer.progressIndex(12.0, listOf(10.0, 10.0, 10.0))!!, 0.001)
    }
}
