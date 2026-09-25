package com.petermathie.vibecheck

import android.content.Context
import java.time.LocalDate
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.petermathie.vibecheck.data.local.VibeDatabase
import com.petermathie.vibecheck.ui.EditorViewModel
import com.petermathie.vibecheck.ui.MeasurementsScreen
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@android.annotation.SuppressLint("ViewModelConstructorInComposable")
class MeasurementsUiTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var database: VibeDatabase

    @After
    fun close() = database.close()


    private fun scrollUntilVisible(text: String) {
        repeat(8) {
            if (compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return
            compose.onRoot().performTouchInput { swipeUp() }
            compose.waitForIdle()
        }
        compose.onNodeWithText(text).assertExists()
    }

    @Test
    fun savesOnlyBodyweightAndDisplaysTwoDecimals() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        compose.setContent {
            VibeCheckTheme {
                MeasurementsScreen(EditorViewModel(database))
            }
        }

        compose.onAllNodesWithText(
            LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy")),
        ).onFirst().assertIsDisplayed()
        compose.onNodeWithContentDescription("Bodyweight").performTextInput("78.126")
        compose.onNodeWithText("Save bodyweight").performClick()
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().measurements().first().isNotEmpty() }
        }

        val saved = runBlocking { database.editorDao().measurements().first().single() }
        assertEquals("Bodyweight", saved.metric)
        assertEquals(78.126, saved.value, 0.0)
        compose.onNodeWithContentDescription("Bodyweight").performTextClearance()
        compose.onNodeWithContentDescription("Bodyweight").performTextInput("80")
        compose.onNodeWithText("Update bodyweight").performClick()
        compose.waitUntil(15_000) {
            runBlocking { database.editorDao().measurements().first().single().value == 80.0 }
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Bodyweight: 80.00 kg"))
        compose.onAllNodesWithText("Bodyweight: 80.00 kg").onFirst().assertIsDisplayed()
        compose.onNodeWithContentDescription("Progress chart", substring = true).assertExists()
        scrollUntilVisible("Calendar")
        scrollUntilVisible("Photos")
        compose.onNodeWithContentDescription("Reorder Calendar").assertExists()
        compose.onNodeWithContentDescription("Reorder Photos").assertExists()
        compose.onNodeWithText("80.00").assertExists()
        compose.onNodeWithContentDescription("80.0 to 80.0 kg", substring = true).assertExists()
    }

    @Test
    fun bodyCardOrderPersistsAcrossRecreation() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("body-layout", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        val generation = androidx.compose.runtime.mutableIntStateOf(0)
        compose.setContent {
            VibeCheckTheme {
                androidx.compose.runtime.key(generation.intValue) {
                    MeasurementsScreen(EditorViewModel(database))
                }
            }
        }
        scrollUntilVisible("Photos")
        val actions = compose.onNodeWithContentDescription("Reorder Photos").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertTrue(actions.any { it.label == "Move earlier" })
        assertTrue(actions.first { it.label == "Move earlier" }.action())
        compose.waitUntil(15_000) {
            context.getSharedPreferences("body-layout", android.content.Context.MODE_PRIVATE).getString("card-order", "")?.startsWith("photos|") == true
        }
        compose.runOnIdle { generation.intValue++ }
        compose.onNodeWithContentDescription("Reorder Photos").assertExists()
        compose.onNodeWithText("Photos").assertExists()
    }
}
