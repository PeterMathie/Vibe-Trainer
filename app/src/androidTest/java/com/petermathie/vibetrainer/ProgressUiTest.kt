package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.ProgressScreen
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun chartExposesAxesSkillExplanationAndExplicitRecords() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        runBlocking { DatabaseSeeder(context, database).seedIfNeeded() }
        val viewModel = EditorViewModel(database)
        compose.setContent { VibeTrainerTheme { ProgressScreen(viewModel) } }

        compose.onNodeWithText("Choose exercise").performClick()
        compose.onNode(hasText("Name, alias or muscle") and hasSetTextAction()).performTextInput("Planche")
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Planche").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasText("Planche") and !hasSetTextAction()).performClick()

        compose.onAllNodes(hasContentDescription("Progress chart", substring = true)).assertCountEquals(2)
        compose.onNodeWithText("Personal records").assertExists()
        compose.onNodeWithText("Repetition PR", substring = true).assertExists()
        compose.onNodeWithText("Skill index is a heuristic", substring = true).assertExists()
    }
}
