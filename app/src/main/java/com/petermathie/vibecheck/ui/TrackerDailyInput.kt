package com.petermathie.vibecheck.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petermathie.vibecheck.data.local.TrackerDailyValueEntity
import com.petermathie.vibecheck.data.local.TrackerFieldEntity
import com.petermathie.vibecheck.domain.tracker.HabitChoiceOption
import com.petermathie.vibecheck.domain.tracker.decodeHabitChoices
import com.petermathie.vibecheck.ui.theme.LocalVibePalette

@Composable
internal fun HabitDailyInput(
    vm: EditorViewModel,
    field: TrackerFieldEntity,
    value: TrackerDailyValueEntity?,
    epoch: Long?,
    habitColour: Color,
) {
    val haptics = rememberVibeHaptics()
    val onLogged = { haptics.perform(VibeHapticEvent.SUCCESS) }
    key(field.id, epoch) {
        var text by remember { mutableStateOf(value?.numericValue?.toString() ?: value?.textValue.orEmpty()) }
        if (field.valueType == "BOOLEAN") {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            onLogged,
                        )
                    }
                })
                Text(field.name)
            }
        } else {
            when (field.valueType) {
                "CHOICE" -> ChoiceInput(
                    field.name,
                    decodeHabitChoices(field),
                    value?.choiceOptionId,
                    text,
                    habitColour,
                ) { option ->
                    text = option.label
                    epoch?.let { day ->
                        vm.save(
                            TrackerDailyValueEntity(
                                field.id,
                                day,
                                null,
                                null,
                                option.label,
                                "",
                                System.currentTimeMillis(),
                                option.id,
                                option.intensity.name,
                            ),
                            onLogged,
                        )
                    }
                }
                else -> EditField("${field.name}${field.unit?.let { " ($it)" }.orEmpty()}", text) {
                    text = it
                    epoch?.let { day ->
                        val numeric = field.valueType == "NUMBER"
                        when {
                            it.isBlank() -> vm.clearValue(field.id, day)
                            !numeric || it.toDoubleOrNull()?.isFinite() == true -> {
                                vm.save(
                                    TrackerDailyValueEntity(
                                        field.id,
                                        day,
                                        if (numeric) it.toDoubleOrNull() else null,
                                        null,
                                        if (!numeric) it else null,
                                        "",
                                        System.currentTimeMillis(),
                                    ),
                                    onLogged,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceInput(
    label: String,
    options: List<HabitChoiceOption>,
    selectedId: String?,
    selectedLabel: String,
    habitColour: Color,
    onSelect: (HabitChoiceOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedLabel.ifBlank { "Choose…" })
        }
    }
    if (expanded) {
        val palette = LocalVibePalette.current
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text(label) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(options.size.coerceIn(1, 3)),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(options, key = { _, option -> option.id }) { _, option ->
                        val level = option.intensity.heatmapLevel
                        val alpha = when (level) {
                            1 -> 0.38f
                            2 -> 0.68f
                            else -> 1f
                        }
                        val container = habitColour.copy(alpha = alpha)
                        val displayed = container.compositeOver(palette.surface)
                        Surface(
                            onClick = {
                                onSelect(option)
                                expanded = false
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "${option.label}, ${option.intensity.name.lowercase()} shade"
                            },
                            color = container,
                            contentColor = if (displayed.luminance() > 0.5f) Color.Black else Color.White,
                            border = if (option.id == selectedId || selectedId == null && option.label == selectedLabel) BorderStroke(2.dp, palette.textPrimary) else null,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                option.label,
                                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 18.dp),
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { expanded = false }) { Text("Cancel") } },
        )
    }
}

internal fun choiceShadeLevel(
    optionCount: Int,
    index: Int,
    configuredLightThrough: Int,
    configuredDarkFrom: Int,
): Int {
    if (optionCount <= 1 || index !in 0 until optionCount) return 1
    val lastIndex = optionCount - 1
    val lightThrough = configuredLightThrough
        .takeIf { it in 0 until lastIndex }
        ?: (lastIndex / 3)
    val darkFrom = configuredDarkFrom
        .takeIf { it in 1..lastIndex && it > lightThrough }
        ?: ((optionCount * 2 + 2) / 3).coerceAtMost(lastIndex)
    return when {
        index <= lightThrough -> 1
        index >= darkFrom -> 3
        else -> 2
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
