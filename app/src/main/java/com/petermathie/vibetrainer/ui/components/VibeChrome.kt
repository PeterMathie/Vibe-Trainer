package com.petermathie.vibetrainer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.theme.VibeColors

enum class VibeDestination(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Outlined.Home),
    PLAN("Plan", Icons.Outlined.MenuBook),
    WORKOUT("Workout", Icons.Outlined.FitnessCenter),
    PROGRESS("Progress", Icons.Outlined.BarChart),
    HABITS("Habits", Icons.Outlined.Checklist),
    LIBRARY("Library", Icons.Outlined.LibraryBooks),
}

@Composable
fun VibeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VibeColors.HeaderBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = VibeColors.Accent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "VT",
                        color = VibeColors.InkOnAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
            Text(
                text = "Vibe Trainer",
                color = VibeColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
        }
        HorizontalDivider(color = VibeColors.Divider)
    }
}

@Composable
fun TrainingModeSwitcher(
    mode: TrainingMode,
    onModeChange: (TrainingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .border(
                width = 1.dp,
                color = VibeColors.BorderSubtle,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ModeSegment(
            label = "Strength",
            selected = mode == TrainingMode.STRENGTH,
            onClick = { onModeChange(TrainingMode.STRENGTH) },
        )
        ModeSegment(
            label = "Stretching",
            selected = mode == TrainingMode.STRETCHING,
            onClick = { onModeChange(TrainingMode.STRETCHING) },
        )
    }
}

@Composable
private fun RowScope.ModeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) VibeColors.Accent else VibeColors.AppBackground,
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (selected) VibeColors.InkOnAccent else VibeColors.TextMuted,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
fun VibeBottomBar(
    selected: VibeDestination,
    onSelect: (VibeDestination) -> Unit,
    workoutActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = VibeColors.BottomBar,
            border = BorderStroke(1.dp, VibeColors.BorderSubtle),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VibeDestination.entries.forEach { destination ->
                    BottomBarItem(
                        destination = destination,
                        selected = selected == destination,
                        showDot = workoutActive && destination == VibeDestination.WORKOUT,
                        onClick = { onSelect(destination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    destination: VibeDestination,
    selected: Boolean,
    showDot: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        color = if (selected) VibeColors.BottomBarSelected else VibeColors.BottomBar,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) VibeColors.TextPrimary else VibeColors.TextMuted,
                )
                if (showDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(9.dp)
                            .background(VibeColors.Notification, RoundedCornerShape(50)),
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = destination.label,
                color = if (selected) VibeColors.TextPrimary else VibeColors.TextMuted,
                fontSize = 12.sp,
            )
        }
    }
}
