package com.petermathie.vibecheck

import com.petermathie.vibecheck.domain.editor.moveItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderedItemsTest {
    private data class Item(val id: String)
    private val items = listOf(Item("a"), Item("b"), Item("c"))

    @Test
    fun movesItemUpAndDownWithoutMutatingInput() {
        assertEquals(listOf("b", "a", "c"), moveItem(items, "b", -1, Item::id)?.map(Item::id))
        assertEquals(listOf("a", "c", "b"), moveItem(items, "b", 1, Item::id)?.map(Item::id))
        assertEquals(listOf("a", "b", "c"), items.map(Item::id))
    }

    @Test
    fun rejectsBoundariesAndMissingIds() {
        assertNull(moveItem(items, "a", -1, Item::id))
        assertNull(moveItem(items, "c", 1, Item::id))
        assertNull(moveItem(items, "missing", 1, Item::id))
    }
}
