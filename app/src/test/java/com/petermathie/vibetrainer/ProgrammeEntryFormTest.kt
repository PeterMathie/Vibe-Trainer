package com.petermathie.vibetrainer

import com.petermathie.vibetrainer.data.local.ProgrammeExerciseEntity
import com.petermathie.vibetrainer.domain.programme.ProgrammeEntryForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgrammeEntryFormTest {
    private val entry = ProgrammeExerciseEntity(
        id = "entry",
        programmeDayId = "day",
        exerciseId = "exercise",
        position = 0,
        targetSets = 3,
        targetRepsMin = 5,
        targetRepsMax = 8,
        targetHoldSeconds = null,
        restSeconds = 90,
        targetRpe = 7.5,
        notes = "Controlled",
        supersetGroup = "A",
    )

    @Test
    fun formRoundTripPreservesConfiguredTargets() {
        assertEquals(entry, ProgrammeEntryForm.from(entry).applyTo(entry))
    }

    @Test
    fun conversionPreservesExistingBlankMalformedDefaultAndClampRules() {
        val result = ProgrammeEntryForm(
            sets = "",
            minimumReps = "invalid",
            maximumReps = "",
            holdSeconds = "30",
            restSeconds = "invalid",
            targetRpe = "12.5",
            group = " ",
            notes = "Updated",
        ).applyTo(entry)

        assertNull(result.targetSets)
        assertNull(result.targetRepsMin)
        assertNull(result.targetRepsMax)
        assertEquals(30, result.targetHoldSeconds)
        assertEquals(120, result.restSeconds)
        assertEquals(10.0, result.targetRpe ?: -1.0, 0.0)
        assertNull(result.supersetGroup)
        assertEquals("Updated", result.notes)

        val negative = ProgrammeEntryForm.from(entry).copy(restSeconds = "-5", targetRpe = "-1").applyTo(entry)
        assertEquals(0, negative.restSeconds)
        assertEquals(0.0, negative.targetRpe ?: -1.0, 0.0)
    }
}
