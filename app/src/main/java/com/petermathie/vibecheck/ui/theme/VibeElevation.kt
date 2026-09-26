package com.petermathie.vibecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class VibeSurfaceLevel { PAGE, INSET, CARD, RAISED, SELECTED, FLOATING, MODAL }

enum class VibeSurfaceState { RESTING, PRESSED, DRAGGED, SELECTED, DISABLED }

@Immutable
data class VibeElevation(
    val shadow: Dp,
    val borderAlpha: Float,
    val topEdgeAlpha: Float,
)

object VibeElevations {
    fun resolve(level: VibeSurfaceLevel, state: VibeSurfaceState, dark: Boolean): VibeElevation {
        if (state == VibeSurfaceState.DISABLED) return VibeElevation(0.dp, 0.06f, 0f)
        if (state == VibeSurfaceState.PRESSED) return VibeElevation(0.dp, 0.14f, 0.03f)
        if (state == VibeSurfaceState.DRAGGED) return VibeElevation(8.dp, 0.55f, 0.08f)
        return when (level) {
            VibeSurfaceLevel.PAGE -> VibeElevation(0.dp, 0f, 0f)
            VibeSurfaceLevel.INSET -> VibeElevation(0.dp, if (dark) 0.05f else 0.08f, 0f)
            VibeSurfaceLevel.CARD -> VibeElevation(0.dp, if (dark) 0.09f else 0.11f, if (dark) 0.05f else 0f)
            VibeSurfaceLevel.RAISED -> VibeElevation(1.dp, 0.12f, if (dark) 0.08f else 0.03f)
            VibeSurfaceLevel.SELECTED -> VibeElevation(2.dp, if (dark) 0.55f else 0.65f, 0.08f)
            VibeSurfaceLevel.FLOATING -> VibeElevation(10.dp, 0.14f, if (dark) 0.10f else 0.04f)
            VibeSurfaceLevel.MODAL -> VibeElevation(16.dp, 0.16f, if (dark) 0.12f else 0.04f)
        }
    }
}

val LocalVibeSurfaceLevel = staticCompositionLocalOf { VibeSurfaceLevel.PAGE }
