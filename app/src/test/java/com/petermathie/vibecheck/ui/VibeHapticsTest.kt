package com.petermathie.vibecheck.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.junit.Assert.assertEquals
import org.junit.Test

class VibeHapticsTest {
    @Test
    fun disabledPreferenceSuppressesFeedback() {
        val recorded = mutableListOf<HapticFeedbackType>()
        val haptics = VibeHaptics(
            enabled = { false },
            feedback = recordingFeedback(recorded),
            nowMillis = { 100L },
        )

        haptics.perform(VibeHapticEvent.SUCCESS)

        assertEquals(emptyList<HapticFeedbackType>(), recorded)
    }

    @Test
    fun rapidSelectionTicksAreThrottledButFinalTicksRemainAvailable() {
        val recorded = mutableListOf<HapticFeedbackType>()
        var now = 100L
        val haptics = VibeHaptics(
            enabled = { true },
            feedback = recordingFeedback(recorded),
            nowMillis = { now },
        )

        haptics.perform(VibeHapticEvent.SELECTION)
        now += 20
        haptics.perform(VibeHapticEvent.SELECTION)
        now += 50
        haptics.perform(VibeHapticEvent.SELECTION)

        assertEquals(
            listOf(HapticFeedbackType.SegmentTick, HapticFeedbackType.SegmentTick),
            recorded,
        )
    }

    private fun recordingFeedback(recorded: MutableList<HapticFeedbackType>) =
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                recorded += hapticFeedbackType
            }
        }
}
