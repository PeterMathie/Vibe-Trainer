package com.petermathie.vibetrainer.domain.model

enum class TrainingMode { STRENGTH, STRETCHING }
enum class AnatomySex { MALE, FEMALE }

data class SeedExercise(
    val id: String,
    val name: String,
    val prescription: String,
    val restSeconds: Int? = null,
    val note: String? = null,
)

data class SeedProgramme(
    val id: String,
    val name: String,
    val mode: TrainingMode,
    val exercises: List<SeedExercise>,
)

data class DemoWorkout(
    val id: String,
    val dateLabel: String,
    val programmeId: String,
    val durationMinutes: Int,
    val workingSets: Int,
    val note: String? = null,
)
