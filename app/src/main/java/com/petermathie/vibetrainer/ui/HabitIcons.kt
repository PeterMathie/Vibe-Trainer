package com.petermathie.vibetrainer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

internal data class HabitIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

internal object HabitIconCatalog {
    val options = listOf(
        HabitIconOption("habit", "Habit", Icons.Outlined.CheckCircle),
        HabitIconOption("piano", "Music", Icons.Outlined.MusicNote),
        HabitIconOption("meditation", "Meditation", Icons.Outlined.SelfImprovement),
        HabitIconOption("protein", "Food", Icons.Outlined.LocalDining),
        HabitIconOption("mood", "Mood", Icons.Outlined.Mood),
        HabitIconOption("journal", "Journal", Icons.Outlined.EditNote),
        HabitIconOption("reading", "Reading", Icons.Outlined.AutoStories),
        HabitIconOption("wellbeing", "Wellbeing", Icons.Outlined.Favorite),
        HabitIconOption("goal", "Goal", Icons.Outlined.Star),
    )

    fun icon(key: String): ImageVector = options.find { it.key == key }?.icon ?: options.first().icon
}
