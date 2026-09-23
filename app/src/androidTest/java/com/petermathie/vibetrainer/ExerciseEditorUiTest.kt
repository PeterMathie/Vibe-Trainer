package com.petermathie.vibetrainer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.ExerciseEntity
import com.petermathie.vibetrainer.domain.programme.ExerciseInputConfig
import com.petermathie.vibetrainer.ui.ExerciseSettingsDialog
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseEditorUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun canonicalSettingsCanBeEditedForSeededExercise() {
        val preferences = androidx.test.core.app.ApplicationProvider
            .getApplicationContext<android.content.Context>()
            .getSharedPreferences("settings", 0)
        preferences.edit().putBoolean("lb", true).commit()
        val exercise = ExerciseEntity(
            "handstand", "Handstand", "STRENGTH", "SKILL_HOLD", null, null, "core", false,
        )
        var saved: ExerciseEntity? = null
        compose.setContent {
            VibeTrainerTheme {
                ExerciseSettingsDialog(
                    exercise = exercise,
                    onDismiss = {},
                    onSave = { saved = it },
                )
            }
        }

        compose.onNodeWithText("Exercise settings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Weight (lb)").assertExists()
        compose.onNodeWithContentDescription("Weight (kg)").assertDoesNotExist()
        compose.onNodeWithContentDescription("Time under tension (seconds)").performClick()
        compose.onNodeWithContentDescription("Sets").performTextInput("4")
        compose.onNodeWithContentDescription("Rest seconds").performTextClearance()
        compose.onNodeWithContentDescription("Rest seconds").performTextInput("90")
        compose.onNodeWithText("Save").performClick()

        val result = requireNotNull(saved)
        val config = ExerciseInputConfig.decode(result.inputConfig, result.trackingType)
        assertTrue(config.timeHeld)
        assertTrue(config.timeUnderTension)
        assertEquals(4, result.targetSets)
        assertEquals(90, result.restSeconds)
        preferences.edit().putBoolean("lb", false).commit()
    }
}
