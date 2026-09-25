package com.petermathie.vibecheck.ui.anatomy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleMapMotionTest {
    @Test
    fun colourTransitionIsVisibleButFastEnoughForScrubbing() {
        assertTrue(MUSCLE_COLOR_TRANSITION_MILLIS in 30..40)
    }

    @Test
    fun neutralBodyOutlineIsHiddenForMuscleOnlyExperiment() {
        assertFalse(RENDER_NEUTRAL_BODY_OUTLINE)
    }
}
