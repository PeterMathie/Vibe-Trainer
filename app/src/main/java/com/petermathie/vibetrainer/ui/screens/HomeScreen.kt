package com.petermathie.vibetrainer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petermathie.vibetrainer.data.seed.SeedData
import com.petermathie.vibetrainer.domain.model.AnatomySex
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.anatomy.AnatomyView
import com.petermathie.vibetrainer.ui.anatomy.MuscleMap
import com.petermathie.vibetrainer.ui.components.VibeOutlinedCard
import com.petermathie.vibetrainer.ui.components.VibePill
import com.petermathie.vibetrainer.ui.theme.VibeColors

@Composable
fun HomeScreen(
    mode: TrainingMode,
    anatomySex: AnatomySex,
    onAnatomySexChange: (AnatomySex) -> Unit,
    onContinueWorkout: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 22.dp,
            end = 22.dp,
            top = 2.dp,
            bottom = 126.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            ActiveSessionCard(
                mode = mode,
                onContinueWorkout = onContinueWorkout,
            )
        }

        item {
            RecoveryMapCard(
                mode = mode,
                anatomySex = anatomySex,
                onAnatomySexChange = onAnatomySexChange,
            )
        }

        item { TodayHabitsCard() }

        item { RecentSessionsCard(mode = mode) }
    }
}

