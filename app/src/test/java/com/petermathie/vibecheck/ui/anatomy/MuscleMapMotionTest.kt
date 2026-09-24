package com.petermathie.vibecheck.ui.anatomy

import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleMapMotionTest {
    @Test
    fun colourTransitionIsVisibleButFastEnoughForScrubbing() {
        assertTrue(MUSCLE_COLOR_TRANSITION_MILLIS in 40..80)
    }
}
