package com.petermathie.vibetrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.petermathie.vibetrainer.data.local.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private fun write(block: suspend () -> Unit) { viewModelScope.launch { try { block() } catch (e: Exception) { error.value = e.message ?: "Could not save" } } }
    fun save(row: ProgrammeEntity) = write { dao.programme(row) }
    fun save(row: ProgrammeDayEntity) = write { dao.day(row) }
    fun save(row: ProgrammeExerciseEntity) = write { dao.entry(row) }
    fun save(row: WorkoutEntity) = write { dao.workout(row) }
    fun save(row: WorkoutExerciseEntity) = write { dao.workoutExercise(row) }
    fun save(row: TrackerEntity) = write { dao.tracker(row) }
    fun save(row: TrackerFieldEntity) = write { dao.field(row) }
    fun save(row: TrackerDailyValueEntity) = write { db.trackerDao().upsertValue(row) }
    fun save(row: BodyMeasurementEntity) = write { dao.measurement(row) }
    fun save(row: ExerciseVariationEntity) = write { dao.variation(row) }
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
