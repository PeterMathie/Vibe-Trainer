package com.petermathie.vibetrainer

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
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
        runBlocking {
            val historicalState = withTimeout(15_000) {
                viewModel.uiState.first {
                    it.selectedHistoryDay == firstDay.toEpochDay() &&
                        it.historyDay?.activities?.isNotEmpty() == true &&
                        it.recency.isNotEmpty()
                }
            }
            assertTrue(historicalState.recency.any { it.band.name != "NEVER" })
        }

        compose.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            VibeTrainerTheme {
                state.selectedHistoryDay?.let {
                    HistoryDayScreen(
                        state = state,
                        onBack = { viewModel.selectHistoryDay(null) },
                        onDayChange = viewModel::selectHistoryDay,
                    )
                }

            }
        }

        compose.onNodeWithText(firstDay.format(DATE_FORMAT), useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Reconstructed from records up to the end of this day").assertDoesNotExist()
        compose.onNodeWithContentDescription("Historical day controls").assertExists()
        compose.onNodeWithContentDescription("Home recency date").assertDoesNotExist()
        compose.onNodeWithText("Strength").assertDoesNotExist()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Planche + Push").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithContentDescription("Previous day").performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(
                firstDay.minusDays(1).format(DATE_FORMAT),
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("Historical day controls").assertDoesNotExist()
    }

    @Test
    fun homeRecencyPreviewDoesNotOpenHistoricalDay() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        DatabaseSeeder(context, database).seedIfNeeded()
        val viewModel = MainViewModel(
            TrainingRepository(database, database.programmeDao(), database.workoutDao(), database.trackerDao()),
            database.catalogueDao(),
        )
        val previewDay = LocalDate.now().minusDays(10).toEpochDay()

        viewModel.selectHomeRecencyDay(previewDay)
        val state = withTimeout(15_000) {
            viewModel.uiState.first { it.homeRecencyDay == previewDay }
        }

        assertEquals(previewDay, state.homeRecencyDay)
        assertNull(state.selectedHistoryDay)
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    }
}
