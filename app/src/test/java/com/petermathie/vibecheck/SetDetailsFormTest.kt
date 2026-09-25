package com.petermathie.vibecheck

import com.petermathie.vibecheck.domain.workout.SetDetailsForm
import com.petermathie.vibecheck.ui.emptyEntryDraft
import com.petermathie.vibecheck.ui.emptySet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SetDetailsFormTest {
    @Test
    fun weightedSetPreservesMetadataAndConvertsPounds() {
        val original = emptySet("exercise", 3).copy(id = "set", notes = "keep", loggedAt = 42)
        val result = SetDetailsForm(
            performance = "220.46226218 x 5",
            rpe = "8.5",
            warmUp = true,
            failed = false,
            variationId = "variation",
            leftValue = "",
            rightValue = "",
            addedWeight = "2.5",
            assistance = "1",
            romValue = "12",
            romUnit = "cm",
        ).buildSet(original, hold = false, weighted = true, pounds = true, updatedAt = 99)

        assertNull(result.error)
        assertEquals("set", result.set?.id)
        assertEquals("keep", result.set?.notes)
        assertEquals(42L, result.set?.loggedAt)
        assertEquals(99L, result.set?.updatedAt)
        assertEquals(100.0, result.set?.weightKg ?: 0.0, 0.00001)
        assertEquals(5.0, result.set?.reps)
        assertEquals(8.5, result.set?.rpe ?: 0.0, 0.0)
        assertEquals("WARM_UP", result.set?.setType)
        assertEquals("variation", result.set?.variationId)
    }

    @Test
    fun unilateralHoldReplacesBilateralResult() {
        val result = SetDetailsForm(
            performance = "30",
            rpe = "",
            warmUp = false,
            failed = false,
            variationId = null,
            leftValue = "10.5",
            rightValue = "12",
            addedWeight = "",
            assistance = "",
            romValue = "",
            romUnit = "degrees",
        ).buildSet(emptySet("exercise", 1), hold = true, weighted = false, pounds = false)

        assertNull(result.error)
        assertNull(result.set?.holdMillis)
        assertEquals(10_500L, result.set?.leftHoldMillis)
        assertEquals(12_000L, result.set?.rightHoldMillis)
    }

    @Test
    fun failureZerosPerformanceAndClearsRom() {
        val result = SetDetailsForm(
            performance = "invalid",
            rpe = "",
            warmUp = false,
            failed = true,
            variationId = null,
            leftValue = "4",
            rightValue = "5",
            addedWeight = "",
            assistance = "",
            romValue = "20",
            romUnit = "cm",
        ).buildSet(emptySet("exercise", 1), hold = false, weighted = false, pounds = false)

        assertNull(result.error)
        assertEquals("FAILED", result.set?.result)
        assertEquals(0.0, result.set?.reps)
        assertEquals(0.0, result.set?.leftReps)
        assertEquals(0.0, result.set?.rightReps)
        assertNull(result.set?.romValue)
    }

    @Test
    fun validationPreservesExistingPriorityAndMessages() {
        val original = emptySet("exercise", 1)
        val baseline = SetDetailsForm.from(emptyEntryDraft("exercise", 1))

        assertEquals("RPE must be 0–10", baseline.copy(rpe = "11", leftValue = "-1").buildSet(original, false, false, false).error)
        assertEquals("Use finite, non-negative numbers", baseline.copy(leftValue = "-1").buildSet(original, false, false, false).error)
        assertEquals(1.5, baseline.copy(leftValue = "1.5").buildSet(original, false, false, false).set?.leftReps)
        assertEquals("Enter a valid result", baseline.buildSet(original, false, false, false).error)
    }

    @Test
    fun draftRoundTripKeepsPersistenceFields() {
        val original = emptyEntryDraft("exercise", 2).copy(setId = "set", bandIds = "[\"band\"]", detailsOpen = true)
        val updated = SetDetailsForm.from(original).copy(performance = "8", rpe = "7").applyTo(original)

        assertEquals("8", updated.performance)
        assertEquals("7", updated.rpe)
        assertEquals("set", updated.setId)
        assertEquals("[\"band\"]", updated.bandIds)
        assertEquals(original.detailsOpen, updated.detailsOpen)
    }
}
