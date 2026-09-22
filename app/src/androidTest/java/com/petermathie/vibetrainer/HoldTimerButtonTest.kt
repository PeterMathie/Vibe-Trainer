package com.petermathie.vibetrainer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.ui.HoldTimerButton
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import java.util.concurrent.atomic.AtomicLong
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HoldTimerButtonTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun startDisplaysElapsedTimeAndStopReturnsSeconds() {
        val clock = AtomicLong(1_000)
        var result: String? = null
        compose.setContent {
            VibeTrainerTheme {
                HoldTimerButton(
                    onStopped = { result = it },
                    nowMillis = clock::get,
                    tickMillis = 1,
                )
            }
        }

        compose.onNodeWithText("Start hold timer").performClick()
        clock.set(3_500)
        compose.waitUntil(5_000) {
            compose.onAllNodes(hasText("Stop · 2.5s")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Stop · 2.5s").assertExists().performClick()

        compose.runOnIdle { assertEquals("2.5", result) }
        compose.onNodeWithText("Start hold timer").assertExists()
    }
}
