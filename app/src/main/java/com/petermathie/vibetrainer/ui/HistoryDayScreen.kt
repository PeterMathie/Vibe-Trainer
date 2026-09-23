package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petermathie.vibetrainer.domain.model.AnatomySex
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.anatomy.AnatomyView
import com.petermathie.vibetrainer.ui.anatomy.MuscleMap
import com.petermathie.vibetrainer.ui.theme.LocalVibePalette
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun HistoryDayScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onDayChange: (Long) -> Unit,
    onModeChange: (TrainingMode) -> Unit,
) {
    val sex = if (LocalContext.current.getSharedPreferences("settings", 0).getBoolean("female", false)) {
        AnatomySex.FEMALE
    } else {
        AnatomySex.MALE
    }
    val day = state.selectedHistoryDay ?: return
    val date = LocalDate.ofEpochDay(day)
    var selectedMuscle by rememberSaveable(day) { mutableStateOf<String?>(null) }
    ScreenList(modifier = Modifier.statusBarsPadding()) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back", tint = LocalVibePalette.current.textPrimary) }
                Text(
                    date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LocalVibePalette.current.textPrimary,
                )
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().semantics { contentDescription = "Historical day and mode controls" },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onDayChange(day - 1) }) {
                    Icon(Icons.Outlined.KeyboardArrowLeft, "Previous day", tint = LocalVibePalette.current.textPrimary)
                }
                SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                    TrainingMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.mode == mode,
                            onClick = { onModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, TrainingMode.entries.size),
                            label = { Text(if (mode == TrainingMode.STRENGTH) "Strength" else "Stretching") },
                        )
                    }
                }
                IconButton(onClick = { onDayChange(day + 1) }, enabled = day < LocalDate.now().toEpochDay()) {
                    Icon(Icons.Outlined.KeyboardArrowRight, "Next day", tint = LocalVibePalette.current.textPrimary)
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
                selectedMuscle?.let { muscle ->
                    Text(muscle.replace('_', ' '), fontWeight = FontWeight.Bold)
                    val selected = state.recency.find { it.muscleId == muscle }
                    Text(selected?.let { "${it.band.name.replace('_', ' ').lowercase()} · ${"%.1f".format(it.setEquivalents)} set-equivalents in the preceding 7 days" } ?: "Never recorded")
                    selected?.lastTrainedAt?.let {
                        Text("Last trained: ${Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm"))}")
                    }
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
