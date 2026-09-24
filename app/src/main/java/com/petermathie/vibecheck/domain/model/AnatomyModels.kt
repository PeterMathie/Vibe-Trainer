package com.petermathie.vibecheck.domain.model

enum class AnatomyView { FRONT, BACK }

data class AnatomyPreference(
    val sex: AnatomySex = AnatomySex.MALE,
)
