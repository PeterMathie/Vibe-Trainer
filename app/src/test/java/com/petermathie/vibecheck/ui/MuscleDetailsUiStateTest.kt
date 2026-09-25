package com.petermathie.vibecheck.ui

import androidx.compose.ui.graphics.Color
import com.petermathie.vibecheck.domain.model.MuscleRecency
import com.petermathie.vibecheck.domain.model.MuscleRecencyBand
import com.petermathie.vibecheck.domain.model.TrainingMode
import com.petermathie.vibecheck.ui.components.muscleDetailsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleDetailsUiStateTest {
    @Test
    fun mapsStrengthFactsWithoutInventingActions() {
        val state = muscleDetailsUiState(
            "CHEST",
            MuscleRecency("CHEST", 1_700_000_000_000, MuscleRecencyBand.UNDER_24_HOURS, listOf("Push up"), 3.5),
            TrainingMode.STRENGTH,
            Color.Red,
        )
        assertEquals("Chest Freshness details", state.title)
        assertEquals("Under 24 hours", state.bandLabel)
        assertEquals("3.5 set-equivalents in 7 days", state.doseLabel)
        assertEquals(listOf("Push up"), state.contributions)
    }

    @Test
    fun mapsStretchNoDataTruthfully() {
        val state = muscleDetailsUiState("HAMSTRINGS", null, TrainingMode.STRETCHING, Color.Gray)
        assertEquals("Hamstrings Stretch details", state.title)
        assertEquals("No data", state.bandLabel)
        assertTrue(state.lastEventLabel.contains("Last stretched: No data"))
    }
}
