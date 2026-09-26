package com.petermathie.vibecheck.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IdentifiedUiEvent {
    val id: String
}

class OneShotEventState<T : IdentifiedUiEvent> {
    private val mutable = MutableStateFlow<T?>(null)
    val state: StateFlow<T?> = mutable

    fun emit(event: T) {
        mutable.value = event
    }

    fun consume(id: String): Boolean {
        if (mutable.value?.id != id) return false
        mutable.value = null
        return true
    }
}
