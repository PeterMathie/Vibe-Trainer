package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.petermathie.vibetrainer.domain.model.AnatomySex
import com.petermathie.vibetrainer.domain.model.TrainingMode
import com.petermathie.vibetrainer.ui.components.TrainingModeSwitcher
import com.petermathie.vibetrainer.ui.components.VibeBottomBar
import com.petermathie.vibetrainer.ui.components.VibeDestination
import com.petermathie.vibetrainer.ui.components.VibeHeader
import com.petermathie.vibetrainer.ui.screens.HabitsScreen
import com.petermathie.vibetrainer.ui.screens.HomeScreen
import com.petermathie.vibetrainer.ui.screens.LibraryScreen
import com.petermathie.vibetrainer.ui.screens.PlanScreen
import com.petermathie.vibetrainer.ui.screens.ProgressScreen
import com.petermathie.vibetrainer.ui.screens.WorkoutScreen
import com.petermathie.vibetrainer.ui.theme.VibeColors

@Composable
fun VibeTrainerApp() {
    var destination by rememberSaveable { mutableStateOf(VibeDestination.HOME) }
    var mode by rememberSaveable { mutableStateOf(TrainingMode.STRENGTH) }
    var anatomySex by rememberSaveable { mutableStateOf(AnatomySex.MALE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeColors.AppBackground),
    ) {
        VibeHeader()
        TrainingModeSwitcher(
            mode = mode,
            onModeChange = { mode = it },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            when (destination) {
                VibeDestination.HOME -> HomeScreen(
                    mode = mode,
                    anatomySex = anatomySex,
                    onAnatomySexChange = { anatomySex = it },
                    onContinueWorkout = { destination = VibeDestination.WORKOUT },
                )
                VibeDestination.PLAN -> PlanScreen(mode = mode)
                VibeDestination.WORKOUT -> WorkoutScreen(mode = mode)
                VibeDestination.PROGRESS -> ProgressScreen(mode = mode)
                VibeDestination.HABITS -> HabitsScreen()
                VibeDestination.LIBRARY -> LibraryScreen(mode = mode)
            }

            VibeBottomBar(
                selected = destination,
                onSelect = { destination = it },
                workoutActive = true,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
