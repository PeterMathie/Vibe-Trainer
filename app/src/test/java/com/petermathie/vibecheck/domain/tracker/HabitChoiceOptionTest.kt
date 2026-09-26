package com.petermathie.vibecheck.domain.tracker

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitChoiceOptionTest {
    @Test
    fun legacyCountsPreservePreviousVisibleIntensity() {
        assertEquals(emptyList<HabitChoiceIntensity>(), migrate(0))
        assertEquals(listOf(HabitChoiceIntensity.LIGHT), migrate(1))
        assertEquals(listOf(HabitChoiceIntensity.LIGHT, HabitChoiceIntensity.DARK), migrate(2))
        assertEquals(
            listOf(HabitChoiceIntensity.LIGHT, HabitChoiceIntensity.MEDIUM, HabitChoiceIntensity.DARK),
            migrate(3),
        )
        assertEquals(
            listOf(
                HabitChoiceIntensity.LIGHT,
                HabitChoiceIntensity.LIGHT,
                HabitChoiceIntensity.MEDIUM,
                HabitChoiceIntensity.MEDIUM,
                HabitChoiceIntensity.DARK,
                HabitChoiceIntensity.DARK,
            ),
            migrate(6),
        )
    }

    @Test
    fun explicitChoicesRoundTripStableIdentityBucketAndOrder() {
        val choices = listOf(
            HabitChoiceOption("field:one", "Same", HabitChoiceIntensity.LIGHT, 0),
            HabitChoiceOption("field:two", "Other", HabitChoiceIntensity.DARK, 0),
        )

        assertEquals(choices, decodeHabitChoices(encodeHabitChoices(choices)))
    }

    @Test
    fun groupsAllowEmptyOneOrManyAndMovingChangesOnlySelectedChoice() {
        val original = listOf(
            HabitChoiceOption("a", "A", HabitChoiceIntensity.LIGHT, 0),
            HabitChoiceOption("b", "B", HabitChoiceIntensity.LIGHT, 1),
            HabitChoiceOption("c", "C", HabitChoiceIntensity.DARK, 0),
        )

        val moved = moveHabitChoice(original, "b", HabitChoiceIntensity.MEDIUM, 0)

        assertEquals(
            listOf(
                HabitChoiceOption("a", "A", HabitChoiceIntensity.LIGHT, 0),
                HabitChoiceOption("b", "B", HabitChoiceIntensity.MEDIUM, 0),
                HabitChoiceOption("c", "C", HabitChoiceIntensity.DARK, 0),
            ),
            moved,
        )
    }

    @Test
    fun deletionPreservesEverySurvivingBucketAndRelativeOrder() {
        val original = listOf(
            HabitChoiceOption("a", "A", HabitChoiceIntensity.LIGHT, 0),
            HabitChoiceOption("b", "B", HabitChoiceIntensity.MEDIUM, 0),
            HabitChoiceOption("c", "C", HabitChoiceIntensity.MEDIUM, 1),
            HabitChoiceOption("d", "D", HabitChoiceIntensity.DARK, 0),
        )

        val remaining = normalizeHabitChoices(original.filterNot { it.id == "b" })

        assertEquals(
            listOf(
                HabitChoiceOption("a", "A", HabitChoiceIntensity.LIGHT, 0),
                HabitChoiceOption("c", "C", HabitChoiceIntensity.MEDIUM, 0),
                HabitChoiceOption("d", "D", HabitChoiceIntensity.DARK, 0),
            ),
            remaining,
        )
    }

    @Test
    fun deletedLegacyOptionGetsStableLightSnapshotMatchingOldFallback() {
        val first = legacyHabitChoiceSnapshot("field", "Deleted", emptyList())
        val second = legacyHabitChoiceSnapshot("field", "Deleted", emptyList())

        assertEquals(first.id, second.id)
        assertEquals(HabitChoiceIntensity.LIGHT, first.intensity)
        assertEquals("Deleted", first.label)
    }

    private fun migrate(count: Int) = legacyHabitChoices(
        fieldId = "field",
        choiceOptions = (0 until count).joinToString("\n") { "Choice $it" },
        lightThrough = -1,
        darkFrom = -1,
    ).map(HabitChoiceOption::intensity)
}