@Composable
private fun ActiveSessionCard(
    mode: TrainingMode,
    onContinueWorkout: () -> Unit,
) {
    val title = if (mode == TrainingMode.STRENGTH) "Planche + push" else "Front Splits"
    val progress = if (mode == TrainingMode.STRENGTH) 0.21f else 0.33f
    val progressLabel = if (mode == TrainingMode.STRENGTH) "21%" else "33%"

    VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(50),
                    color = VibeColors.Accent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            tint = VibeColors.InkOnAccent,
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACTIVE SESSION",
                        color = VibeColors.TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.1.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        color = VibeColors.TextPrimary,
                        fontSize = 27.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }

                Text(
                    text = progressLabel,
                    color = VibeColors.Accent,
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = VibeColors.Accent,
                trackColor = VibeColors.BottomBarSelected,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onContinueWorkout),
                shape = RoundedCornerShape(18.dp),
                color = VibeColors.Accent,
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Continue workout",
                        color = VibeColors.InkOnAccent,
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = VibeColors.InkOnAccent,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecoveryMapCard(
    mode: TrainingMode,
    anatomySex: AnatomySex,
    onAnatomySexChange: (AnatomySex) -> Unit,
) {
    val groupScores = if (mode == TrainingMode.STRENGTH) {
        mapOf(
            "CHEST" to 0.46f,
            "TRICEPS" to 0.44f,
            "SHOULDERS_FRONT" to 0.42f,
            "SHOULDERS_SIDE" to 0.40f,
            "BACK_UPPER" to 0.28f,
            "LATS" to 0.27f,
        )
    } else {
        mapOf(
            "HAMSTRINGS" to 0.82f,
            "ADDUCTORS" to 0.78f,
            "HIP_FLEXORS" to 0.60f,
            "SHOULDERS_FRONT" to 0.52f,
            "TRICEPS" to 0.49f,
            "BACK_LOWER" to 0.35f,
        )
    }

    VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(
                    text = if (mode == TrainingMode.STRENGTH) "RECOVERY MAP" else "STRETCH MAP",
                    color = VibeColors.Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.2.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (mode == TrainingMode.STRENGTH) "Muscle\nfreshness" else "Stretch\nrecency",
                        color = VibeColors.TextPrimary,
                        fontSize = 28.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    VibePill(
                        text = if (mode == TrainingMode.STRENGTH) {
                            "Recency, not readiness"
                        } else {
                            "Last meaningful stretch"
                        },
                    )
                }
            }

            HorizontalDivider(color = VibeColors.Divider)

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                RecencyLegend(mode = mode)
                Spacer(modifier = Modifier.height(16.dp))
                AnatomySelector(
                    selected = anatomySex,
                    onSelected = onAnatomySexChange,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = VibeColors.DiagramBackground,
                    border = BorderStroke(1.dp, VibeColors.DiagramLine),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DiagramColumn(
                            modifier = Modifier.weight(1f),
                            label = anatomySex.name + " · FRONT",
                        ) {
                            MuscleMap(
                                sex = anatomySex,
                                view = AnatomyView.FRONT,
                                groupScores = groupScores,
                            )
                        }
                        DiagramColumn(
                            modifier = Modifier.weight(1f),
                            label = anatomySex.name + " · BACK",
                        ) {
                            MuscleMap(
                                sex = anatomySex,
                                view = AnatomyView.BACK,
                                groupScores = groupScores,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VibeColors.CardRaised,
                ) {
                    Text(
                        text = if (mode == TrainingMode.STRENGTH) {
                            "Chest · trained 11 hours ago · Bench press · 1.0 set-equivalents"
                        } else {
                            "Hamstrings · stretched 2 days ago · Forward Fold · Pike Pulses"
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        color = VibeColors.TextMuted,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagramColumn(
    modifier: Modifier,
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = VibeColors.DiagramInk,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun RecencyLegend(mode: TrainingMode) {
    val labels = if (mode == TrainingMode.STRENGTH) {
        listOf(
            "Under 24h" to VibeColors.RecencyUnder24,
            "24–48h" to VibeColors.Recency24To48,
            "48–72h" to VibeColors.Recency48To72,
            "Ready" to VibeColors.RecencyReady,
        )
    } else {
        listOf(
            "Today" to VibeColors.RecencyUnder24,
            "1–2d" to VibeColors.Recency24To48,
            "3–4d" to VibeColors.Recency48To72,
            "5d+" to VibeColors.RecencyReady,
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEach { pair ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = RoundedCornerShape(50),
                    color = pair.second,
                ) {}
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = pair.first,
                    color = VibeColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun AnatomySelector(
    selected: AnatomySex,
    onSelected: (AnatomySex) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "BODY",
            color = VibeColors.TextFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
        )
        AnatomyChoice(
            label = "Male",
            selected = selected == AnatomySex.MALE,
            onClick = { onSelected(AnatomySex.MALE) },
        )
        AnatomyChoice(
            label = "Female",
            selected = selected == AnatomySex.FEMALE,
            onClick = { onSelected(AnatomySex.FEMALE) },
        )
    }
}

@Composable
private fun AnatomyChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) VibeColors.Accent else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) VibeColors.Accent else VibeColors.BorderSubtle,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (selected) VibeColors.InkOnAccent else VibeColors.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TodayHabitsCard() {
    VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "TODAY",
                color = VibeColors.Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            SeedData.habits.forEach { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(habit, color = VibeColors.TextPrimary, fontSize = 16.sp)
                    Text("○", color = VibeColors.TextMuted, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun RecentSessionsCard(mode: TrainingMode) {
    val rows = if (mode == TrainingMode.STRENGTH) {
        SeedData.demoWorkouts.take(3).map { session ->
            Triple(
                session.dateLabel,
                SeedData.programmes.firstOrNull { it.id == session.programmeId }?.name ?: "Workout",
                session.durationMinutes.toString() + " min · " + session.workingSets + " sets",
            )
        }
    } else {
        listOf(
            Triple("16 Sept", "Side Splits · Forward Fold · Bridge", "45 min"),
            Triple("14 Sept", "Front Splits · Forward Fold · Side Splits", "15 min"),
            Triple("28 Aug", "Front Splits · Forward Fold · Bridge", "2 sessions"),
        )
    }

    VibeOutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = if (mode == TrainingMode.STRENGTH) "RECENT WORKOUTS" else "RECENT STRETCHING",
                color = VibeColors.Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            rows.forEach { row ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(row.first, color = VibeColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(row.third, color = VibeColors.TextMuted, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(row.second, color = VibeColors.TextMuted, fontSize = 14.sp)
                }
            }
        }
    }
}
