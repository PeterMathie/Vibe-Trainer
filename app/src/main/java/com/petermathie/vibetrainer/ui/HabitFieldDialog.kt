package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity
import com.petermathie.vibetrainer.domain.tracker.HabitFieldForm
import java.util.UUID

private data class ChoiceDraft(val id: String, val value: String)

@Composable
internal fun HabitFieldDialog(
    vm: EditorViewModel,
    field: TrackerFieldEntity,
    habitColour: Color,
    dismiss: () -> Unit,
) {
    var form by remember(field.id) {
        val initial = HabitFieldForm.from(field)
        val size = initial.choiceValues.size
        val light = initial.choiceLightThrough
            .takeIf { it in 0 until (size - 1) }
            ?: ((size - 1) / 3).coerceAtLeast(0)
        val dark = initial.choiceDarkFrom
            .takeIf { it in 1 until size && it > light }
            ?: ((size * 2 + 2) / 3).coerceIn(light + 1, (size - 1).coerceAtLeast(light + 1))
        mutableStateOf(
            if (initial.type == HabitFieldForm.CHOICE && size >= 2) {
                initial.copy(choiceLightThrough = light, choiceDarkFrom = dark)
            } else {
                initial
            },
        )
    }
    val choices = remember(field.id) {
        mutableStateListOf<ChoiceDraft>().apply {
            addAll(form.choiceValues.map { ChoiceDraft(UUID.randomUUID().toString(), it) })
        }
    }
    fun defaultLightThrough(size: Int) = ((size - 1) / 3).coerceAtLeast(0)
    fun defaultDarkFrom(size: Int): Int {
        val light = defaultLightThrough(size)
        return ((size * 2 + 2) / 3).coerceIn(light + 1, (size - 1).coerceAtLeast(light + 1))
    }
    fun syncChoices(
        lightThrough: Int = form.choiceLightThrough,
        darkFrom: Int = form.choiceDarkFrom,
    ) {
        form = form.copy(
            options = choices.joinToString("\n") { it.value },
            choiceLightThrough = lightThrough,
            choiceDarkFrom = darkFrom,
        )
    }
    val choiceOrder = rememberReorderState(choices.map { it.id }) { key, from, to ->
        val item = choices.removeAt(from)
        choices.add(to, item)
        syncChoices()
    }
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
                        TextButton(
                            onClick = {
                                form = form.copy(type = value)
                                if (value == HabitFieldForm.CHOICE && choices.isEmpty()) {
                                    repeat(3) {
                                        choices += ChoiceDraft(UUID.randomUUID().toString(), "")
                                    }
                                    syncChoices(defaultLightThrough(choices.size), defaultDarkFrom(choices.size))
                                }
                            },
                        ) {
                            Text((if (form.type == value) "✓ " else "") + habitTypeLabel(value))
                        }
                    }
                    if (form.isNumeric) {
                        EditField("Unit, for example minutes or grams", form.unit) {
                            form = form.copy(unit = it)
                        }
                    }
                    if (form.type == HabitFieldForm.CHOICE) {
                        Text("Choice shade scale", style = MaterialTheme.typography.titleMedium)
                        Text("Drag choices from lightest at the top to darkest at the bottom.")
                        choiceOrder.ordered(choices) { it.id }.forEachIndexed { index, choice ->
                            val shade = when {
                                index <= form.choiceLightThrough -> "Light"
                                index >= form.choiceDarkFrom -> "Dark"
                                else -> "Medium"
                            }
                            val alpha = when (shade) {
                                "Light" -> 0.38f
                                "Medium" -> 0.68f
                                else -> 1f
                            }
                            Surface(
                                color = habitColour.copy(alpha = alpha),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    ReorderHandle(
                                        choiceOrder,
                                        choice.id,
                                        choice.value.ifBlank { "unnamed choice" },
                                        enabled = choices.size > 1,
                                    )
                                    OutlinedTextField(
                                        value = choice.value,
                                        onValueChange = { updated ->
                                            val itemIndex = choices.indexOfFirst { it.id == choice.id }
                                            choices[itemIndex] = choice.copy(value = updated)
                                            syncChoices()
                                        },
                                        label = { Text(shade) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(
                                        enabled = choices.size > 2,
                                        onClick = {
                                            choices.removeAll { it.id == choice.id }
                                            val light = form.choiceLightThrough.coerceIn(0, choices.lastIndex - 1)
                                            val dark = form.choiceDarkFrom.coerceIn(light + 1, choices.lastIndex)
                                            syncChoices(light, dark)
                                        },
                                    ) { Text("×") }
                                }
                            }
                        }
                        VibeActionButton(
                            "Add choice",
                            {
                                choices += ChoiceDraft(UUID.randomUUID().toString(), "")
                                syncChoices()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            importance = ActionImportance.SECONDARY,
                        )
                        ChoiceBoundaryPicker(
                            label = "Light shade through",
                            choices = choiceOrder.ordered(choices) { it.id },
                            selectedIndex = form.choiceLightThrough,
                            allowedIndices = 0 until form.choiceDarkFrom,
                        ) {
                            syncChoices(lightThrough = it)
                        }
                        ChoiceBoundaryPicker(
                            label = "Dark shade starts at",
                            choices = choiceOrder.ordered(choices) { it.id },
                            selectedIndex = form.choiceDarkFrom,
                            allowedIndices = (form.choiceLightThrough + 1)..choices.lastIndex,
                        ) {
                            syncChoices(darkFrom = it)
                        }
                        if (!form.areChoicesValid || choices.any { it.value.isBlank() }) {
                            Text(
                                "Enter at least two unique, named choices.",
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
                enabled = form.canSave && choices.none { it.value.isBlank() },
                onClick = {
                    vm.save(form.applyTo(field))
                    dismiss()
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ChoiceBoundaryPicker(
    label: String,
    choices: List<ChoiceDraft>,
    selectedIndex: Int,
    allowedIndices: IntRange,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            ) {
                Text(choices.getOrNull(selectedIndex)?.value?.ifBlank { "Unnamed choice" } ?: "Choose boundary")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allowedIndices.filter { it in choices.indices }.forEach { index ->
                    DropdownMenuItem(
                        text = { Text(choices[index].value.ifBlank { "Unnamed choice ${index + 1}" }) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        },
                        modifier = Modifier.semantics {
                            contentDescription =
                                "$label: ${choices[index].value.ifBlank { "Unnamed choice ${index + 1}" }}"
                        },
                    )
                }
            }
        }
    }
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
