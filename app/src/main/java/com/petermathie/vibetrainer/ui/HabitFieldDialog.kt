package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
        mutableStateOf(resolvedHabitFieldForm(field))
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
                                form = if (value == HabitFieldForm.CHOICE && form.choiceValues.isEmpty()) {
                                    form.copy(type = value, choiceLightThrough = 0, choiceDarkFrom = 2)
                                } else {
                                    form.copy(type = value)
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
                        ChoiceScaleEditor(field.id, form, habitColour) { form = it }
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

internal fun resolvedHabitFieldForm(field: TrackerFieldEntity): HabitFieldForm {
    val initial = HabitFieldForm.from(field)
    val size = initial.choiceValues.size
    if (initial.type != HabitFieldForm.CHOICE || size < 2) return initial
    val light = initial.choiceLightThrough
        .takeIf { it in 0 until (size - 1) }
        ?: ((size - 1) / 3).coerceAtLeast(0)
    val dark = initial.choiceDarkFrom
        .takeIf { it in 1 until size && it > light }
        ?: ((size * 2 + 2) / 3).coerceIn(light + 1, size - 1)
    return initial.copy(choiceLightThrough = light, choiceDarkFrom = dark)
}

@Composable
internal fun ChoiceScaleEditor(
    fieldKey: String,
    form: HabitFieldForm,
    habitColour: Color,
    onFormChange: (HabitFieldForm) -> Unit,
) {
    val baseForm = remember(fieldKey) { form }
    var editorForm by remember(fieldKey) { mutableStateOf(form) }
    var revision by remember(fieldKey) { mutableIntStateOf(0) }
    var lightThrough by remember(fieldKey) { mutableIntStateOf(form.choiceLightThrough) }
    var darkFrom by remember(fieldKey) { mutableIntStateOf(form.choiceDarkFrom) }
    val choices = remember(fieldKey) {
        mutableStateListOf<ChoiceDraft>().apply {
            val values = form.choiceValues
            if (values.isEmpty()) {
                repeat(3) { add(ChoiceDraft(UUID.randomUUID().toString(), "")) }
            } else {
                addAll(values.map { ChoiceDraft(UUID.randomUUID().toString(), it) })
            }
        }
    }
    LaunchedEffect(revision) {
        if (revision > 0) onFormChange(editorForm)
    }
    fun syncChoices(
        updatedLightThrough: Int = lightThrough,
        updatedDarkFrom: Int = darkFrom,
    ) {
        lightThrough = updatedLightThrough
        darkFrom = updatedDarkFrom
        editorForm = baseForm.copy(
            options = choices.joinToString("\n") { it.value },
            choiceLightThrough = updatedLightThrough,
            choiceDarkFrom = updatedDarkFrom,
        )
        revision++
    }
    val choiceKeys = remember(choices.map { it.id }) { choices.map { it.id } }
    val choiceOrder = rememberReorderState(choiceKeys) { _, from, to ->
        val item = choices.removeAt(from)
        choices.add(to, item)
        syncChoices()
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        choiceOrder.ordered(choices) { it.id }.forEachIndexed { index, choice ->
            val shade = when {
                index <= lightThrough -> "Light"
                index >= darkFrom -> "Dark"
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
                Column(
                    Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
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
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics { contentDescription = "Choice ${index + 1}" },
                        )
                        IconButton(
                            enabled = choices.size > 2,
                            onClick = {
                                choices.removeAll { it.id == choice.id }
                                val light = lightThrough.coerceIn(0, choices.lastIndex - 1)
                                val dark = darkFrom.coerceIn(light + 1, choices.lastIndex)
                                syncChoices(light, dark)
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Remove ${choice.value.ifBlank { "choice ${index + 1}" }}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (index == lightThrough) {
                ShadeBoundary("LIGHT", "MEDIUM", "Light to medium boundary")
            }
            if (index == darkFrom - 1) {
                ShadeBoundary("MEDIUM", "DARK", "Medium to dark boundary")
            }
        }
        Button(
            onClick = {
                val ordered = choiceOrder.ordered(choices) { it.id }.toMutableList()
                ordered.add(darkFrom, ChoiceDraft(UUID.randomUUID().toString(), ""))
                choices.clear()
                choices.addAll(ordered)
                syncChoices(updatedDarkFrom = darkFrom + 1)
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Add choice" },
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
        }
        if (
            choices.size < 2 ||
            choices.map { it.value.trim() }.distinct().size != choices.size ||
            choices.any { it.value.isBlank() }
        ) {
            Text(
                "Enter at least two unique, named choices.",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ShadeBoundary(
    above: String,
    below: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(above, style = MaterialTheme.typography.labelSmall)
        androidx.compose.material3.HorizontalDivider(Modifier.weight(1f))
        Text(below, style = MaterialTheme.typography.labelSmall)
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
