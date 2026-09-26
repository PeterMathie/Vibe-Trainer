package com.petermathie.vibecheck

import com.petermathie.vibecheck.domain.workout.CompactEntryForm
import com.petermathie.vibecheck.domain.workout.hasInvalidQuantitativeInput
import com.petermathie.vibecheck.domain.workout.quantitativeInput
import com.petermathie.vibecheck.ui.emptyEntryDraft
import com.petermathie.vibecheck.ui.emptySet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuantitativeInputTest {
    @Test
    fun dotAndCommaDecimalsAreEquivalent() {
        assertEquals(12.75, quantitativeInput("12.75").value ?: 0.0, 0.0)
        assertEquals(12.75, quantitativeInput("12,75").value ?: 0.0, 0.0)
    }

    @Test
    fun pasteAndMixedTextAreRejectedInsteadOfCoerced() {
        listOf("12 reps", "kg12", "1e3", "NaN", "Infinity", " 12", "12 ").forEach {
            assertFalse("$it should be invalid", quantitativeInput(it).isValid)
            assertNull(quantitativeInput(it).value)
        }
    }

    @Test
    fun multipleSeparatorsAndSignsAreRejected() {
        listOf("1.2.3", "1,2,3", "1,2.3", "-1", "+1").forEach {
            assertFalse("$it should be invalid", quantitativeInput(it).isValid)
        }
    }

    @Test
    fun emptyAndTrailingSeparatorRemainTransientDraftStates() {
        listOf("", "12.", "12,", ".", ",").forEach {
            assertTrue("$it should be transient", quantitativeInput(it).isTransient)
            assertNull(quantitativeInput(it).value)
        }
    }

    @Test
    fun hugeAndFieldSpecificOutOfRangeValuesAreRejected() {
        assertFalse(quantitativeInput("1000000001").isValid)
        assertFalse(quantitativeInput("10.1", maximum = 10.0).isValid)
        assertTrue(quantitativeInput("10", maximum = 10.0).isValid)
    }

    @Test
    fun malformedRestoredDraftCannotBuildASet() {
        val restored = emptyEntryDraft("exercise", 1).copy(performance = "6 reps")

        assertTrue(hasInvalidQuantitativeInput(restored))
        assertNull(
            CompactEntryForm.from(restored).buildSet(
                base = emptySet("exercise", 1),
                hold = false,
                weighted = false,
                pounds = false,
            ),
        )
    }

    @Test
    fun fractionalRepsAndLoadKeepTheirPrecisionAtTheModelBoundary() {
        val set = CompactEntryForm(performance = "72,125 x 2.75").buildSet(
            base = emptySet("exercise", 1),
            hold = false,
            weighted = true,
            pounds = false,
        )

        assertEquals(72.125, set?.weightKg ?: 0.0, 0.0)
        assertEquals(2.75, set?.reps ?: 0.0, 0.0)
    }
}
