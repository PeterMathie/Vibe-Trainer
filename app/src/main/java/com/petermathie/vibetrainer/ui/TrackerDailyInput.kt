package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.petermathie.vibetrainer.data.local.TrackerDailyValueEntity
import com.petermathie.vibetrainer.data.local.TrackerFieldEntity

@Composable
internal fun HabitDailyInput(
    vm: EditorViewModel,
    field: TrackerFieldEntity,
    value: TrackerDailyValueEntity?,
    epoch: Long?,
) {
    key(field.id, epoch, value?.updatedAt) {
        var text by remember { mutableStateOf(value?.numericValue?.toString() ?: value?.textValue.orEmpty()) }
        if (field.valueType == "BOOLEAN") {
            Row {
                Checkbox(value?.booleanValue == true, { checked ->
                    epoch?.let {
                        vm.save(
                            TrackerDailyValueEntity(
                                field.id,
                                it,
                                null,
                                checked,
                                null,
                                "",
                                System.currentTimeMillis(),
                            ),
                        )
                    }
                })
                Text(field.name)
            }
        } else {
            when (field.valueType) {
                "CHOICE" -> ChoiceInput(
                    field.name,
                    field.choiceOptions.lineSequence().filter(String::isNotBlank).toList(),
                    text,
                ) { text = it }
                else -> EditField("${field.name}${field.unit?.let { " ($it)" }.orEmpty()}", text) {
                    text = it
                }
            }
            val numeric = field.valueType == "NUMBER"
            val valid = text.isNotBlank() && (!numeric || text.toDoubleOrNull()?.isFinite() == true)
            VibeActionButton(
                label = "Save daily total",
                importance = ActionImportance.COMPACT,
                enabled = epoch != null && valid,
                onClick = {
                    epoch?.let { day ->
                        vm.save(
                            TrackerDailyValueEntity(
                                field.id,
                                day,
                                if (numeric) text.toDoubleOrNull() else null,
                                null,
                                if (!numeric) text else null,
                                "",
                                System.currentTimeMillis(),
                            ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun ChoiceInput(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selected.ifBlank { "Choose…" })
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun TargetSummary(field: TrackerFieldEntity) {
    if (field.targetValue == null) return
    val description = if (field.targetComparison == "RANGE") {
        "Between ${field.targetValue} and ${field.targetMaxValue}"
    } else {
        "${targetComparisonLabel(field.targetComparison.orEmpty())} ${field.targetValue}"
    }
    Text("Target: $description ${field.unit.orEmpty()}")
}
