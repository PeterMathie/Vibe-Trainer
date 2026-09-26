package com.petermathie.vibecheck.ui

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

enum class VibeHapticEvent {
    SUCCESS,
    SELECTION,
    DRAG_START,
    DRAG_CROSS,
    DRAG_DROP,
    EDIT,
    PLAY,
    NAVIGATION,
}

class VibeHaptics internal constructor(
    private val enabled: () -> Boolean,
    private val feedback: HapticFeedback,
    private val nowMillis: () -> Long = SystemClock::elapsedRealtime,
) {
    private var lastEventAt = Long.MIN_VALUE

    fun perform(event: VibeHapticEvent) {
        if (!enabled()) return
        val now = nowMillis()
        val minimumGap = when (event) {
            VibeHapticEvent.SELECTION, VibeHapticEvent.DRAG_CROSS -> 50L
            VibeHapticEvent.SUCCESS -> 250L
            else -> 0L
        }
        if (lastEventAt != Long.MIN_VALUE && now - lastEventAt < minimumGap) return
        lastEventAt = now
        feedback.performHapticFeedback(
            when (event) {
                VibeHapticEvent.SUCCESS -> HapticFeedbackType.Confirm
                VibeHapticEvent.SELECTION, VibeHapticEvent.NAVIGATION -> HapticFeedbackType.SegmentTick
                VibeHapticEvent.DRAG_START -> HapticFeedbackType.GestureThresholdActivate
                VibeHapticEvent.DRAG_CROSS -> HapticFeedbackType.SegmentFrequentTick
                VibeHapticEvent.DRAG_DROP -> HapticFeedbackType.GestureEnd
                VibeHapticEvent.EDIT, VibeHapticEvent.PLAY -> HapticFeedbackType.ContextClick
            },
        )
    }
}

@Composable
fun rememberVibeHaptics(): VibeHaptics {
    val context = LocalContext.current
    val feedback = LocalHapticFeedback.current
    val preferences = remember(context) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }
    return remember(preferences, feedback) {
        VibeHaptics(
            enabled = { preferences.getBoolean("haptic", true) },
            feedback = feedback,
        )
    }
}
