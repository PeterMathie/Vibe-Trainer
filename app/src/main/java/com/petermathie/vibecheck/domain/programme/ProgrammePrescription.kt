package com.petermathie.vibecheck.domain.programme

fun prescriptionSummary(
    targetSets: Int?,
    targetRepsMin: Int?,
    targetRepsMax: Int?,
    targetHoldSeconds: Int?,
    targetRpe: Double?,
): String = buildList {
    add("${targetSets ?: 3} sets")
    when {
        targetRepsMin != null && targetRepsMax != null -> add("$targetRepsMin–$targetRepsMax reps")
        targetRepsMin != null -> add("$targetRepsMin reps")
        targetHoldSeconds != null -> add("$targetHoldSeconds seconds")
    }
    targetRpe?.let { add("RPE ${formatPrescriptionNumber(it)}") }
}.joinToString(" · ")

private fun formatPrescriptionNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
