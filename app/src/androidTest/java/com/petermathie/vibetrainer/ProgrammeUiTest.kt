package com.petermathie.vibetrainer

import android.content.Context
import androidx.room.Room
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.ui.EditorViewModel
import com.petermathie.vibetrainer.ui.ProgrammeEditor
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import com.petermathie.vibetrainer.domain.model.TrainingMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgrammeUiTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var db:VibeDatabase
    @After fun close(){db.close()}
    @Test fun createRenameAndOpenProgramme() {
        db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),VibeDatabase::class.java).build()
        val vm=EditorViewModel(db)
        compose.setContent { VibeTrainerTheme { ProgrammeEditor(vm,TrainingMode.STRENGTH,{}) } }
        compose.onNodeWithText("Create programme").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("My gym plan")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15000){compose.onAllNodesWithText("My gym plan").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("My gym plan").performClick()
        compose.onNodeWithText("Add day").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Push")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15000){compose.onAllNodesWithText("Push").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("Exercises").performClick()
        compose.onNodeWithText("Add exercise").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Push").assertExists()
    }

    @Test fun createRenameDuplicateReorderAndArchiveProgramme() {
        db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),VibeDatabase::class.java).build()
        val vm=EditorViewModel(db)
        compose.setContent { VibeTrainerTheme { ProgrammeEditor(vm,TrainingMode.STRENGTH,{}) } }

        createProgramme("Alpha")
        createProgramme("Beta")
        compose.waitUntil(15_000) { activeProgrammes().size == 2 }

        compose.onAllNodesWithText("Move up")[1].performClick()
        compose.waitUntil(15_000) { activeProgrammes().sortedBy { it.position }.map { it.name } == listOf("Beta","Alpha") }

        compose.onAllNodesWithText("Rename")[0].performClick()
        compose.onNode(hasSetTextAction()).performTextClearance()
        compose.onNode(hasSetTextAction()).performTextInput("Gamma")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "Gamma" } }

        compose.onAllNodesWithText("Duplicate")[0].performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == "Gamma (copy)" } }

        compose.onAllNodesWithText("Archive")[1].performClick()
        compose.waitUntil(15_000) { activeProgrammes().size == 2 }
        assertEquals(listOf("Gamma","Alpha"),activeProgrammes().sortedBy { it.position }.map { it.name })
        db.openHelper.readableDatabase.query("SELECT isArchived FROM programmes WHERE name='Gamma (copy)'").use {
            assertTrue(it.moveToFirst())
            assertEquals(1,it.getInt(0))
        }
    }

    private fun createProgramme(name:String) {
        compose.onNodeWithText("Create programme").performClick()
        compose.onNode(hasSetTextAction()).performTextInput(name)
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(15_000) { activeProgrammes().any { it.name == name } }
    }

    private fun activeProgrammes()=runBlocking { db.editorDao().programmes().first() }
}
