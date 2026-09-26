package com.petermathie.vibecheck.domain.recency

import com.petermathie.vibecheck.domain.model.MuscleRecencyBand
import org.junit.Assert.assertEquals
import org.junit.Test

class RecencyCalculatorTest {
    private val now = 10L * 24 * 60 * 60 * 1000
    private val hour = 60L * 60 * 1000

    @Test fun neverHasDistinctState() = assertEquals(MuscleRecencyBand.NEVER, RecencyCalculator.band(null, now))
    @Test fun ageBoundariesMapToOrderedFreshnessBands() {
        assertEquals(MuscleRecencyBand.UNDER_24_HOURS, RecencyCalculator.band(now - 23 * hour, now))
        assertEquals(MuscleRecencyBand.HOURS_24_TO_48, RecencyCalculator.band(now - 24 * hour, now))
        assertEquals(MuscleRecencyBand.HOURS_48_TO_72, RecencyCalculator.band(now - 48 * hour, now))
        assertEquals(MuscleRecencyBand.DAYS_3_TO_7, RecencyCalculator.band(now - 72 * hour, now))
        assertEquals(MuscleRecencyBand.DAYS_3_TO_7, RecencyCalculator.band(now - 7 * 24 * hour, now))
        assertEquals(MuscleRecencyBand.OVER_7_DAYS, RecencyCalculator.band(now - 7 * 24 * hour - 1, now))
    }
}
