package com.petermathie.vibecheck.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petermathie.vibecheck.data.TrainingRepository
import com.petermathie.vibecheck.data.local.CatalogueDao
import com.petermathie.vibecheck.domain.model.ActiveWorkout
import com.petermathie.vibecheck.domain.model.ActivityDay
import com.petermathie.vibecheck.domain.model.ExerciseSummary
import com.petermathie.vibecheck.domain.model.ExerciseTag
import com.petermathie.vibecheck.domain.model.HistoryDayDetail
import com.petermathie.vibecheck.domain.model.MuscleRecency
import com.petermathie.vibecheck.domain.model.ProgrammeDaySummary
import com.petermathie.vibecheck.domain.model.SetDraft
import com.petermathie.vibecheck.domain.model.TrackingType
import com.petermathie.vibecheck.domain.model.TrainingMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

data class MainUiState(
    val mode: TrainingMode = TrainingMode.STRENGTH,
    val programmeDays: List<ProgrammeDaySummary> = emptyList(),
    val activeWorkout: ActiveWorkout? = null,
    val recency: List<MuscleRecency> = emptyList(),
    val activityDays: List<ActivityDay> = emptyList(),
    val homeRecencyDay: Long? = null,
    val selectedHistoryDay: Long? = null,
    val historyDay: HistoryDayDetail? = null,
)

private data class CoreUiState(
    val mode: TrainingMode,
    val programmeDays: List<ProgrammeDaySummary>,
    val activeWorkout: ActiveWorkout?,
    val recency: List<MuscleRecency>,
    val activityDays: List<ActivityDay>,
)

private data class RecencyQuery(
    val mode: TrainingMode,
    val homeDay: Long?,
    val historyDay: Long?,
    val now: Long,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TrainingRepository,
    private val catalogueDao: CatalogueDao,
) : ViewModel() {
    private val mode = MutableStateFlow(TrainingMode.STRENGTH)
    private val homeRecencyDay = MutableStateFlow<Long?>(null)
    private val selectedHistoryDay = MutableStateFlow<Long?>(null)
    private val searchQuery = MutableStateFlow("")

    private val clock = MutableStateFlow(System.currentTimeMillis())
    init { viewModelScope.launch { while (true) { delay(30_000); clock.value=System.currentTimeMillis() } } }
    private val recency = combine(mode, homeRecencyDay, selectedHistoryDay, clock) { currentMode, homeDay, historyDay, now ->
        RecencyQuery(currentMode, homeDay, historyDay, now)
    }.flatMapLatest { query ->
            val day = query.historyDay ?: query.homeDay
            val atMillis = day?.let { epochDay ->
                LocalDate.ofEpochDay(epochDay).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            } ?: query.now
            repository.observeMuscleRecency(query.mode, atMillis)
        }

    private val history = selectedHistoryDay.flatMapLatest { day ->
        if (day == null) kotlinx.coroutines.flow.flowOf(null)
        else repository.observeHistoryDay(day).map { it as HistoryDayDetail? }
    }

    private val coreState = combine(
        mode,
        repository.observeProgrammeDays(),
        repository.observeDraft(),
        recency,
        repository.observeActivityHeatmap(),
    ) { currentMode, days, workout, currentRecency, activity ->
        CoreUiState(currentMode, days, workout, currentRecency, activity)
    }

    val uiState: StateFlow<MainUiState> = combine(coreState, homeRecencyDay, selectedHistoryDay, history) { core, homeDay, selectedDay, dayHistory ->
        MainUiState(
            mode = core.mode,
            programmeDays = core.programmeDays,
            activeWorkout = core.activeWorkout,
            recency = core.recency,
            activityDays = core.activityDays,
            homeRecencyDay = homeDay,
            selectedHistoryDay = selectedDay,
            historyDay = dayHistory,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    val exerciseResults: StateFlow<List<ExerciseSummary>> = searchQuery.flatMapLatest { query ->
        catalogueDao.searchExercises(query).map { rows ->
            rows.map { row ->
                ExerciseSummary(
                    id = row.id,
                    name = row.canonicalName,
                    tag = ExerciseTag.valueOf(row.tag),
                    trackingType = TrackingType.valueOf(row.trackingType),
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setMode(value: TrainingMode) { mode.value = value }
    fun setSearchQuery(value: String) { searchQuery.value = value }
    fun selectHomeRecencyDay(epochDay: Long) {
        homeRecencyDay.value = epochDay.takeUnless { it == LocalDate.now().toEpochDay() }
    }
    fun selectHistoryDay(epochDay: Long?) { selectedHistoryDay.value = epochDay }

    fun startWorkout(dayId: String, replaceExisting: Boolean = false, onStarted: () -> Unit = {}) = viewModelScope.launch {
        repository.startWorkout(dayId, replaceExisting)
        onStarted()
    }

    fun addSet(workoutExerciseId: String, draft: SetDraft) = viewModelScope.launch {
        repository.addSet(workoutExerciseId, draft)
    }

    fun updateExerciseNotes(workoutExerciseId: String, notes: String) = viewModelScope.launch {
        repository.updateExerciseNotes(workoutExerciseId, notes)
    }

    fun finishWorkout(workoutId: String, onFinished: () -> Unit = {}) = viewModelScope.launch {
        repository.finishWorkout(workoutId)
        clock.value=System.currentTimeMillis()
        onFinished()
    }

    fun removeDemoData(onResult: (Result<com.petermathie.vibecheck.data.DemoRemovalSummary>) -> Unit) =
        viewModelScope.launch {
            onResult(runCatching { repository.removeDemoData() })
        }
}
