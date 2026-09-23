package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.MeasurementsScreen
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MeasurementsUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun savesOnlyBodyweightAndDisplaysTwoDecimals() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        compose.setContent {
            VibeTrainerTheme {
                MeasurementsScreen(EditorViewModel(database))
            }
        }

        compose.onNodeWithContentDescription("Metric").assertDoesNotExist()
        compose.onNodeWithContentDescription("Bodyweight").performTextInput("78.126")
        compose.onNodeWithText("Save bodyweight").performClick()
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().measurements().first().isNotEmpty() }
        }

        val saved = runBlocking { database.editorDao().measurements().first().single() }
        assertEquals("Bodyweight", saved.metric)
        assertEquals(78.126, saved.value, 0.0)
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Bodyweight: 78.13 kg"))
        compose.onNodeWithText("Bodyweight: 78.13 kg").assertIsDisplayed()
    }
}
