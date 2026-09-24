package com.petermathie.vibecheck.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReorderStateTest {
    @Test
    fun reverseMovementNearBoundaryDoesNotOscillateOrder() {
        var persistedMove: Triple<Any, Int, Int>? = null
        val state = ReorderState(listOf("first", "second")) { key, from, to ->
            persistedMove = Triple(key, from, to)
        }

        state.begin("first")
        state.dragBy(70f, 48f)
        assertEquals(listOf("second", "first"), state.ordered(listOf("first", "second")) { it })

        state.dragBy(-80f, 48f)
        assertEquals(listOf("second", "first"), state.ordered(listOf("first", "second")) { it })

        state.end()
        assertEquals(Triple("first", 0, 1), persistedMove)
    }
}
