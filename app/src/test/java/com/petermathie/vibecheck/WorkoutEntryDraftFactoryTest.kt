package com.petermathie.vibecheck

import com.petermathie.vibecheck.ui.emptyEntryDraft
import com.petermathie.vibecheck.ui.newSetEntryDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEntryDraftFactoryTest {
    @Test
    fun firstPlancheSetUsesTheDefaultVariation() {
        val draft = newSetEntryDraft(
            workoutExerciseId = "planche-row",
            ordinal = 1,
            previous = null,
            defaultVariationId = "planche-tuck",
            validVariationIds = setOf("planche-tuck", "planche-full"),
        )

        assertEquals("planche-tuck", draft.variationId)
        assertBlankInputs(draft)
    }

    @Test
    fun subsequentPlancheSetInheritsOnlyPreviousVariation() {
        val previous = emptyEntryDraft("planche-row", 1).copy(
            variationId = "planche-full",
            performance = "8",
            rpe = "9",
            bandIds = "[\"band:green\"]",
            timeHeld = "12",
            timeUnderTension = "30",
            addedWeight = "5",
        )

        val draft = newSetEntryDraft(
            workoutExerciseId = "planche-row",
            ordinal = 2,
            previous = previous,
            defaultVariationId = "planche-tuck",
            validVariationIds = setOf("planche-tuck", "planche-full"),
        )

        assertEquals("planche-full", draft.variationId)
        assertBlankInputs(draft)
    }

    @Test
    fun bandAssistedMuscleUpSetDoesNotCopyBandOrNumericInput() {
        val previous = emptyEntryDraft("muscle-up-row", 1).copy(
            performance = "8",
            rpe = "9",
            bandIds = "[\"band:green\"]",
            timeUnderTension = "30",
        )

        val draft = newSetEntryDraft(
            workoutExerciseId = "muscle-up-row",
            ordinal = 2,
            previous = previous,
            defaultVariationId = null,
            validVariationIds = emptySet(),
        )

        assertEquals(null, draft.variationId)
        assertBlankInputs(draft)
    }

    @Test
    fun setAfterRestoredDraftInheritsItsVariationOnly() {
        val restored = emptyEntryDraft("planche-row", 2).copy(
            variationId = "planche-full",
            performance = "6",
            timeHeld = "6",
            rpe = "8",
        )

        val draft = newSetEntryDraft(
            workoutExerciseId = "planche-row",
            ordinal = 3,
            previous = restored,
            defaultVariationId = "planche-tuck",
            validVariationIds = setOf("planche-tuck", "planche-full"),
        )

        assertEquals("planche-full", draft.variationId)
        assertBlankInputs(draft)
    }

    @Test
    fun exerciseSwitchRejectsVariationFromPreviousExercise() {
        val planche = emptyEntryDraft("shared-row", 1).copy(variationId = "planche-full")

        val muscleUp = newSetEntryDraft(
            workoutExerciseId = "shared-row",
            ordinal = 1,
            previous = planche,
            defaultVariationId = "muscle-up-band",
            validVariationIds = setOf("muscle-up-band", "muscle-up-strict"),
        )

        assertEquals("muscle-up-band", muscleUp.variationId)
        assertBlankInputs(muscleUp)
    }

    private fun assertBlankInputs(draft: com.petermathie.vibecheck.data.local.WorkoutEntryDraftEntity) {
        assertTrue(draft.performance.isBlank())
        assertTrue(draft.rpe.isBlank())
        assertEquals("[]", draft.bandIds)
        assertTrue(draft.timeHeld.isBlank())
        assertTrue(draft.timeUnderTension.isBlank())
        assertTrue(draft.addedWeight.isBlank())
        assertTrue(draft.leftValue.isBlank())
        assertTrue(draft.rightValue.isBlank())
        assertTrue(draft.assistance.isBlank())
        assertTrue(draft.romValue.isBlank())
    }
}
