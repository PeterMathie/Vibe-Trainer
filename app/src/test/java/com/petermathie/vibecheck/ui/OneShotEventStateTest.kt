package com.petermathie.vibecheck.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OneShotEventStateTest {
    private data class Event(override val id: String) : IdentifiedUiEvent

    @Test
    fun eventIsConsumedOnceByMatchingStableId() {
        val events = OneShotEventState<Event>()
        val event = Event("completion-1")

        events.emit(event)
        assertEquals(event, events.state.value)
        assertFalse(events.consume("another-event"))
        assertEquals(event, events.state.value)
        assertTrue(events.consume(event.id))
        assertNull(events.state.value)
        assertFalse(events.consume(event.id))
    }
}
