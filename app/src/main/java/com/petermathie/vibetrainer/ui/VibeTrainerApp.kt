package com.petermathie.vibetrainer.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.domain.model.ActiveWorkout
import com.petermathie.vibetrainer.domain.model.ActivityDay
import com.petermathie.vibetrainer.domain.model.AnatomySex
import com.petermathie.vibetrainer.domain.model.ExerciseSummary
import com.petermathie.vibetrainer.domain.model.MuscleRecency
import com.petermathie.vibetrainer.domain.model.SetDraft
import com.petermathie.vibetrainer.domain.model.TrackingType
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.domain.model.WorkoutExerciseLog
import com.petermathie.vibetrainer.domain.model.WorkoutSetLog
import com.petermathie.vibetrainer.ui.anatomy.AnatomyView
import com.petermathie.vibetrainer.ui.anatomy.MuscleMap
import com.petermathie.vibetrainer.ui.theme.LocalVibePalette
import com.petermathie.vibetrainer.ui.theme.VibePalette
import com.petermathie.vibetrainer.ui.theme.VibePalettes
import com.petermathie.vibetrainer.ui.theme.VibeShapes
import com.petermathie.vibetrainer.ui.theme.VibeSpacing
import com.petermathie.vibetrainer.ui.theme.VibeTrainerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

private enum class Destination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    PROGRAMMES("Programmes", Icons.Outlined.FitnessCenter),
    WORKOUT("Workout", Icons.Outlined.PlayArrow),
    EXERCISES("Exercises", Icons.Outlined.LibraryBooks),
    PROGRESS("Progress", Icons.Outlined.BarChart),
    HABITS("Habits", Icons.Outlined.Check),
    HISTORY("History", Icons.Outlined.LibraryBooks),
    MEASUREMENTS("Body", Icons.Outlined.FitnessCenter),
    SETTINGS("Settings", Icons.Outlined.Palette),
    STYLE("Style", Icons.Outlined.Palette),
}

