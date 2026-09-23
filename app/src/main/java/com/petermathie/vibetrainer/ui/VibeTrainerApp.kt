package com.petermathie.vibetrainer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.MoreHoriz
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

internal enum class Destination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    PROGRAMMES("Programmes", Icons.Outlined.FitnessCenter),
    ACTIVE_WORKOUT("Workout", Icons.Outlined.PlayArrow),
    PROGRESS("Progress", Icons.Outlined.BarChart),
    MORE("More", Icons.Outlined.MoreHoriz),
    EXERCISES("Exercises", Icons.Outlined.LibraryBooks),
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
                PrimaryNavigationBar(destination) { destination = it }
            },
        ) { padding ->
            BackHandler(destination in moreDestinations) { destination = Destination.MORE }
            Column(Modifier.fillMaxSize().padding(padding)) {
                if(error != null) TextButton(onClick = { editor.error.value = null }) { Text(error.orEmpty(),color=MaterialTheme.colorScheme.error) }
                when (destination) {
                    Destination.HOME -> HomeScreen(state, viewModel::selectHistoryDay) { destination = Destination.ACTIVE_WORKOUT }
                    Destination.PROGRAMMES -> ProgrammeEditor(editor, state.mode) { dayId ->
                        viewModel.startWorkout(dayId) { destination = Destination.ACTIVE_WORKOUT }
                    }
                    Destination.ACTIVE_WORKOUT -> WorkoutEditor(
                        vm = editor,
                        workoutId = state.activeWorkout?.id,
                        onFinish = { id -> viewModel.finishWorkout(id) { destination = Destination.HOME } },
                        onChoose = { destination = Destination.PROGRAMMES },
                    )
                    Destination.EXERCISES -> ExerciseEditor(editor)
                    Destination.PROGRESS -> ProgressScreen(editor)
                    Destination.MORE -> MoreScreen { destination = it }
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

private val primaryDestinations = listOf(
    Destination.HOME,
    Destination.PROGRAMMES,
    Destination.PROGRESS,
    Destination.MORE,
)

private val moreDestinations = setOf(
    Destination.EXERCISES,
    Destination.HABITS,
    Destination.HISTORY,
    Destination.MEASUREMENTS,
    Destination.SETTINGS,
    Destination.STYLE,
)

@Composable
internal fun PrimaryNavigationBar(selected: Destination, onSelect: (Destination) -> Unit) {
    val selectedItem = if (selected in moreDestinations) Destination.MORE else selected
    NavigationBar(Modifier.fillMaxWidth().navigationBarsPadding()) {
        primaryDestinations.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
internal fun MoreScreen(onSelect: (Destination) -> Unit) {
    ScreenList {
        item { Text("More", style = MaterialTheme.typography.headlineLarge) }
        items(moreDestinations.toList(), key = { it.name }) { destination ->
            VibeActionButton(
                label = destination.label,
                onClick = { onSelect(destination) },
                modifier = Modifier.fillMaxWidth(),
                importance = ActionImportance.SECONDARY,
                icon = destination.icon,
            )
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

@Composable
internal fun HomeScreen(state: MainUiState, onDayClick: (Long) -> Unit, onContinue: () -> Unit) {
    val sex = if(LocalContext.current.getSharedPreferences("settings",0).getBoolean("female",false)) AnatomySex.FEMALE else AnatomySex.MALE
    var selectedMuscle by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = state.recency.firstOrNull { it.muscleId == selectedMuscle }
    ScreenList {
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
    Row(
        Modifier.fillMaxWidth().semantics { contentDescription = "Activity date navigation" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        VibeActionButton("Earlier", { offset -= 35 }, importance = ActionImportance.COMPACT)
        Text(
            "${LocalDate.ofEpochDay(start).format(DateTimeFormatter.ofPattern("d MMM"))} – ${LocalDate.ofEpochDay(today).format(DateTimeFormatter.ofPattern("d MMM"))}",
            Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        VibeActionButton("Later", { offset = (offset + 35).coerceAtMost(0) }, importance = ActionImportance.COMPACT, enabled = offset < 0)
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
internal fun VibeCard(content: @Composable ColumnScope.() -> Unit) {
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
internal fun ScreenList(
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
