package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.local.ExerciseEntity
import com.petermathie.vibetrainer.data.local.ProgrammeEntity
import com.petermathie.vibetrainer.data.local.TrackerEntity
import com.petermathie.vibetrainer.ui.ArchiveScreen
import com.petermathie.vibetrainer.ui.Destination
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.MoreScreen
import com.petermathie.vibetrainer.ui.PrimaryNavigationBar
import com.petermathie.vibetrainer.ui.TrackerScreen
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NavigationUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() {
        if (::database.isInitialized) database.close()
    }

    @Test
    fun fixedNavigationDirectlyExposesHabitsAndBody() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme {
                var destination by remember { mutableStateOf(Destination.HOME) }
                Column(Modifier.width(320.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (destination) {
                            Destination.MORE -> MoreScreen { destination = it }
                            Destination.HABITS -> TrackerScreen(viewModel)
                            else -> Unit
                        }
                    }
                    PrimaryNavigationBar(destination) { destination = it }
                }
            }
        }

        listOf("Home", "Plans", "Progress", "Habits", "Body", "More").forEach {
            compose.onNodeWithText(it).assertIsDisplayed()
        }
        compose.onNodeWithText("Workout").assertDoesNotExist()
        compose.onNodeWithText("Habits").performClick()
        compose.onNodeWithText("New habit").assertIsDisplayed()
        compose.onNodeWithText("Body").assertIsDisplayed()
        compose.onNodeWithText("More").assertIsDisplayed()
    }

    @Test
    fun moreArchiveRestoresArchivedItems() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        runBlocking {
            database.editorDao().programme(ProgrammeEntity("archived-plan", "Old plan", "STRENGTH", false, true))
            database.editorDao().exercise(
                ExerciseEntity("archived-exercise", "Old exercise", "STRENGTH", "", null, null, "custom", true, true),
            )
            database.editorDao().tracker(TrackerEntity("archived-habit", "Old habit", false, true))
        }
        val viewModel = EditorViewModel(database)
        compose.setContent {
            VibeTrainerTheme {
                var destination by remember { mutableStateOf(Destination.MORE) }
                when (destination) {
                    Destination.MORE -> MoreScreen { destination = it }
                    Destination.ARCHIVE -> ArchiveScreen(viewModel)
                    else -> Unit
                }
            }
        }

        compose.onNodeWithText("Archive").performClick()
        listOf("Old plan", "Old exercise", "Old habit").forEach {
            compose.waitUntil(5_000) { compose.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty() }
        }
        compose.onAllNodesWithText("Restore")[0].performClick()
        compose.waitUntil(5_000) {
            runBlocking { database.editorDao().archivedProgrammes().first().none { it.id == "archived-plan" } }
        }
    }
}