@Composable
fun VibeTrainerApp(viewModel: MainViewModel = hiltViewModel()) {
    val editor: EditorViewModel = hiltViewModel()
    val error by editor.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings",0)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exercises by viewModel.exerciseResults.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    var paletteId by rememberSaveable { mutableStateOf(prefs.getString("palette",VibePalettes.MidnightLime.id)!!) }
    val palette = VibePalettes.builtIns[paletteId] ?: VibePalettes.MidnightLime

    VibeTrainerTheme(palette) {
        if (state.selectedHistoryDay != null) {
            BackHandler { viewModel.selectHistoryDay(null) }
            HistoryDayScreen(
                state = state,
                onBack = { viewModel.selectHistoryDay(null) },
            )
            return@VibeTrainerTheme
        }

        Scaffold(
            containerColor = palette.background,
            bottomBar = {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().horizontalScroll(rememberScrollState()).background(palette.surface)) {
                    Destination.entries.forEach { item ->
                        TextButton(onClick = { destination = item }) { Text(if(destination == item) "• ${item.label}" else item.label) }
                    }
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                AppHeader(state.mode, viewModel::setMode)
                if(error != null) TextButton(onClick = { editor.error.value = null }) { Text(error.orEmpty(),color=MaterialTheme.colorScheme.error) }
                when (destination) {
                    Destination.HOME -> HomeScreen(state, viewModel::selectHistoryDay) { destination = Destination.WORKOUT }
                    Destination.PROGRAMMES -> ProgrammeEditor(editor, state.mode) { dayId ->
                        viewModel.startWorkout(dayId) { destination = Destination.WORKOUT }
                    }
                    Destination.WORKOUT -> WorkoutEditor(
                        vm = editor,
                        workoutId = state.activeWorkout?.id,
                        onFinish = { id -> viewModel.finishWorkout(id) { destination = Destination.HOME } },
                        onChoose = { destination = Destination.PROGRAMMES },
                    )
                    Destination.EXERCISES -> ExerciseEditor(editor)
                    Destination.PROGRESS -> ProgressScreen(editor)
                    Destination.HABITS -> TrackerScreen(editor)
                    Destination.HISTORY -> HistoryScreen(editor,viewModel::selectHistoryDay)
                    Destination.MEASUREMENTS -> MeasurementsScreen(editor)
                    Destination.SETTINGS -> SettingsScreen(editor,{destination=Destination.STYLE},viewModel::removeDemoData)
                    Destination.STYLE -> StyleScreen(paletteId, { paletteId = it; prefs.edit().putString("palette",it).apply() }, viewModel::removeDemoData)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppHeader(mode: TrainingMode, onModeChange: (TrainingMode) -> Unit) {
    val palette = LocalVibePalette.current
    Row(
        Modifier.fillMaxWidth().background(palette.background).padding(horizontal = VibeSpacing.medium, vertical = VibeSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VibeSpacing.medium),
    ) {
        Column(Modifier.weight(1f)) {
            Text("VIBE TRAINER", color = palette.accent, style = MaterialTheme.typography.labelLarge)
            Text("Training log", style = MaterialTheme.typography.titleLarge)
        }
        SingleChoiceSegmentedButtonRow {
            TrainingMode.entries.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = mode == item,
                    onClick = { onModeChange(item) },
                    shape = SegmentedButtonDefaults.itemShape(index, TrainingMode.entries.size),
                    label = { Text(if (item == TrainingMode.STRENGTH) "Strength" else "Stretch") },
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(state: MainUiState, onDayClick: (Long) -> Unit, onContinue: () -> Unit) {
    val sex = if(LocalContext.current.getSharedPreferences("settings",0).getBoolean("female",false)) AnatomySex.FEMALE else AnatomySex.MALE
    var selectedMuscle by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = state.recency.firstOrNull { it.muscleId == selectedMuscle }
    ScreenList {
        item {
            Text("Overview", style = MaterialTheme.typography.headlineLarge)
            Text("Recency, not recovery or fatigue", color = LocalVibePalette.current.textSecondary)
        }
        state.activeWorkout?.let { workout ->
            item {
                VibeCard {
                    Text("ACTIVE WORKOUT", color = LocalVibePalette.current.accent, style = MaterialTheme.typography.labelLarge)
                    Text(workout.name, style = MaterialTheme.typography.titleLarge)
                    Text("${workout.exercises.sumOf { it.sets.size }} sets autosaved", color = LocalVibePalette.current.textSecondary)
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Continue workout") }
                }
            }
        }
        item {
            VibeCard {
                Text(if (state.mode == TrainingMode.STRENGTH) "MUSCLE RECENCY" else "STRETCH RECENCY", color = LocalVibePalette.current.accent, style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MuscleMap(
                        sex = sex,
                        view = AnatomyView.FRONT,
                        states = state.recency.associate { it.muscleId to it.band },
                        onMuscleTap = { selectedMuscle = it },
                        modifier = Modifier.weight(1f),
                        selectedMuscleId = selectedMuscle,
                    )
                    MuscleMap(
                        sex = sex,
                        view = AnatomyView.BACK,
                        states = state.recency.associate { it.muscleId to it.band },
                        onMuscleTap = { selectedMuscle = it },
                        modifier = Modifier.weight(1f),
                        selectedMuscleId = selectedMuscle,
                    )
                }
                selectedMuscle?.let { muscle ->
                    Text(muscle.replace('_', ' '), fontWeight = FontWeight.Bold)
                    Text(
                        selected?.let { "${it.band.name.replace('_', ' ').lowercase()} · ${"%.1f".format(it.setEquivalents)} set-equivalents in 7 days" }
                            ?: "Never recorded",
                        color = LocalVibePalette.current.textSecondary,
                    )
                    selected?.contributingExerciseNames?.takeIf { it.isNotEmpty() }?.let { Text(it.joinToString(), color = LocalVibePalette.current.textFaint) }
                }
            }
        }
        item {
            VibeCard {
                Text("LAST 5 WEEKS", color = LocalVibePalette.current.accent, style = MaterialTheme.typography.labelLarge)
                Text("Habits, workouts and stretching", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                ActivityHeatmap(state.activityDays, onDayClick)
                Text("0 neutral · 1 light · 2 medium · 3+ dark", color = LocalVibePalette.current.textFaint, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ActivityHeatmap(days: List<ActivityDay>, onDayClick: (Long) -> Unit) {
    val palette = LocalVibePalette.current
    val counts = days.associate { it.epochDay to it.activityCount }
    val today = LocalDate.now().toEpochDay()
    val start = today - 34
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(5) { week ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(7) { day ->
                    val epochDay = start + week * 7 + day
                    val count = counts[epochDay] ?: 0
                    val color = when {
                        count >= 3 -> palette.heatmapThreePlus
                        count == 2 -> palette.heatmapTwo
                        count == 1 -> palette.heatmapOne
                        else -> palette.heatmapNeutral
                    }
                    Box(
                        Modifier.fillMaxWidth().height(22.dp)
                            .semantics { contentDescription = "${LocalDate.ofEpochDay(epochDay)}: $count activities" }
                            .background(color, RoundedCornerShape(5.dp))
                            .clickable { onDayClick(epochDay) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgrammeScreen(state: MainUiState, onStart: (String) -> Unit) {
    ScreenList {
        item {
            Text("Programmes", style = MaterialTheme.typography.headlineLarge)
            Text("Start whichever day you want—nothing is scheduled.", color = LocalVibePalette.current.textSecondary)
        }
        items(state.programmeDays.filter { it.mode == state.mode }, key = { it.id }) { day ->
            VibeCard {
                Text(day.name, style = MaterialTheme.typography.titleLarge)
                Text("${day.exerciseCount} exercises", color = LocalVibePalette.current.textSecondary)
                Button(onClick = { onStart(day.id) }, enabled = state.activeWorkout == null, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.activeWorkout == null) "Start this day" else "Finish the active workout first")
                }
            }
        }
        if (state.programmeDays.none { it.mode == state.mode }) {
            item { VibeCard { Text("No programmes yet. Production installs begin empty; test builds can include removable demo data.") } }
        }
    }
}

@Composable
private fun WorkoutScreen(
    workout: ActiveWorkout?,
    onAddSet: (String, SetDraft) -> Unit,
    onExerciseNotesChange: (String, String) -> Unit,
    onFinish: (String) -> Unit,
    onChooseProgramme: () -> Unit,
) {
    if (workout == null) {
        ScreenList {
            item {
                Text("Workout", style = MaterialTheme.typography.headlineLarge)
                VibeCard {
                    Text("No workout in progress")
                    Button(onClick = onChooseProgramme, modifier = Modifier.fillMaxWidth()) { Text("Choose a programme day") }
                }
            }
        }
        return
    }
    ScreenList {
        item {
            Text(workout.name, style = MaterialTheme.typography.headlineMedium)
            Text("Every saved set is stored immediately. Recency updates only when you finish.", color = LocalVibePalette.current.textSecondary)
        }
        items(workout.exercises, key = { it.id }) { exercise ->
            ExerciseLogger(exercise, onAddSet, onExerciseNotesChange)
        }
        item {
            Button(onClick = { onFinish(workout.id) }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Icon(Icons.Outlined.Check, null)
                Text(" Finish workout")
            }
        }
    }
}

@Composable
private fun ExerciseLogger(
    exercise: WorkoutExerciseLog,
    onAddSet: (String, SetDraft) -> Unit,
    onExerciseNotesChange: (String, String) -> Unit,
) {
    var performance by rememberSaveable(exercise.id) { mutableStateOf("") }
    var rpe by rememberSaveable(exercise.id) { mutableStateOf("") }
    var exerciseNotes by rememberSaveable(exercise.id) { mutableStateOf(exercise.notes) }
    val usesHold = exercise.trackingType in setOf(TrackingType.HOLD, TrackingType.SKILL_HOLD, TrackingType.ROM_MEASUREMENT)
    val draft = parseCompactSet(exercise.trackingType, performance, rpe)

    VibeCard {
        Text(exercise.exerciseName, style = MaterialTheme.typography.titleLarge)
        if (exercise.sets.isNotEmpty()) {
            exercise.sets.forEach { set ->
                CompactSavedSet(set)
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = performance,
                onValueChange = { performance = it },
                label = { Text(compactSetLabel(exercise.trackingType)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = rpe,
                onValueChange = { value -> if (value.isEmpty() || value.matches(Regex("\\d*(\\.\\d*)?"))) rpe = value },
                label = { Text("RPE") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.width(76.dp),
            )
            if (usesHold) {
                HoldTimer(seconds = performance, onSecondsChange = { performance = it })
            }
            IconButton(
                onClick = {
                    draft?.let { onAddSet(exercise.id, it) }
                    performance = ""
                    rpe = ""
                },
                enabled = draft != null,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(Icons.Outlined.Add, "Save set")
            }
        }
        OutlinedTextField(
            value = exerciseNotes,
            onValueChange = {
                exerciseNotes = it
                onExerciseNotesChange(exercise.id, it)
            },
            label = { Text("Exercise notes") },
            minLines = 1,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CompactSavedSet(set: WorkoutSetLog) {
    val performance = when {
        set.weightKg != null && set.reps != null -> "${set.weightKg.cleanNumber()} × ${set.reps}"
        set.holdMillis != null -> "${(set.holdMillis / 1000.0).cleanNumber()} sec"
        set.reps != null -> "${set.reps} reps"
        set.weightKg != null -> "${set.weightKg.cleanNumber()} kg"
        else -> "No result"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${set.ordinal}", color = LocalVibePalette.current.textFaint, modifier = Modifier.width(28.dp))
        Text(performance, color = LocalVibePalette.current.textSecondary, modifier = Modifier.weight(1f))
        set.rpe?.let { Text("RPE ${it.cleanNumber()}", color = LocalVibePalette.current.textFaint) }
    }
}

@Composable
private fun HoldTimer(seconds: String, onSecondsChange: (String) -> Unit) {
    var running by rememberSaveable { mutableStateOf(false) }
    var startedAt by rememberSaveable { mutableLongStateOf(0L) }
    var displayedMillis by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(running, startedAt) {
        while (running) {
            displayedMillis = SystemClock.elapsedRealtime() - startedAt
            delay(50)
        }
    }
    IconButton(onClick = {
            if (running) {
                displayedMillis = SystemClock.elapsedRealtime() - startedAt
                running = false
                onSecondsChange("%.1f".format(displayedMillis / 1000.0))
            } else {
                displayedMillis = 0
                startedAt = SystemClock.elapsedRealtime()
                running = true
            }
        }) {
            Icon(if (running) Icons.Outlined.Stop else Icons.Outlined.PlayArrow, if (running) "Stop hold" else "Start hold")
    }
}

private fun compactSetLabel(type: TrackingType): String = when (type) {
    TrackingType.WEIGHT_REPS -> "kg × reps"
    TrackingType.HOLD, TrackingType.SKILL_HOLD, TrackingType.ROM_MEASUREMENT -> "Seconds"
    else -> "Reps"
}

private fun parseCompactSet(type: TrackingType, performance: String, rpe: String): SetDraft? {
    val parsedRpe = rpe.takeIf { it.isNotBlank() }?.toDoubleOrNull() ?: if (rpe.isBlank()) null else return null
    if (parsedRpe != null && parsedRpe !in 0.0..10.0) return null
    val cleaned = performance.trim().lowercase().replace("kg", "").replace('×', 'x')
    return when (type) {
        TrackingType.WEIGHT_REPS -> {
            val values = cleaned.split(Regex("\\s*x\\s*"), limit = 2)
            val weight = values.getOrNull(0)?.trim()?.toDoubleOrNull()
            val reps = values.getOrNull(1)?.trim()?.toIntOrNull()
            if (weight == null || weight < 0 || reps == null || reps <= 0) null
            else SetDraft(weightKg = weight, reps = reps, rpe = parsedRpe)
        }
        TrackingType.HOLD, TrackingType.SKILL_HOLD, TrackingType.ROM_MEASUREMENT -> {
            val seconds = cleaned.removeSuffix("s").trim().toDoubleOrNull()
            if (seconds == null || seconds <= 0) null
            else SetDraft(holdMillis = (seconds * 1000).toLong(), rpe = parsedRpe)
        }
        else -> {
            val reps = cleaned.toIntOrNull()
            if (reps == null || reps <= 0) null else SetDraft(reps = reps, rpe = parsedRpe)
        }
    }
}

private fun Double.cleanNumber(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

@Composable
private fun NumericField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { candidate -> if (candidate.isEmpty() || candidate.matches(Regex("\\d*(\\.\\d*)?"))) onChange(candidate) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier,
    )
}

@Composable
private fun ExerciseLibraryScreen(exercises: List<ExerciseSummary>, onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    ScreenList {
        item {
            Text("Exercise library", style = MaterialTheme.typography.headlineLarge)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; onSearch(it) },
                label = { Text("Search by name, alias or muscle") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(exercises, key = { it.id }) { exercise ->
            VibeCard {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text("${exercise.tag.name.lowercase()} · ${exercise.trackingType.name.lowercase().replace('_', ' ')}", color = LocalVibePalette.current.textSecondary)
            }
        }
    }
}

@Composable
private fun HistoryDayScreen(state: MainUiState, onBack: () -> Unit) {
    val sex = if(LocalContext.current.getSharedPreferences("settings",0).getBoolean("female",false)) AnatomySex.FEMALE else AnatomySex.MALE
    val day = state.selectedHistoryDay ?: return
    val date = LocalDate.ofEpochDay(day)
    var selectedMuscle by rememberSaveable(day) { mutableStateOf<String?>(null) }
    ScreenList(modifier = Modifier.statusBarsPadding()) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
                Column {
                    Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), style = MaterialTheme.typography.headlineMedium)
                    Text("Reconstructed from records up to the end of this day", color = LocalVibePalette.current.textSecondary)
                }
            }
        }
        item {
            VibeCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MuscleMap(
                        sex,
                        AnatomyView.FRONT,
                        state.recency.associate { it.muscleId to it.band },
                        { selectedMuscle = it },
                        Modifier.weight(1f),
                        selectedMuscle,
                    )
                    MuscleMap(
                        sex,
                        AnatomyView.BACK,
                        state.recency.associate { it.muscleId to it.band },
                        { selectedMuscle = it },
                        Modifier.weight(1f),
                        selectedMuscle,
                    )
                }
                selectedMuscle?.let { Text(it.replace('_', ' '), fontWeight = FontWeight.Bold) }
            }
        }
        val activities = state.historyDay?.activities.orEmpty()
        if (activities.isEmpty()) item { VibeCard { Text("No logged activity") } }
        items(activities) { activity ->
            VibeCard {
                Text(activity.title, style = MaterialTheme.typography.titleMedium)
                Text(activity.detail, color = LocalVibePalette.current.textSecondary)
                if (activity.notes.isNotBlank()) Text(activity.notes, color = LocalVibePalette.current.textFaint)
            }
        }
    }
}

@Composable
private fun StyleScreen(selectedId: String, onSelect: (String) -> Unit, onRemoveDemo: () -> Unit) {
    ScreenList {
        item {
            Text("Style", style = MaterialTheme.typography.headlineLarge)
            Text("Every screen uses semantic design tokens. New palettes require no screen changes.", color = LocalVibePalette.current.textSecondary)
        }
        items(VibePalettes.builtIns.values.toList(), key = { it.id }) { palette ->
            PaletteCard(palette, selectedId == palette.id) { onSelect(palette.id) }
        }
        item {
            VibeCard {
                Text("Development data", style = MaterialTheme.typography.titleLarge)
                Text("Remove demo workouts, programmes and trackers while keeping the exercise database.", color = LocalVibePalette.current.textSecondary)
                OutlinedButton(onClick = onRemoveDemo, modifier = Modifier.fillMaxWidth()) { Text("Remove demo data") }
            }
        }
    }
}

@Composable
private fun PaletteCard(palette: VibePalette, selected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = LocalVibePalette.current.surface),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LocalVibePalette.current.accent else LocalVibePalette.current.border),
        shape = RoundedCornerShape(VibeShapes.card),
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(palette.accent, palette.recencyUnder24, palette.heatmapThreePlus).forEach { color -> Box(Modifier.size(28.dp).background(color, RoundedCornerShape(50))) }
            Text(palette.displayName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            if (selected) Icon(Icons.Outlined.Check, "Selected")
        }
    }
}

@Composable
private fun VibeCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = LocalVibePalette.current
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = BorderStroke(1.dp, palette.border),
        shape = RoundedCornerShape(VibeShapes.card),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth().padding(VibeSpacing.medium), verticalArrangement = Arrangement.spacedBy(VibeSpacing.small)) { content() }
    }
}

@Composable
private fun ScreenList(
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = VibeSpacing.medium, vertical = VibeSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.medium),
        content = content,
    )
}
