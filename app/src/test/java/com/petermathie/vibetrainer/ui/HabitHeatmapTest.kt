package com.petermathie.vibetrainer.ui

import com.petermathie.vibetrainer.data.local.TrackerDailyValueEntity
import com.petermathie.vibetrainer.data.local.TrackerEntity
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitHeatmapTest {
    private val tracker = TrackerEntity("tracker", "Mood", false)

    @Test
    fun orderedChoicesMapFromLightToDark() {
        val field = field(
            "CHOICE",
            "Sad\nTired\nIrritated\nTense\nNeutral\nCalm\nAlert\nHappy\nExcited",
        ).copy(
            choiceLightThrough = 2,
            choiceDarkFrom = 6,
        )

        assertEquals(1, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Sad"))))
        assertEquals(1, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Irritated"))))
        assertEquals(2, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Tense"))))
        assertEquals(2, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Calm"))))
        assertEquals(3, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Alert"))))
        assertEquals(3, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Happy"))))
        assertEquals(3, habitHeatmapLevel(tracker, listOf(field), listOf(value(text = "Excited"))))
    }

    @Test
    fun yesNoAndWrittenEntriesHaveUsefulShades() {
        val boolean = field("BOOLEAN")
        val text = field("TEXT")

        assertEquals(1, habitHeatmapLevel(tracker, listOf(boolean), listOf(value(boolean = false))))
        assertEquals(3, habitHeatmapLevel(tracker, listOf(boolean), listOf(value(boolean = true))))
        assertEquals(2, habitHeatmapLevel(tracker, listOf(text), listOf(value(text = "Journal entry"))))
    }

    private fun field(type: String, options: String = "") = TrackerFieldEntity(
        id = "field",
        trackerId = tracker.id,
        name = "Value",
        valueType = type,
        unit = null,
        targetComparison = null,
        targetValue = null,
        position = 0,
        choiceOptions = options,
    )

    private fun value(
        text: String? = null,
        boolean: Boolean? = null,
    ) = TrackerDailyValueEntity("field", 1, null, boolean, text, "", 1)
}
