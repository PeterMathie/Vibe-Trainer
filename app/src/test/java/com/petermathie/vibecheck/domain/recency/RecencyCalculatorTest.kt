package com.petermathie.vibecheck.domain.recency

import com.petermathie.vibecheck.domain.model.MuscleRecencyBand
import org.junit.Assert.assertEquals
import org.junit.Test

class RecencyCalculatorTest {
    private val now = 10L * 24 * 60 * 60 * 1000
    private val hour = 60L * 60 * 1000

    @Test fun neverHasDistinctState() = assertEquals(MuscleRecencyBand.NEVER, RecencyCalculator.band(null, now))
    @Test fun under24HoursIsRedBand() = assertEquals(MuscleRecencyBand.UNDER_24_HOURS, RecencyCalculator.band(now - 23 * hour, now))
    @Test fun exactly24HoursMovesToOrangeBand() = assertEquals(MuscleRecencyBand.HOURS_24_TO_48, RecencyCalculator.band(now - 24 * hour, now))
    @Test fun overSevenDaysIsNeglectedBand() = assertEquals(MuscleRecencyBand.OVER_7_DAYS, RecencyCalculator.band(now - 8 * 24 * hour, now))
}
