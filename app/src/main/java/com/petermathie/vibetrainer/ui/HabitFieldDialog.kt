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
        title = { Text("Habit field") },
        text = {
            LazyColumn {
                item {
                    EditField("Name", form.name) { form = form.copy(name = it) }
                    EditField("Unit (minutes, grams, pages…)", form.unit) {
                        form = form.copy(unit = it)
                    }
                    HabitFieldForm.TYPES.forEach { value ->
                        TextButton(onClick = { form = form.copy(type = value) }) {
                            Text((if (form.type == value) "✓ " else "") + value.lowercase())
                        }
                    }
                    if (form.type == HabitFieldForm.CHOICE) {
                        EditField("Choices (comma-separated)", form.options) {
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
                        EditField("Optional target", form.target) {
                            form = form.copy(target = it)
                        }
                        HabitFieldForm.COMPARISONS.forEach { value ->
                            TextButton(onClick = { form = form.copy(comparison = value) }) {
                                Text((if (form.comparison == value) "✓ " else "") + value)
                            }
                        }
                        if (form.comparison == HabitFieldForm.RANGE && form.target.isNotBlank()) {
                            EditField("Target maximum", form.targetMaximum) {
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
