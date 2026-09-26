package com.petermathie.vibecheck.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionCelebrationTest {
    @Test
    fun reducedMotionRemovesStaggerAndConfetti() {
        val reduced = completionAnimationPlan(true)
        assertFalse(reduced.showConfetti)
        assertTrue(reduced.muscleStaggerMillis == 0L)

        val animated = completionAnimationPlan(false)
        assertTrue(animated.showConfetti)
        assertTrue(animated.muscleStaggerMillis in 40L..80L)
    }
}
