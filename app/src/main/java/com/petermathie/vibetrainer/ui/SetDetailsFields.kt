package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.petermathie.vibetrainer.data.local.BandEntity
import com.petermathie.vibetrainer.data.local.ExerciseVariationEntity
import com.petermathie.vibetrainer.domain.workout.SetDetailsForm

@Composable
internal fun SetDetailsFields(
    form: SetDetailsForm,
    hold: Boolean,
    weighted: Boolean,
    bands: List<BandEntity>,
    selectedBandIds: List<String>,
    variations: List<ExerciseVariationEntity>,
    exerciseId: String,
    error: String?,
    onFormChange: (SetDetailsForm) -> Unit,
    onBandSelectionChange: (List<String>) -> Unit,
) {
    EditField(
        if (hold) "Seconds" else if (weighted) "Weight × reps" else "Reps",
        form.performance,
    ) { onFormChange(form.copy(performance = it)) }
    EditField("RPE (optional)", form.rpe) { onFormChange(form.copy(rpe = it)) }
    Row {
        Checkbox(form.warmUp, { onFormChange(form.copy(warmUp = it)) })
        Text("Warm-up")
    }
    Row {
        Checkbox(form.failed, { onFormChange(form.copy(failed = it)) })
        Text("Failed/partial — store zero")
    }
    Text("Bands: ${selectedBandIds.sumOf { id -> bands.find { it.id == id }?.widthCentimetres ?: 0.0 }} cm total")
    bands.forEach { band ->
        Row {
            Checkbox(
                band.id in selectedBandIds,
                { checked ->
                    onBandSelectionChange(
                        if (checked) selectedBandIds + band.id else selectedBandIds - band.id,
                    )
                },
                modifier = Modifier.semantics { contentDescription = "Use ${band.name} band" },
            )
            Text("${band.name} (${band.widthCentimetres}cm)")
        }
    }
    Text("Variation")
    TextButton(onClick = { onFormChange(form.copy(variationId = null)) }) {
        Text(if (form.variationId == null) "✓ Default" else "Default")
    }
    variations.filter { it.exerciseId == exerciseId }.forEach { variation ->
        TextButton(onClick = { onFormChange(form.copy(variationId = variation.id)) }) {
            Text((if (form.variationId == variation.id) "✓ " else "") + variation.name)
        }
    }
    EditField("Left ${if (hold) "seconds" else "reps"}", form.leftValue) {
        onFormChange(form.copy(leftValue = it))
    }
    EditField("Right ${if (hold) "seconds" else "reps"}", form.rightValue) {
        onFormChange(form.copy(rightValue = it))
    }
    EditField("Added weight (kg)", form.addedWeight) {
        onFormChange(form.copy(addedWeight = it))
    }
    EditField("Assistance (kg)", form.assistance) {
        onFormChange(form.copy(assistance = it))
    }
    EditField("ROM measurement (optional)", form.romValue) {
        onFormChange(form.copy(romValue = it))
    }
    EditField("ROM unit", form.romUnit) {
        onFormChange(form.copy(romUnit = it))
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
