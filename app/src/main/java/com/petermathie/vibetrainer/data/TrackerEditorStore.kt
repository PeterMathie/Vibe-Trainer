package com.petermathie.vibetrainer.data

import androidx.room.withTransaction
import com.petermathie.vibetrainer.data.local.TrackerDailyValueEntity
import com.petermathie.vibetrainer.data.local.TrackerEntity
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import com.petermathie.vibetrainer.data.local.VibeDatabase
import com.petermathie.vibetrainer.domain.editor.moveItem
import java.util.UUID
import kotlinx.coroutines.flow.first

class TrackerEditorStore(
    private val db: VibeDatabase,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val dao = db.editorDao()

    suspend fun saveTracker(row: TrackerEntity) = dao.tracker(row)

    suspend fun deleteTrackerIfEmpty(id: String) {
        db.withTransaction {
            if (dao.trackerValueCount(id) == 0) dao.deleteTracker(id)
        }
    }

    suspend fun createTracker(row: TrackerEntity) {
        db.withTransaction {
            val position = dao.trackers().first().maxOfOrNull { it.position }?.plus(1) ?: 0
            dao.tracker(row.copy(position = position))
            dao.field(
                TrackerFieldEntity(
                    idFactory(),
                    row.id,
                    "Done",
                    "BOOLEAN",
                    null,
                    null,
                    null,
                    0,
                ),
            )
        }
    }

    suspend fun saveField(row: TrackerFieldEntity) = dao.field(row)

    suspend fun moveTracker(id: String, delta: Int) {
        val siblings = dao.trackers().first().filterNot { it.isArchived }.sortedBy { it.position }
        moveItem(siblings, id, delta) { it.id }?.let { rows ->
            db.withTransaction {
                rows.forEachIndexed { index, row -> dao.tracker(row.copy(position = index)) }
            }
        }
    }

    suspend fun moveField(id: String, delta: Int) {
        val selected = dao.fieldById(id) ?: return
        val siblings = dao.fields().first()
            .filter { it.trackerId == selected.trackerId && !it.isArchived }
            .sortedBy { it.position }
        moveItem(siblings, id, delta) { it.id }?.let { rows ->
            db.withTransaction {
                rows.forEachIndexed { index, row -> dao.field(row.copy(position = index)) }
            }
        }
    }

    suspend fun saveValue(row: TrackerDailyValueEntity) = db.trackerDao().upsertValue(row)

    suspend fun clearValue(fieldId: String, day: Long) = db.trackerDao().clearValue(fieldId, day)
}
