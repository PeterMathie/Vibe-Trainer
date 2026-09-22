package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.TrainingRepository
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.HistoryDayScreen
import com.petermathie.vibetrainer.ui.MainViewModel
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()

    @Test
    fun navigatesHistoricalDaysAndSwitchesMapModesWithSelectedSemantics() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        runBlocking { DatabaseSeeder(context, database).seedIfNeeded() }
        val repository = TrainingRepository(
            database,
            database.programmeDao(),
            database.workoutDao(),
            database.trackerDao(),
        )
        val viewModel = MainViewModel(repository, database.catalogueDao())
        val firstDay = LocalDate.now().minusDays(1)
        viewModel.selectHistoryDay(firstDay.toEpochDay())

        compose.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            VibeTrainerTheme {
                state.selectedHistoryDay?.let {
                    HistoryDayScreen(
                        state = state,
                        onBack = { viewModel.selectHistoryDay(null) },
                        onDayChange = viewModel::selectHistoryDay,
                        onModeChange = viewModel::setMode,
                    )
                }
            }
        }

        compose.onNodeWithText(firstDay.format(DATE_FORMAT), useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Strength").assertIsSelected()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Monday — Planche + Push").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithText("Previous day").performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(
                firstDay.minusDays(1).format(DATE_FORMAT),
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Stretching").performClick()
        compose.onNodeWithText("Stretching").assertIsSelected()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Front Splits").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Front Splits").assertDoesNotExist()
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    }
}
