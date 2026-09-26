package com.petermathie.vibecheck.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.petermathie.vibecheck.data.local.TrackerFieldEntity
import com.petermathie.vibecheck.domain.tracker.HabitFieldForm
import com.petermathie.vibecheck.domain.tracker.HabitChoiceIntensity
import com.petermathie.vibecheck.domain.tracker.HabitChoiceOption
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import com.petermathie.vibecheck.ui.theme.habitHeatmapColors
import java.util.UUID

private data class ChoiceDraft(
    val id: String,
    val value: String,
    val intensity: HabitChoiceIntensity,
    val position: Int,
)

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
    val listState = rememberLazyListState()
    val reorderContext = rememberReorderScrollContext(listState)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("What would you like to track?") },
        text = {
            ReorderOverlayHost(Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalReorderScrollContext provides reorderContext) {
                    LazyColumn(state = listState, modifier = Modifier.reorderScrollViewport(reorderContext)) {
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
                                    form.copy(type = value)
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
    val choices = remember(fieldKey) {
        mutableStateListOf<ChoiceDraft>().apply {
            if (form.choices.isEmpty()) {
                repeat(2) {
                    add(ChoiceDraft(UUID.randomUUID().toString(), "", HabitChoiceIntensity.LIGHT, it))
                }
            } else {
                addAll(form.choices.map { ChoiceDraft(it.id, it.label, it.intensity, it.position) })
            }
        }
    }
    LaunchedEffect(revision) {
        if (revision > 0) onFormChange(editorForm)
    }
    fun syncChoices() {
        val normalized = HabitChoiceIntensity.entries.flatMap { intensity ->
            choices.filter { it.intensity == intensity }
                .sortedBy(ChoiceDraft::position)
                .mapIndexed { index, choice -> choice.copy(position = index) }
        }
        choices.clear()
        choices.addAll(normalized)
        editorForm = baseForm.copy(
            options = choices.joinToString("\n") { it.value },
            choices = choices.map { HabitChoiceOption(it.id, it.value.trim(), it.intensity, it.position) },
        )
        revision++
    }
    fun marker(intensity: HabitChoiceIntensity) = "$fieldKey:intensity:${intensity.name}"
    val choiceKeys = HabitChoiceIntensity.entries.flatMap { intensity ->
        listOf(marker(intensity)) + choices.filter { it.intensity == intensity }
            .sortedBy(ChoiceDraft::position)
            .map(ChoiceDraft::id)
    }
    lateinit var choiceOrder: ReorderState
    choiceOrder = rememberReorderState(choiceKeys) { key, _, _ ->
        val order = choiceOrder.keysSnapshot()
        var currentIntensity = HabitChoiceIntensity.LIGHT
        val updated = mutableListOf<ChoiceDraft>()
        val positions = mutableMapOf<HabitChoiceIntensity, Int>()
        order.forEach { orderedKey ->
            val markerIntensity = HabitChoiceIntensity.entries.firstOrNull { marker(it) == orderedKey }
            if (markerIntensity != null) {
                currentIntensity = markerIntensity
            } else {
                choices.firstOrNull { it.id == orderedKey }?.let { choice ->
                    val position = positions.getOrDefault(currentIntensity, 0)
                    updated += choice.copy(intensity = currentIntensity, position = position)
                    positions[currentIntensity] = position + 1
                }
            }
        }
        choices.clear()
        choices.addAll(updated)
        syncChoices()
    }
    val heatmapColors = LocalVibePalette.current.habitHeatmapColors(habitColour)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val markerKeys = HabitChoiceIntensity.entries.map(::marker).toSet()
        val choiceIds = choices.mapTo(mutableSetOf(), ChoiceDraft::id)
        val displayedKeys = choiceOrder.keysSnapshot().filter { it in markerKeys || it in choiceIds }
        displayedKeys.forEachIndexed { orderIndex, orderedKey ->
            key(orderedKey) {
                val intensity = HabitChoiceIntensity.entries.firstOrNull { marker(it) == orderedKey }
                    ?: displayedKeys.take(orderIndex + 1).mapNotNull { candidate ->
                        HabitChoiceIntensity.entries.firstOrNull { marker(it) == candidate }
                    }.last()
                val shadeColor = when (intensity) {
                    HabitChoiceIntensity.LIGHT -> heatmapColors.low
                    HabitChoiceIntensity.MEDIUM -> heatmapColors.medium
                    HabitChoiceIntensity.DARK -> heatmapColors.strong
                }
                if (orderedKey == marker(intensity)) {
                    val count = displayedKeys.drop(orderIndex + 1)
                        .takeWhile { candidate -> HabitChoiceIntensity.entries.none { marker(it) == candidate } }
                        .size
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${intensity.name.lowercase().replaceFirstChar(Char::uppercase)} · $count", style = MaterialTheme.typography.labelLarge)
                        Text(
                            when (intensity) {
                                HabitChoiceIntensity.LIGHT -> "Uses the light heat-map colour."
                                HabitChoiceIntensity.MEDIUM -> "Uses the medium heat-map colour."
                                HabitChoiceIntensity.DARK -> "Uses the dark heat-map colour."
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                        ReorderItem(choiceOrder, orderedKey, orderIndex) {
                            Surface(
                                color = shadeColor.copy(alpha = 0.18f),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (count == 0) 48.dp else 1.dp)
                                    .semantics { contentDescription = "${intensity.name.lowercase()} choice drop target" },
                            ) {}
                        }
                    }
                } else {
                    choices.firstOrNull { it.id == orderedKey }?.let { choice ->
                        ReorderItem(choiceOrder, choice.id, orderIndex) {
                        Surface(
                            color = shadeColor,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "${choice.value.ifBlank { "Unnamed choice" }} intensity controls"
                                    customActions = HabitChoiceIntensity.entries
                                        .filter { it != choice.intensity }
                                        .map { target ->
                                            CustomAccessibilityAction("Move to ${target.name.lowercase().replaceFirstChar(Char::uppercase)}") {
                                                val targetPosition = choices.count { it.intensity == target }
                                                val itemIndex = choices.indexOfFirst { it.id == choice.id }
                                                if (itemIndex < 0) return@CustomAccessibilityAction false
                                                choices[itemIndex] = choice.copy(intensity = target, position = targetPosition)
                                                syncChoices()
                                                true
                                            }
                                        }
                                },
                        ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
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
                                .semantics {
                                    val position = displayedKeys.subList(
                                        displayedKeys.indexOf(marker(intensity)) + 1,
                                        orderIndex + 1,
                                    ).count { candidate -> HabitChoiceIntensity.entries.none { marker(it) == candidate } }
                                    contentDescription = "${intensity.name.lowercase()} choice $position"
                                },
                        )
                        IconButton(
                            enabled = choices.size > 2,
                            onClick = {
                                choiceOrder.cancel()
                                choices.removeAll { it.id == choice.id }
                                syncChoices()
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Remove ${choice.value.ifBlank { "choice ${choice.position + 1}" }}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            }
                        }
                        }
                    }
                }
            }
        }
        }
        Button(
            onClick = {
                choices += ChoiceDraft(
                    UUID.randomUUID().toString(),
                    "",
                    HabitChoiceIntensity.LIGHT,
                    choices.count { it.intensity == HabitChoiceIntensity.LIGHT },
                )
                syncChoices()
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Add choice" },
            shape = MaterialTheme.shapes.medium,
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
