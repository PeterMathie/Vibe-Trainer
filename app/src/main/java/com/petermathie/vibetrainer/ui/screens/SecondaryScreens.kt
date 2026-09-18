package com.petermathie.vibetrainer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petermathie.vibetrainer.data.seed.SeedData
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.components.VibeOutlinedCard
import com.petermathie.vibetrainer.ui.components.VibePill
import com.petermathie.vibetrainer.ui.theme.VibeColors

@Composable
fun PlanScreen(mode: TrainingMode) {
    val programmes = SeedData.programmes.filter { it.mode == mode }
    ScreenList(title = if (mode == TrainingMode.STRENGTH) "Training plan" else "Stretching plan") {
        items(programmes) { programme ->
            VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(programme.name, color = VibeColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Text(programme.exercises.size.toString() + " exercises", color = VibeColors.TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.padding(top = 6.dp))
                    programme.exercises.take(4).forEach { exercise ->
                        Text("• " + exercise.name, color = VibeColors.TextMuted, fontSize = 14.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        VibePill("Edit")
                        VibePill("Duplicate")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutScreen(mode: TrainingMode) {
    val programme = SeedData.programmes.first { it.mode == mode }
    ScreenList(title = programme.name, eyebrow = "ACTIVE SESSION") {
        items(programme.exercises) { exercise ->
            VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(exercise.name, color = VibeColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        VibePill("Substitute")
                    }
                    Text(exercise.prescription, color = VibeColors.TextMuted, fontSize = 14.sp)
                    if (exercise.restSeconds != null) {
                        Text("Rest " + exercise.restSeconds + "s", color = VibeColors.TextFaint, fontSize = 13.sp)
                    }
                    Text(
                        text = if (mode == TrainingMode.STRENGTH) "Previous: 3 sets · tap to log next set" else "Previous: logged duration/reps · tap to log",
                        modifier = Modifier.padding(top = 10.dp),
                        color = VibeColors.Accent,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(mode: TrainingMode) {
    ScreenList(title = "Progress") {
        item {
            MetricCard(
                label = if (mode == TrainingMode.STRENGTH) "LATEST PR" else "FLEXIBILITY PR",
                value = if (mode == TrainingMode.STRENGTH) "Bench press · 80 kg × 8" else "Front split · 7 cm from floor",
                detail = if (mode == TrainingMode.STRENGTH) "Weight · reps · estimated 1RM · holds" else "ROM · hold duration · side-to-side history",
            )
        }
        item {
            MetricCard(
                label = "BODY",
                value = "Bodyweight · measurements · photos",
                detail = "History stays shared between Strength and Stretching.",
            )
        }
        item {
            MetricCard(
                label = "HISTORY",
                value = "Search and filter workouts",
                detail = "Historical editing, aliases, PRs and CSV export.",
            )
        }
    }
}

@Composable
fun HabitsScreen() {
    ScreenList(title = "Habits") {
        items(SeedData.habits) { habit ->
            VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(habit, color = VibeColors.TextPrimary, fontSize = 18.sp)
                    Text("○", color = VibeColors.TextMuted, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(mode: TrainingMode) {
    val exercises = SeedData.programmes
        .filter { it.mode == mode }
        .flatMap { it.exercises }
        .distinctBy { it.id }

    ScreenList(title = "Library") {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = VibeColors.CardRaised,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, VibeColors.BorderSubtle),
            ) {
                Text(
                    text = "Search by exercise name, alias, muscle or movement type",
                    modifier = Modifier.padding(16.dp),
                    color = VibeColors.TextMuted,
                    fontSize = 14.sp,
                )
            }
        }
        items(exercises) { exercise ->
            VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(exercise.name, color = VibeColors.TextPrimary, fontWeight = FontWeight.Medium)
                    Text(exercise.prescription, color = VibeColors.TextMuted, fontSize = 13.sp)
                }
            }
        }
        item {
            MetricCard(
                label = "DATA",
                value = "Backup · restore · CSV",
                detail = "Local-first storage, last-backup indicator, kg by default.",
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, detail: String) {
    VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, color = VibeColors.Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.8.sp)
            Text(value, modifier = Modifier.padding(top = 8.dp), color = VibeColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text(detail, modifier = Modifier.padding(top = 6.dp), color = VibeColors.TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ScreenList(
    title: String,
    eyebrow: String? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 22.dp,
            end = 22.dp,
            top = 8.dp,
            bottom = 126.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                if (eyebrow != null) {
                    Text(eyebrow, color = VibeColors.Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                }
                Text(title, color = VibeColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        content()
    }
}
