package com.petermathie.vibecheck.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VibeFoundationTokensTest {
    @Test
    fun reducedMotionRemovesTravelAndDecorativeDurations() {
        val resolved = VibeMotion.resolve(reducedMotion = true)
        assertEquals(0, resolved.travelDp)
        assertEquals(0, resolved.pressInMillis)
        assertEquals(0, resolved.selectionMillis)
        assertEquals(0, resolved.graphRevealMillis)
    }

    @Test
    fun elevationStatesResolveDeterministically() {
        assertEquals(
            0.dp,
            VibeElevations.resolve(VibeSurfaceLevel.CARD, VibeSurfaceState.RESTING, dark = true).shadow,
        )
        assertEquals(
            8.dp,
            VibeElevations.resolve(VibeSurfaceLevel.CARD, VibeSurfaceState.DRAGGED, dark = true).shadow,
        )
        assertEquals(
            0.dp,
            VibeElevations.resolve(VibeSurfaceLevel.MODAL, VibeSurfaceState.PRESSED, dark = false).shadow,
        )
        assertTrue(
            VibeElevations.resolve(VibeSurfaceLevel.SELECTED, VibeSurfaceState.SELECTED, dark = false).borderAlpha >= 0.65f,
        )
    }
}
