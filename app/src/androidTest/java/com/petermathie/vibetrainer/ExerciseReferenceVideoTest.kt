package com.petermathie.vibetrainer

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.data.seed.DatabaseSeeder
import com.petermathie.vibetrainer.ui.EditorViewModel
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseReferenceVideoTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: VibeDatabase
    private lateinit var source: File

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(context, VibeDatabase::class.java).build()
        DatabaseSeeder(context, database).seedIfNeeded()
        source = File(context.cacheDir, "reference-video-source.mp4").also { it.writeBytes(byteArrayOf(1, 2, 3, 4)) }
    }

    @After
    fun tearDown() {
        database.close()
        source.delete()
    }

    @Test
    fun attachingAndDeletingReferenceVideoOwnsTheCopiedFile() = runBlocking {
        val viewModel = EditorViewModel(database)

        viewModel.attachReferenceVideo(context, "core:handstand", Uri.fromFile(source), "Best handstand")
        val attached = viewModel.referenceVideos.first { it.size == 1 }.single()
        val copied = File(File(context.filesDir, "exercise-reference-videos"), attached.fileName)

        assertEquals("Best handstand", attached.displayName)
        assertTrue(copied.isFile)
        assertEquals(source.readBytes().toList(), copied.readBytes().toList())

        viewModel.deleteReferenceVideo(context, attached)
        viewModel.referenceVideos.first { it.isEmpty() }

        assertFalse(copied.exists())
    }
}
