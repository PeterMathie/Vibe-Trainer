package com.petermathie.vibetrainer

import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import com.petermathie.vibetrainer.domain.tracker.HabitFieldForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitFieldFormTest {
    private val field = TrackerFieldEntity(
        id = "field",
        trackerId = "tracker",
        name = "Minutes",
        valueType = "NUMBER",
        unit = "min",
        targetComparison = "AT_LEAST",
        targetValue = 20.0,
        position = 3,
        targetMaxValue = null,
    )

    @Test
    fun numericRangeRoundTripsWithoutChangingIdentityOrPosition() {
        val updated = HabitFieldForm.from(field).copy(
            target = "10",
            targetMaximum = "30",
            comparison = HabitFieldForm.RANGE,
        ).applyTo(field)

        assertEquals("field", updated.id)
        assertEquals("tracker", updated.trackerId)
        assertEquals(3, updated.position)
        assertEquals(10.0, updated.targetValue ?: 0.0, 0.0)
        assertEquals(30.0, updated.targetMaxValue ?: 0.0, 0.0)
        assertEquals(HabitFieldForm.RANGE, updated.targetComparison)
    }

    @Test
    fun rangeAndNumericValidationPreserveExistingRules() {
        val form = HabitFieldForm.from(field).copy(
            target = "20",
            targetMaximum = "10",
            comparison = HabitFieldForm.RANGE,
        )

        assertFalse(form.isRangeValid)
        assertFalse(form.canSave)
        assertFalse(form.copy(target = "NaN").isTargetValid)
        assertTrue(form.copy(target = "").isRangeValid)
    }

    @Test
    fun choicesRequireTwoUniqueValuesAndAreNormalised() {
        val form = HabitFieldForm.from(field).copy(
            type = HabitFieldForm.CHOICE,
            options = "Good, Bad\nGood",
        )

        assertFalse(form.areChoicesValid)
        val saved = form.copy(options = " Good, Bad \n").applyTo(field)
        assertEquals("Good\nBad", saved.choiceOptions)
        assertNull(saved.targetValue)
        assertNull(saved.targetComparison)
    }

    @Test
    fun nonChoiceTypeClearsStaleChoicesAndTargets() {
        val saved = HabitFieldForm.from(
            field.copy(choiceOptions = "A\nB", targetMaxValue = 30.0),
        ).copy(type = "TEXT").applyTo(field)

        assertEquals("", saved.choiceOptions)
        assertNull(saved.targetValue)
        assertNull(saved.targetComparison)
        assertNull(saved.targetMaxValue)
    }
}
