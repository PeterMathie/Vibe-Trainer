package com.petermathie.vibecheck.ui

import com.petermathie.vibecheck.ui.components.GraphDomain
import com.petermathie.vibecheck.ui.components.graphDomain
import com.petermathie.vibecheck.ui.components.graphPoint
import com.petermathie.vibecheck.ui.components.nearestGraphIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VibeGraphTest {
    @Test
    fun domainIgnoresGapsAndRejectsNoFiniteData() {
        assertEquals(GraphDomain(2.0, 8.0), graphDomain(listOf(Double.NaN, 8.0, 2.0)))
        assertNull(graphDomain(listOf(Double.NaN, Double.POSITIVE_INFINITY)))
    }

    @Test
    fun onePointIsCenteredAndNearestSelectionIsClamped() {
        val point = graphPoint(0, 5.0, 1, 200f, 100f, GraphDomain(0.0, 10.0), 12f)
        assertEquals(100f, point.x)
        assertTrue(point.y in 12f..88f)
        assertEquals(0, nearestGraphIndex(-20f, 200f, 3))
        assertEquals(2, nearestGraphIndex(300f, 200f, 3))
    }

    @Test
    fun fixedDomainPreservesBarChartScale() {
        assertEquals(GraphDomain(0.0, 10.0), graphDomain(listOf(4.0), 0.0..10.0))
    }
}
