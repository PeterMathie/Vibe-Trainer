package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import com.petermathie.vibetrainer.domain.tracker.HabitFieldForm

@Composable
internal fun HabitFieldDialog(
    vm: EditorViewModel,
    field: TrackerFieldEntity,
    dismiss: () -> Unit,
) {
    var form by remember(field.id) { mutableStateOf(HabitFieldForm.from(field)) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("What would you like to track?") },
        text = {
            LazyColumn {
                item {
                    Text("Add one thing you want to record for this habit.")
                    EditField("Name, for example Minutes or Protein", form.name) {
                        form = form.copy(name = it)
                    }
                    Text("How will you record it?", style = MaterialTheme.typography.labelLarge)
                    HabitFieldForm.TYPES.forEach { value ->
                        TextButton(onClick = { form = form.copy(type = value) }) {
                            Text((if (form.type == value) "✓ " else "") + habitTypeLabel(value))
                        }
                    }
                    if (form.isNumeric) {
                        EditField("Unit, for example minutes or grams", form.unit) {
                            form = form.copy(unit = it)
                        }
                    }
                    if (form.type == HabitFieldForm.CHOICE) {
                        EditField("Choices, separated by commas", form.options) {
                            form = form.copy(options = it)
                        }
                        if (!form.areChoicesValid) {
                            Text(
                                "Enter at least two unique choices",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    if (form.isNumeric) {
                        Text("Goal (optional)", style = MaterialTheme.typography.labelLarge)
                        Text("Choose how the recorded value should compare with your goal.")
                        HabitFieldForm.COMPARISONS.forEach { value ->
                            TextButton(onClick = { form = form.copy(comparison = value) }) {
                                Text((if (form.comparison == value) "✓ " else "") + targetComparisonLabel(value))
                            }
                        }
                        EditField(
                            if (form.comparison == HabitFieldForm.RANGE) "Minimum goal" else "Goal value",
                            form.target,
                        ) {
                            form = form.copy(target = it)
                        }
                        if (form.comparison == HabitFieldForm.RANGE && form.target.isNotBlank()) {
                            EditField("Maximum goal", form.targetMaximum) {
                                form = form.copy(targetMaximum = it)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = form.canSave,
                onClick = {
                    vm.save(form.applyTo(field))
                    dismiss()
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } },
    )
}

internal fun habitTypeLabel(type: String): String = when (type) {
    "BOOLEAN" -> "Yes or no"
    "NUMBER" -> "Number"
    "COUNT", "DURATION", "RATING" -> "Number"
    "TEXT" -> "Written note"
    HabitFieldForm.CHOICE -> "Choose from a list"
    "DATETIME" -> "Written note"
    else -> type.lowercase().replace('_', ' ')
}

internal fun targetComparisonLabel(comparison: String): String = when (comparison) {
    "AT_LEAST" -> "At least"
    "AT_MOST" -> "At most"
    "EXACTLY" -> "Exactly"
    HabitFieldForm.RANGE -> "Between two values"
    else -> comparison.lowercase().replace('_', ' ')
}
