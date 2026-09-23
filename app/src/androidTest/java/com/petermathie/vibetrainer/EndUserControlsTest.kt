package com.petermathie.vibetrainer

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.petermathie.vibetrainer.ui.HomeScreen
import com.petermathie.vibetrainer.ui.MainUiState
import com.petermathie.vibetrainer.ui.ReorderHandle
import com.petermathie.vibetrainer.ui.rememberReorderState
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EndUserControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun homeKeepsDateControlsTogetherWithoutRemovedHeadings() {
        compose.setContent {
            VibeTrainerTheme {
                HomeScreen(MainUiState(), {}, {})
            }
        }

        compose.onNodeWithText("Overview").assertDoesNotExist()
        compose.onNodeWithText("Recency, not recovery or fatigue").assertDoesNotExist()
        compose.onNodeWithText("Habits, workouts and stretching").assertDoesNotExist()
        compose.onNodeWithContentDescription("Activity date navigation").assertExists()
        compose.onNodeWithText("Earlier").assertExists()
        compose.onNodeWithText("Later").assertExists()
    }

    @Test
    fun dragHandleCommitsOneCompletedMove() {
        val moves = mutableListOf<Triple<Any, Int, Int>>()
        compose.setContent {
            VibeTrainerTheme {
                val state = rememberReorderState(listOf("one", "two")) { key, from, to ->
                    moves += Triple(key, from, to)
                }
                Column {
                    ReorderHandle(state, "one", "one")
                    ReorderHandle(state, "two", "two")
                }
            }
        }

        compose.onNodeWithContentDescription("Reorder one").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            moveBy(Offset(0f, 300f))
            advanceEventTime(100)
            up()
        }
        compose.waitForIdle()

        assertEquals(listOf(Triple("one", 0, 1)), moves)
    }
}
