package com.petermathie.vibetrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.petermathie.vibetrainer.data.local.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

@HiltViewModel
class EditorViewModel @Inject constructor(private val db: VibeDatabase) : ViewModel() {
    suspend fun exportJson() = com.petermathie.vibetrainer.data.DataTransfer.export(db)
    suspend fun importJson(text: String) = com.petermathie.vibetrainer.data.DataTransfer.import(db, text)
    suspend fun exportCsv() = com.petermathie.vibetrainer.data.DataTransfer.csv(db)
    private val dao = db.editorDao()
    private fun <T> Flow<List<T>>.live() = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val programmes = dao.programmes().live()
    val days = dao.days().live()
    val entries = dao.entries().live()
    val exercises = dao.exercises().live()
    val muscles = dao.muscles().live()
    val mappings = dao.mappings().live()
    val aliases = dao.aliases().live()
    val variations = dao.variations().live()
    val workouts = dao.workouts().live()
    val workoutExercises = dao.workoutExercises().live()
    val sets = dao.sets().live()
    val bands = dao.bands().live()
    val setBands = dao.setBands().live()
    val trackers = dao.trackers().live()
    val fields = dao.fields().live()
    val values = dao.values().live()
    val measurements = dao.measurements().live()
    val error = MutableStateFlow<String?>(null)
    private val writes = Mutex()
    private fun write(block: suspend () -> Unit) { viewModelScope.launch { writes.withLock { try { block() } catch (e: CancellationException) { throw e } catch (e: Exception) { error.value = e.message ?: "Could not save" } } } }
    fun save(row: ProgrammeEntity) = write { dao.programme(row) }
    fun moveProgramme(id: String, delta: Int) = write {
        val rows=dao.programmes().first().toMutableList()
        val from=rows.indexOfFirst { it.id==id }; val to=from+delta
        if(from>=0 && to in rows.indices) {
            java.util.Collections.swap(rows,from,to)
            db.withTransaction { rows.forEachIndexed { i,p -> dao.programme(p.copy(position=i)) } }
        }
    }
    fun moveDay(id: String, delta: Int) = write {
        val all=dao.days().first(); val selected=all.find { it.id==id } ?: return@write
        val rows=all.filter { it.programmeId==selected.programmeId }.sortedBy { it.position }.toMutableList()
        val from=rows.indexOfFirst { it.id==id }; val to=from+delta
        if(to in rows.indices) { java.util.Collections.swap(rows,from,to); db.withTransaction { rows.forEachIndexed { i,d -> dao.day(d.copy(position=i)) } } }
    }
    fun moveEntry(id: String, delta: Int) = write {
        val all=dao.entries().first(); val selected=all.find { it.id==id } ?: return@write
        val rows=all.filter { it.programmeDayId==selected.programmeDayId }.sortedBy { it.position }.toMutableList()
        val from=rows.indexOfFirst { it.id==id };val to=from+delta
        if(to in rows.indices) {java.util.Collections.swap(rows,from,to);db.withTransaction {rows.forEachIndexed { i,e -> dao.entry(e.copy(position=i)) }}}
    }
    fun save(row: ProgrammeDayEntity) = write { dao.day(row) }
    fun save(row: ProgrammeExerciseEntity) = write { dao.entry(row) }
    fun save(row: WorkoutEntity) = write { dao.workout(row) }
    fun changeWorkoutDate(row: WorkoutEntity, end: Long) = write {
        val delta=end-(row.finishedAt ?: row.startedAt)
        val ids=dao.workoutExercises().first().filter { it.workoutId==row.id }.map { it.id }.toSet()
        val sets=dao.sets().first().filter { it.workoutExerciseId in ids }
        db.withTransaction {
            dao.workout(row.copy(startedAt=row.startedAt+delta,finishedAt=end))
            sets.forEach { dao.set(it.copy(loggedAt=it.loggedAt+delta,updatedAt=System.currentTimeMillis())) }
        }
    }
    fun save(row: WorkoutExerciseEntity) = write {
        db.withTransaction {
            val definition=dao.exerciseById(row.actualExerciseId)
            val changed=row.exerciseName.isBlank()
            dao.workoutExercise(if(changed)row.copy(exerciseName=definition?.canonicalName.orEmpty(),trackingType=definition?.trackingType.orEmpty()) else row)
            if(changed) {
                dao.clearWorkoutMuscles(row.id)
                dao.workoutMuscles(dao.muscleMappings(row.actualExerciseId).map { WorkoutMuscleEntity(row.id,it.muscleId,it.role) })
            }
        }
    }
    fun save(row: TrackerEntity) = write { dao.tracker(row) }
    fun createTracker(row: TrackerEntity) = write {
        db.withTransaction {
            dao.tracker(row)
            dao.field(TrackerFieldEntity(newId(),row.id,"Done","BOOLEAN",null,null,null,0))
        }
    }
    fun save(row: TrackerFieldEntity) = write { dao.field(row) }
    fun save(row: TrackerDailyValueEntity) = write { db.trackerDao().upsertValue(row) }
    fun save(row: BodyMeasurementEntity) = write { dao.measurement(row) }
    fun save(row: ExerciseVariationEntity) = write { dao.variation(row) }
    fun moveVariation(id: String, delta: Int) = write {
        val all=dao.variations().first()
        val selected=all.find { it.id==id && !it.isSeeded } ?: return@write
        val rows=all.filter { it.exerciseId==selected.exerciseId && !it.isSeeded }.sortedBy { it.progressionRank }.toMutableList()
        val from=rows.indexOfFirst { it.id==id }; val to=from+delta
        val start=(all.filter { it.exerciseId==selected.exerciseId && it.isSeeded }.maxOfOrNull { it.progressionRank } ?: -1)+1
        if(to in rows.indices) { java.util.Collections.swap(rows,from,to);db.withTransaction { rows.forEachIndexed { i,v -> dao.variation(v.copy(progressionRank=start+i)) } } }
    }
    fun removeDay(id: String) = write { dao.deleteDay(id) }
    fun removeEntry(id: String) = write { dao.deleteEntry(id) }
    fun removeSet(id: String) = write { dao.deleteSet(id) }
    fun removeWorkout(id: String) = write { dao.deleteWorkout(id) }
    fun clearValue(id: String, day: Long) = write { dao.clearValue(id, day) }
    fun removeMeasurement(id: String) = write { dao.deleteMeasurement(id) }
    fun saveSet(row: WorkoutSetEntity, bandIds: List<String>) = write {
        db.withTransaction {
            dao.set(row)
            dao.clearBands(row.id)
            db.workoutDao().insertSetBands(bandIds.mapIndexed { index, id -> WorkoutSetBandEntity(row.id, id, index) })
        }
    }
    fun duplicate(programme: ProgrammeEntity) = write {
        val sourceDays = dao.days().first().filter { it.programmeId == programme.id }
        val sourceEntries = dao.entries().first()
        db.withTransaction {
            val id = newId()
            dao.programme(programme.copy(id = id, name = programme.name + " (copy)", isDemo = false))
            sourceDays.forEach { day ->
                val dayId = newId()
                dao.day(day.copy(id = dayId, programmeId = id))
                sourceEntries.filter { it.programmeDayId == day.id }.forEach {
                    dao.entry(it.copy(id = newId(), programmeDayId = dayId))
                }
            }
        }
    }
    fun saveExercise(row: ExerciseEntity, names: List<String>, muscleRoles: Map<String, String>) = write {
        require(row.isCustom) { "Duplicate seeded exercises before editing" }
        db.withTransaction {
            dao.exercise(row)
            dao.clearAliases(row.id)
            dao.clearMappings(row.id)
            db.catalogueDao().insertAliases(names.filter { it.isNotBlank() }.distinct().map { ExerciseAliasEntity(row.id, it.trim(), it.trim().lowercase()) })
            db.catalogueDao().insertExerciseMuscles(muscleRoles.map { ExerciseMuscleEntity(row.id, it.key, it.value) })
        }
    }
}
