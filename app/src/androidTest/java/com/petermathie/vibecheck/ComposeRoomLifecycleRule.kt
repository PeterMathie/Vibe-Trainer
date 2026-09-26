package com.petermathie.vibecheck

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.room.RoomDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ComposeRoomLifecycleRule(
    private val database: () -> RoomDatabase?,
) : TestRule {
    val compose: ComposeContentTestRule = createComposeRule()

    private val viewModelStore = ViewModelStore()
    private val viewModelJobs = mutableListOf<Job>()
    private var nextViewModelId = 0

    fun <T : ViewModel> own(viewModel: T): T = viewModel.also {
        viewModelStore.put("test-view-model-${nextViewModelId++}", it)
        viewModelJobs += it.viewModelScope.coroutineContext[Job]
            ?: error("Owned ViewModel has no scope job")
    }

    override fun apply(base: Statement, description: Description): Statement {
        val composeStatement = compose.apply(base, description)
        return object : Statement() {
            override fun evaluate() {
                try {
                    composeStatement.evaluate()
                } finally {
                    try {
                        viewModelStore.clear()
                        runBlocking { viewModelJobs.joinAll() }
                    } finally {
                        database()?.close()
                    }
                }
            }
        }
    }
}
