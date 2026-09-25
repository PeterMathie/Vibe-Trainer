package com.petermathie.vibecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class VibeMotionTokens(
    val pressInMillis: Int,
    val pressOutMillis: Int,
    val selectionMillis: Int,
    val expandMillis: Int,
    val pageEnterMillis: Int,
    val pageExitMillis: Int,
    val graphRevealMillis: Int,
    val travelDp: Int,
)

object VibeMotion {
    fun resolve(reducedMotion: Boolean): VibeMotionTokens =
        if (reducedMotion) {
            VibeMotionTokens(0, 0, 0, 0, 80, 80, 0, 0)
        } else {
            VibeMotionTokens(70, 110, 140, 180, 180, 120, 280, 8)
        }
}

val LocalVibeMotion = staticCompositionLocalOf { VibeMotion.resolve(reducedMotion = false) }
