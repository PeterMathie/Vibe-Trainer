package com.petermathie.vibecheck.ui.anatomy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleMapMotionTest {
    @Test
    fun colourTransitionIsVisibleButFastEnoughForScrubbing() {
        assertTrue(MUSCLE_COLOR_TRANSITION_MILLIS in 30..40)
    }

    @Test
    fun bodyTransformIsTenPercentTallerWithoutChangingRequestedWidth() {
        val transform = muscleMapTransform(
            canvasWidth = 560f,
            canvasHeight = 1_000f,
            diagramWidth = 1_024f,
            contentTop = 70f,
            contentBottom = 1_420f,
        )
        assertEquals(MUSCLE_MAP_VERTICAL_STRETCH, transform.scaleY / transform.scaleX, 0.001f)
        assertEquals(560f * MUSCLE_MAP_HORIZONTAL_SCALE, 1_024f * transform.scaleX, 0.01f)
        assertTrue(transform.offsetY + 70f * transform.scaleY >= 0f)
        assertTrue(transform.offsetY + 1_420f * transform.scaleY <= 1_000.01f)
    }

    @Test
    fun heightConstrainedBodyTransformFitsFullAnatomyBounds() {
        val transform = muscleMapTransform(
            canvasWidth = 900f,
            canvasHeight = 420f,
            diagramWidth = 1_024f,
            contentTop = 70f,
            contentBottom = 1_420f,
        )
        assertTrue(transform.offsetY + 70f * transform.scaleY >= -0.01f)
        assertTrue(transform.offsetY + 1_420f * transform.scaleY <= 420.01f)
        assertEquals(MUSCLE_MAP_VERTICAL_STRETCH, transform.scaleY / transform.scaleX, 0.001f)
    }
}
