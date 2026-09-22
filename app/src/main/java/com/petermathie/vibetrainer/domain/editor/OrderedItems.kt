package com.petermathie.vibetrainer.domain.editor

fun <T> moveItem(
    items: List<T>,
    id: String,
    delta: Int,
    idOf: (T) -> String,
): List<T>? {
    val reordered = items.toMutableList()
    val from = reordered.indexOfFirst { idOf(it) == id }
    val to = from + delta
    if (from < 0 || to !in reordered.indices) return null
    java.util.Collections.swap(reordered, from, to)
    return reordered
}
