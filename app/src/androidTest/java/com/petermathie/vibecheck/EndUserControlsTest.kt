package com.petermathie.vibecheck

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.petermathie.vibecheck.ui.HomeScreen
import com.petermathie.vibecheck.ui.MainUiState
import com.petermathie.vibecheck.ui.ReorderHandle
import com.petermathie.vibecheck.ui.WorkoutCompletionEvent
import com.petermathie.vibecheck.ui.rememberReorderState
import com.petermathie.vibecheck.domain.model.TrainingMode
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class EndUserControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun homeKeepsDateControlsTogetherWithoutRemovedHeadings() {
        var previewDay: Long? = null
        var selectedMode: TrainingMode? = null
        var state by mutableStateOf(MainUiState())
        compose.setContent {
            VibeCheckTheme {
                HomeScreen(
                    state,
                    {},
                    {},
                    {
                        previewDay = it
                        state = state.copy(homeRecencyDay = it)
                    },
                    { selectedMode = it },
                )
            }
        }

        compose.onNodeWithText("FRESHNESS").assertIsDisplayed()
        val legend = compose.onNodeWithContentDescription("Freshness colour scale", substring = true)
        legend.assertIsDisplayed()
        val frontMapBounds = compose.onNodeWithContentDescription("male front freshness map").fetchSemanticsNode().boundsInRoot
        val backMapBounds = compose.onNodeWithContentDescription("male back freshness map").fetchSemanticsNode().boundsInRoot
        val legendBounds = legend.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "front=$frontMapBounds legend=$legendBounds",
            kotlin.math.abs(frontMapBounds.top - legendBounds.top) < 2f,
        )
        assertTrue(
            "front=$frontMapBounds legend=$legendBounds",
            kotlin.math.abs(frontMapBounds.bottom - legendBounds.bottom) < 2f,
        )
        assertTrue(
            "back=$backMapBounds legend=$legendBounds",
            kotlin.math.abs(backMapBounds.top - legendBounds.top) < 2f,
        )
        assertTrue(
            "back=$backMapBounds legend=$legendBounds",
            kotlin.math.abs(backMapBounds.bottom - legendBounds.bottom) < 2f,
        )
        compose.onNodeWithText("Overview").assertDoesNotExist()
        compose.onNodeWithText("Recency, not recovery or fatigue").assertDoesNotExist()
        compose.onNodeWithText("Habits, workouts and stretching").assertDoesNotExist()
        compose.onNodeWithContentDescription("Freshness month navigation").assertExists()
        compose.onNodeWithText("Earlier").assertExists()
        compose.onNodeWithText("Later").assertExists()
        val earlierCenter = compose.onNodeWithText("Earlier").fetchSemanticsNode().boundsInRoot.center.y
        val laterCenter = compose.onNodeWithText("Later").fetchSemanticsNode().boundsInRoot.center.y
        assertTrue(kotlin.math.abs(earlierCenter - laterCenter) < 2f)
        compose.onNodeWithText("Strength").assertIsDisplayed()
        compose.onNodeWithText("Stretch").assertIsDisplayed().performClick()
        assertEquals(TrainingMode.STRETCHING, selectedMode)
        val today = LocalDate.now()
        val targetDay = (today.dayOfMonth - 1).coerceAtLeast(1)
        compose.onNodeWithText(
            "Freshness, ${today.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
        ).assertIsDisplayed()
        val slider = compose.onNodeWithContentDescription("Freshness date")
        val range = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(1f, range.range.start)
        assertEquals(today.dayOfMonth.toFloat(), range.range.endInclusive)
        assertEquals((today.dayOfMonth - 2).coerceAtLeast(0), range.steps)
        slider.performSemanticsAction(SemanticsActions.SetProgress) {
            it(targetDay.toFloat())
        }
        compose.waitUntil(15_000) { previewDay == today.withDayOfMonth(targetDay).toEpochDay() }
        assertEquals(today.withDayOfMonth(targetDay).toEpochDay(), previewDay)
        compose.onNodeWithText(
            "Freshness, ${today.withDayOfMonth(targetDay).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
        ).assertIsDisplayed()

        compose.onNodeWithText("Earlier").performClick()
        val previousMonth = YearMonth.from(today).minusMonths(1)
        compose.waitUntil(15_000) {
            previewDay?.let(LocalDate::ofEpochDay)?.let(YearMonth::from) == previousMonth
        }
        compose.onNodeWithText(previousMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))).assertIsDisplayed()
        val previousRange = compose.onNodeWithContentDescription("Freshness date")
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(previousMonth.lengthOfMonth().toFloat(), previousRange.range.endInclusive)
        compose.onNodeWithContentDescription("Freshness date").performSemanticsAction(SemanticsActions.SetProgress) {
            it(10.49f)
        }
        compose.waitUntil(15_000) { previewDay == previousMonth.atDay(10).toEpochDay() }
        compose.onNodeWithContentDescription("Freshness date").performSemanticsAction(SemanticsActions.SetProgress) {
            it(10.51f)
        }
        compose.waitUntil(15_000) { previewDay == previousMonth.atDay(11).toEpochDay() }
        assertNotNull(previewDay)
    }

    @Test
    fun dragHandleCommitsOneCompletedMove() {
        val moves = mutableListOf<Triple<Any, Int, Int>>()
        compose.setContent {
            VibeCheckTheme {
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

    @Test
    fun muscleDetailsSheetPreservesMapGeometryAndDismissesWithClose() {
        compose.setContent {
            VibeCheckTheme {
                HomeScreen(
                    state = MainUiState(),
                    onDayClick = {},
                    onContinue = {},
                    onRecencyDayChange = {},
                    onModeChange = {},
                )
            }
        }
        val map = compose.onNodeWithContentDescription("male front freshness map")
        val before = map.fetchSemanticsNode().boundsInRoot
        val inspectChest = map.fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label.startsWith("Inspect CHEST") }

        compose.runOnIdle { inspectChest.action() }
        compose.onNodeWithText("Chest Freshness details").assertIsDisplayed()
        val during = map.fetchSemanticsNode().boundsInRoot
        assertEquals(before.left, during.left, 0.5f)
        assertEquals(before.top, during.top, 0.5f)
        assertEquals(before.right, during.right, 0.5f)
        assertEquals(before.bottom, during.bottom, 0.5f)

        compose.onNodeWithContentDescription("Close muscle details").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Chest Freshness details").assertDoesNotExist()
        val after = map.fetchSemanticsNode().boundsInRoot
        assertEquals(before.left, after.left, 0.5f)
        assertEquals(before.top, after.top, 0.5f)
        assertEquals(before.right, after.right, 0.5f)
        assertEquals(before.bottom, after.bottom, 0.5f)
    }

    @Test
    fun reducedMotionCompletionAcknowledgesOnceWithoutAnimatedDelay() {
        val preferences = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("settings", 0)
        preferences.edit().putBoolean("reducedMotion", true).commit()
        var consumedId: String? = null
        compose.mainClock.autoAdvance = false
        compose.setContent {
            VibeCheckTheme {
                HomeScreen(
                    state = MainUiState(),
                    onDayClick = {},
                    onContinue = {},
                    onRecencyDayChange = {},
                    onModeChange = {},
                    completionEvent = WorkoutCompletionEvent("event-1", TrainingMode.STRENGTH, listOf("CHEST")),
                    onCompletionConsumed = { consumedId = it },
                )
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithContentDescription("Strength session complete. Freshness updated.").assertExists()
        compose.runOnIdle { assertEquals("event-1", consumedId) }
        compose.mainClock.advanceTimeBy(300)
        preferences.edit().remove("reducedMotion").commit()
        compose.mainClock.autoAdvance = true
    }
}
