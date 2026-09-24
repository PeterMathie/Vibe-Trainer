package com.petermathie.vibecheck.domain.programme

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgrammePrescriptionTest {
    @Test
    fun summaryUsesAssignmentTargets() {
        assertEquals(
            "4 sets · 6–10 reps · RPE 8.5",
            prescriptionSummary(4, 6, 10, null, 8.5),
        )
        assertEquals(
            "2 sets · 30 seconds · RPE 7",
            prescriptionSummary(2, null, null, 30, 7.0),
        )
    }
}
