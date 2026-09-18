package com.petermathie.vibetrainer.data.seed

import com.petermathie.vibetrainer.domain.model.DemoWorkout
import com.petermathie.vibetrainer.domain.model.SeedExercise
import com.petermathie.vibetrainer.domain.model.SeedProgramme
import com.petermathie.vibetrainer.domain.model.TrainingMode

object SeedData {
    const val VERSION = 1

    val programmes = listOf(
        SeedProgramme(
            id = "strength-planche-push",
            name = "Planche + push",
            mode = TrainingMode.STRENGTH,
            exercises = listOf(
                SeedExercise("handstand-1", "Handstand", "Skill practice"),
                SeedExercise("planche", "Planche", "4–6 quality holds", 120),
                SeedExercise("easy-muscle-up", "Easy muscle-up practice", "Technique work", 120),
                SeedExercise("bench-press", "Bench press", "3–4 challenging sets", 180),
                SeedExercise("dips-1", "Dips", "3 challenging sets", 120),
                SeedExercise("leg-raises", "Leg raises", "3 sets", 60),
            ),
        ),
        SeedProgramme(
            id = "strength-legs-dips",
            name = "Legs + dips",
            mode = TrainingMode.STRENGTH,
            exercises = listOf(
                SeedExercise("handstand-2", "Handstand", "Skill practice"),
                SeedExercise("dips-2", "Dips", "Working sets", 120),
                SeedExercise("squat", "Squat", "3–4 working sets", 180),
                SeedExercise("lunge", "Lunge", "3 working sets", 120),
                SeedExercise("cossack-zercher-strength", "Cossack Zercher", "4–6 reps", 120),
                SeedExercise("jefferson-curl", "Jefferson curl", "Controlled reps", 120),
            ),
        ),
        SeedProgramme(
            id = "strength-muscleup-pull",
            name = "Muscle-up + pull",
            mode = TrainingMode.STRENGTH,
            exercises = listOf(
                SeedExercise("handstand-3", "Handstand", "Skill practice"),
                SeedExercise("muscle-up", "Muscle-up practice", "1–3 reps", 120),
                SeedExercise("easy-planche", "Easy planche practice", "Quality holds", 120),
                SeedExercise("pullups", "Pull-ups / negative pull-ups", "4–6 reps", 120),
                SeedExercise("ohp", "Overhead press", "Working sets", 180),
                SeedExercise("back-extensions", "Back extensions", "4 sets", 90),
            ),
        ),

        SeedProgramme(
            id = "stretch-front-splits",
            name = "Front Splits",
            mode = TrainingMode.STRETCHING,
            exercises = listOf(
                SeedExercise("half-split", "Half Split", "3 × 5 Mississippi"),
                SeedExercise("lizard-lunge", "Lizard Lunge", "3 × 5 Mississippi", note = "Quad active"),
                SeedExercise("front-split-hold", "Front Split Hold", "30 sec holds", 60),
            ),
        ),
        SeedProgramme(
            id = "stretch-forward-fold",
            name = "Forward Fold",
            mode = TrainingMode.STRETCHING,
            exercises = listOf(
                SeedExercise("forward-fold", "Forward Fold", "10 × 1 Mississippi", 30),
                SeedExercise("seated-pike-pulses", "Seated Pike Pulses", "10 reps", 30),
                SeedExercise("active-standing-forward-fold", "Active Standing Forward Fold", "45–60 sec", 60),
            ),
        ),
        SeedProgramme(
            id = "stretch-side-splits",
            name = "Side Splits",
            mode = TrainingMode.STRETCHING,
            exercises = listOf(
                SeedExercise("frog-rocks", "Frog Rocks", "8–10 reps", 30),
                SeedExercise("cossack-zerchers", "Cossack Zerchers", "4–6 reps", 120),
                SeedExercise("side-split-hold", "Side Split Hold", "20–30 sec", 60),
            ),
        ),
        SeedProgramme(
            id = "stretch-bridge",
            name = "Bridge",
            mode = TrainingMode.STRETCHING,
            exercises = listOf(
                SeedExercise("wall-shoulder-stretch", "Wall Shoulder Stretch", "45–60 sec"),
                SeedExercise("overhead-triceps-stretch", "Overhead Triceps Stretch", "45–60 sec"),
                SeedExercise("bridge-hold", "Bridge Hold", "20–30 sec", 30),
            ),
        ),
    )

    val demoWorkouts = listOf(
        DemoWorkout("demo-2026-09-15", "15 Sept", "strength-planche-push", 33, 12, "Demo"),
        DemoWorkout("demo-2026-09-13", "13 Sept", "strength-legs-dips", 15, 6, "Demo"),
        DemoWorkout("demo-2026-09-11", "11 Sept", "strength-muscleup-pull", 24, 9, "Demo"),
        DemoWorkout("demo-2026-09-01", "1 Sept", "strength-planche-push", 15, 6, "Planche band progression · Demo"),
        DemoWorkout("demo-2026-08-25", "25 Aug", "strength-planche-push", 15, 6, "Planche band progression · Demo"),
    )

    val habits = listOf("Piano", "Meditation", "Stretching", "Workouts")
}
