package com.petermathie.vibetrainer

import com.petermathie.vibetrainer.domain.workout.CompactEntryForm
import com.petermathie.vibetrainer.ui.emptyEntryDraft
import com.petermathie.vibetrainer.ui.emptySet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompactEntryFormTest {
    @Test
    fun weightedEntryConvertsPoundsAndAppliesRpe() {
        val set = CompactEntryForm("220.46226218 x 5", "8.5")
            .buildSet(emptySet("exercise", 1), hold = false, weighted = true, pounds = true)

        assertEquals(100.0, set?.weightKg ?: 0.0, 0.00001)
        assertEquals(5, set?.reps)
        assertEquals(8.5, set?.rpe ?: 0.0, 0.0)
    }

    @Test
    fun holdEntryAndRpeValidationMatchCompactSubmission() {
        val base = emptySet("exercise", 1)

        assertEquals(
            12_500L,
            CompactEntryForm("12.5", "").buildSet(base, true, false, false)?.holdMillis,
        )
        assertNull(CompactEntryForm("12.5", "-1").buildSet(base, true, false, false))
        assertNull(CompactEntryForm("12.5", "11").buildSet(base, true, false, false))
        assertNull(CompactEntryForm("invalid", "8").buildSet(base, true, false, false))
    }

    @Test
    fun draftRoundTripChangesOnlyCompactValues() {
        val original = emptyEntryDraft("exercise", 2).copy(
            detailsOpen = true,
            bandIds = "[\"band\"]",
        )
        val updated = CompactEntryForm("10", "7").applyTo(original)

        assertEquals("10", updated.performance)
        assertEquals("7", updated.rpe)
        assertEquals(true, updated.detailsOpen)
        assertEquals("[\"band\"]", updated.bandIds)
    }
}
