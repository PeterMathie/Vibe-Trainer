package com.petermathie.vibetrainer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibetrainer.data.local.TrackerDailyValueEntity
import com.petermathie.vibetrainer.data.local.TrackerEntity
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import com.petermathie.vibetrainer.data.local.VibeDatabase
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
class HabitFieldTest {
    private lateinit var database: VibeDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            VibeDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun inclusiveRangeTargetEvaluatesBothBounds() = runBlocking {
        val dao = database.trackerDao()
        dao.insertTrackers(listOf(TrackerEntity("tracker", "Tracker", false)))
        dao.insertFields(listOf(TrackerFieldEntity(
            "range",
            "tracker",
            "Range",
            "NUMBER",
            null,
            "RANGE",
            5.0,
            0,
            targetMaxValue = 10.0,
        )))

        dao.upsertValue(value(1, 5.0))
        dao.upsertValue(value(2, 10.0))
        dao.upsertValue(value(3, 10.1))

        val activity = dao.observeActivityValues().first()
        assertTrue(activity.first { it.epochDay == 1L }.targetMet)
        assertTrue(activity.first { it.epochDay == 2L }.targetMet)
        assertFalse(activity.first { it.epochDay == 3L }.targetMet)
    }

    @Test
    fun archivingAndReorderingPreserveHistoricalValues() = runBlocking {
        val editor = database.editorDao()
        val trackerDao = database.trackerDao()
        trackerDao.insertTrackers(listOf(TrackerEntity("tracker", "Tracker", false)))
        trackerDao.insertFields(listOf(
            TrackerFieldEntity("first", "tracker", "First", "CHOICE", null, null, null, 0, "A\nB"),
            TrackerFieldEntity("second", "tracker", "Second", "DATETIME", null, null, null, 1),
        ))
        trackerDao.upsertValue(TrackerDailyValueEntity("first", 1, null, null, "A", "", 1))

        val first = requireNotNull(editor.fieldById("first"))
        editor.field(first.copy(isArchived = true))
        editor.field(requireNotNull(editor.fieldById("second")).copy(position = 0))

        assertEquals("A", editor.values().first().single().textValue)
        assertTrue(requireNotNull(editor.fieldById("first")).isArchived)
        assertEquals(0, requireNotNull(editor.fieldById("second")).position)
        assertEquals("First", trackerDao.observeHistoryValues().first().single().fieldName)
    }

    @Test
    fun changingTargetDoesNotRewriteSavedDayOutcome() = runBlocking {
        val dao = database.trackerDao()
        dao.insertTrackers(listOf(TrackerEntity("tracker", "Tracker", false)))
        val field = TrackerFieldEntity("range", "tracker", "Range", "NUMBER", null, "AT_LEAST", 5.0, 0)
        dao.insertFields(listOf(field))
        dao.upsertValue(value(1, 6.0))

        dao.insertFields(listOf(field.copy(targetValue = 10.0)))

        assertTrue(dao.observeActivityValues().first().single().targetMet)
        dao.upsertValue(value(1, 6.0))
        assertFalse(dao.observeActivityValues().first().single().targetMet)
    }

    private fun value(day: Long, numeric: Double) =
        TrackerDailyValueEntity("range", day, numeric, null, null, "", day)
}
