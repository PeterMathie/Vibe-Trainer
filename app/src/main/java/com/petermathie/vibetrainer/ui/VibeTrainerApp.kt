package com.petermathie.vibetrainer.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.petermathie.vibetrainer.domain.style.PaletteContrast
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petermathie.vibetrainer.domain.model.ActivityDay
import com.petermathie.vibetrainer.domain.model.AnatomySex
import com.petermathie.vibetrainer.domain.model.ExerciseSummary
import com.petermathie.vibetrainer.domain.model.MuscleRecency
import com.petermathie.vibetrainer.domain.model.TrainingMode
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
    val palette = rememberVibePalette(prefs)
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
            if (key == "palette") paletteId = preferences.getString(key, VibePalettes.MidnightLime.id)!!
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    VibeTrainerTheme(palette) {
        if (state.selectedHistoryDay != null) {
            BackHandler { viewModel.selectHistoryDay(null) }
            HistoryDayScreen(
                state = state,
                onBack = { viewModel.selectHistoryDay(null) },
                onDayChange = viewModel::selectHistoryDay,
                onModeChange = viewModel::setMode,
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

@Composable
internal fun rememberVibePalette(prefs: android.content.SharedPreferences): VibePalette {
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf("palette", "accent", "background", "surface")) revision++
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    revision
    val id = prefs.getString("palette", VibePalettes.MidnightLime.id)!!
    return if (id == "custom") VibePalettes.MidnightLime.copy(
        id = "custom",
        displayName = "Custom",
        accent = Color(prefs.getInt("accent", 0xFFC2F85A.toInt())),
        background = Color(prefs.getInt("background", 0xFF081017.toInt())),
        surface = Color(prefs.getInt("surface", 0xFF101B23.toInt())),
    ) else VibePalettes.builtIns[id] ?: VibePalettes.MidnightLime
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
                    selected?.lastTrainedAt?.let { Text("Last trained: ${java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm"))}") }
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
fun ActivityHeatmap(days: List<ActivityDay>, onDayClick: (Long) -> Unit) {
    val palette = LocalVibePalette.current
    val counts = days.associate { it.epochDay to it.activityCount }
    var offset by rememberSaveable { mutableStateOf(0L) }
    val today = LocalDate.now().toEpochDay() + offset
    val start = today - 34
    Row {
        TextButton(onClick={offset-=35}){Text("Earlier")}
        Text("${LocalDate.ofEpochDay(start)} – ${LocalDate.ofEpochDay(today)}",Modifier.weight(1f),style=MaterialTheme.typography.labelSmall)
        TextButton(onClick={offset=(offset+35).coerceAtMost(0)},enabled=offset<0){Text("Later")}
    }
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
internal fun HistoryDayScreen(state: MainUiState, onBack: () -> Unit, onDayChange: (Long) -> Unit, onModeChange: (TrainingMode) -> Unit) {
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
            SingleChoiceSegmentedButtonRow {
                TrainingMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.mode == mode,
                        onClick = { onModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, TrainingMode.entries.size),
                        label = { Text(if (mode == TrainingMode.STRENGTH) "Strength" else "Stretching") },
                    )
                }
            }
            Row {
                TextButton(onClick={onDayChange(day-1)}){Text("Previous day")}
                TextButton(onClick={onDayChange(day+1)},enabled=day<LocalDate.now().toEpochDay()){Text("Next day")}
            }
            androidx.compose.material3.Slider(value=day.toFloat(),onValueChange={onDayChange(it.toLong())},valueRange=(LocalDate.now().toEpochDay()-365).toFloat()..LocalDate.now().toEpochDay().toFloat(),steps=364)
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
                selectedMuscle?.let { muscle ->
                    Text(muscle.replace('_', ' '), fontWeight = FontWeight.Bold)
                    val selected=state.recency.find { it.muscleId==muscle }
                    Text(selected?.let { "${it.band.name.replace('_',' ').lowercase()} · ${"%.1f".format(it.setEquivalents)} set-equivalents in the preceding 7 days" } ?: "Never recorded")
                    selected?.lastTrainedAt?.let { Text("Last trained: ${java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm"))}") }
                    selected?.contributingExerciseNames?.let { Text(it.joinToString()) }
                }
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
internal fun StyleScreen(selectedId: String, onSelect: (String) -> Unit, onRemoveDemo: () -> Unit) {
    val prefs=LocalContext.current.getSharedPreferences("settings",0)
    fun storedHex(key: String, fallback: Int) = String.format("#%06X", 0xFFFFFF and prefs.getInt(key, fallback))
    var accent by remember { mutableStateOf(storedHex("accent",0xFFC2F85A.toInt())) }
    var background by remember { mutableStateOf(storedHex("background",0xFF081017.toInt())) }
    var surface by remember { mutableStateOf(storedHex("surface",0xFF101B23.toInt())) }
    var error by remember { mutableStateOf<String?>(null) }
    var applied by remember { mutableStateOf(false) }
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
                Text("Custom palette")
                EditField("Accent hex",accent){accent=it};EditField("Background hex",background){background=it};EditField("Surface hex",surface){surface=it}
                Button(onClick={
                    try {
                        val accentValue=android.graphics.Color.parseColor(accent)
                        val backgroundValue=android.graphics.Color.parseColor(background)
                        val surfaceValue=android.graphics.Color.parseColor(surface)
                        error=PaletteContrast.customPaletteError(accentValue,backgroundValue,surfaceValue)
                        if(error==null) {
                            prefs.edit().putInt("accent",accentValue).putInt("background",backgroundValue).putInt("surface",surfaceValue).apply()
                            onSelect("custom");applied=true
                        }
                    }catch(_:IllegalArgumentException){error="Use valid hex colours, for example #C2F85A"}
                }){Text("Apply custom palette")}
                error?.let { Text(it, color=MaterialTheme.colorScheme.error) }
                if(applied && error==null)Text("Custom palette applied")
            }
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
