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
    fun bodyTransformIsTenPercentTallerAndFitsMeasuredBounds() {
        val transform = muscleMapTransform(
            canvasWidth = 560f,
            canvasHeight = 1_000f,
            contentLeft = 200f,
            contentRight = 824f,
            contentTop = 70f,
            contentBottom = 1_420f,
        )
        assertEquals(MUSCLE_MAP_VERTICAL_STRETCH, transform.scaleY / transform.scaleX, 0.001f)
        assertTrue(transform.offsetX + 200f * transform.scaleX >= -0.01f)
        assertTrue(transform.offsetX + 824f * transform.scaleX <= 560.01f)
        val renderedTop = transform.offsetY + 70f * transform.scaleY
        val renderedBottom = transform.offsetY + 1_420f * transform.scaleY
        assertTrue("top=$renderedTop transform=$transform", renderedTop >= -0.1f)
        assertTrue("bottom=$renderedBottom transform=$transform", renderedBottom <= 1_000.1f)
    }

    @Test
    fun heightConstrainedBodyTransformFitsFullAnatomyBounds() {
        val transform = muscleMapTransform(
            canvasWidth = 900f,
            canvasHeight = 420f,
            contentLeft = 200f,
            contentRight = 824f,
            contentTop = 70f,
            contentBottom = 1_420f,
        )
        assertTrue(transform.offsetY + 70f * transform.scaleY >= -0.01f)
        assertTrue(transform.offsetY + 1_420f * transform.scaleY <= 420.01f)
        assertEquals(MUSCLE_MAP_VERTICAL_STRETCH, transform.scaleY / transform.scaleX, 0.001f)
    }
}
