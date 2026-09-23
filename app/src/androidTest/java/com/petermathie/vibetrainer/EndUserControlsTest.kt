package com.petermathie.vibetrainer

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.petermathie.vibetrainer.ui.HomeScreen
import com.petermathie.vibetrainer.ui.MainUiState
import com.petermathie.vibetrainer.ui.ReorderHandle
import com.petermathie.vibetrainer.ui.rememberReorderState
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class EndUserControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun homeKeepsDateControlsTogetherWithoutRemovedHeadings() {
        var previewDay: Long? = null
        var selectedMode: TrainingMode? = null
        compose.setContent {
            VibeTrainerTheme {
                HomeScreen(MainUiState(), {}, {}, { previewDay = it }, { selectedMode = it })
            }
        }

        compose.onNodeWithText("Overview").assertDoesNotExist()
        compose.onNodeWithText("Recency, not recovery or fatigue").assertDoesNotExist()
        compose.onNodeWithText("Habits, workouts and stretching").assertDoesNotExist()
        compose.onNodeWithContentDescription("Activity date navigation").assertExists()
        compose.onNodeWithText("Earlier").assertExists()
        compose.onNodeWithText("Later").assertExists()
        val earlierCenter = compose.onNodeWithText("Earlier").fetchSemanticsNode().boundsInRoot.center.y
        val laterCenter = compose.onNodeWithText("Later").fetchSemanticsNode().boundsInRoot.center.y
        assertTrue(kotlin.math.abs(earlierCenter - laterCenter) < 2f)
        compose.onNodeWithText("Strength").assertIsDisplayed()
        compose.onNodeWithText("Stretch").assertIsDisplayed().performClick()
        assertEquals(TrainingMode.STRETCHING, selectedMode)
        compose.onNodeWithContentDescription("Home recency date").performSemanticsAction(SemanticsActions.SetProgress) {
            it((LocalDate.now().toEpochDay() - 10).toFloat())
        }
        compose.waitUntil(15_000) { previewDay != null }
        assertNotNull(previewDay)
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
