package com.petermathie.vibecheck.domain.tracker

import com.petermathie.vibecheck.data.local.TrackerFieldEntity
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.nio.charset.StandardCharsets
import java.util.UUID

enum class HabitChoiceIntensity(val heatmapLevel: Int) {
    LIGHT(1),
    MEDIUM(2),
    DARK(3),
}

data class HabitChoiceOption(
    val id: String,
    val label: String,
    val intensity: HabitChoiceIntensity,
    val position: Int,
)

fun legacyHabitChoices(
    fieldId: String,
    choiceOptions: String,
    lightThrough: Int,
    darkFrom: Int,
): List<HabitChoiceOption> {
    val labels = choiceOptions.lineSequence().map(String::trim).filter(String::isNotBlank).toList()
    if (labels.isEmpty()) return emptyList()
    val resolvedLight = lightThrough.takeIf { it in 0 until labels.lastIndex }
        ?: ((labels.size - 1) / 3).coerceAtLeast(0)
    val resolvedDark = darkFrom.takeIf { it in 1 until labels.size && it > resolvedLight }
        ?: ((labels.size * 2 + 2) / 3).coerceIn((resolvedLight + 1).coerceAtMost(labels.lastIndex), labels.lastIndex)
    val positions = mutableMapOf<HabitChoiceIntensity, Int>()
    return labels.mapIndexed { index, label ->
        val intensity = when {
            labels.size == 1 || index <= resolvedLight -> HabitChoiceIntensity.LIGHT
            index >= resolvedDark -> HabitChoiceIntensity.DARK
            else -> HabitChoiceIntensity.MEDIUM
        }
        HabitChoiceOption(
            id = "$fieldId:choice:$index",
            label = label,
            intensity = intensity,
            position = positions.getOrDefault(intensity, 0).also { positions[intensity] = it + 1 },
        )
    }
}

fun decodeHabitChoices(field: TrackerFieldEntity): List<HabitChoiceOption> =
    decodeHabitChoices(field.choiceOptionsJson).ifEmpty {
        legacyHabitChoices(field.id, field.choiceOptions, field.choiceLightThrough, field.choiceDarkFrom)
    }

fun decodeHabitChoices(json: String): List<HabitChoiceOption> = runCatching {
    val array = kotlinx.serialization.json.Json.parseToJsonElement(json).jsonArray
    array.map { element ->
        val row = element.jsonObject
        HabitChoiceOption(
            id = row.getValue("id").jsonPrimitive.content,
            label = row.getValue("label").jsonPrimitive.content,
            intensity = HabitChoiceIntensity.valueOf(row.getValue("intensity").jsonPrimitive.content),
            position = row.getValue("position").jsonPrimitive.int,
        )
    }.sortedWith(compareBy<HabitChoiceOption> { it.intensity.ordinal }.thenBy { it.position })
}.getOrDefault(emptyList())

fun encodeHabitChoices(options: List<HabitChoiceOption>): String {
    val normalized = normalizeHabitChoices(options)
    return buildJsonArray {
        normalized.forEach { option ->
            add(buildJsonObject {
                put("id", option.id)
                put("label", option.label)
                put("intensity", option.intensity.name)
                put("position", option.position)
            })
        }
    }.toString()
}

fun normalizeHabitChoices(options: List<HabitChoiceOption>): List<HabitChoiceOption> =
    HabitChoiceIntensity.entries.flatMap { intensity ->
        options.filter { it.intensity == intensity }
            .sortedBy(HabitChoiceOption::position)
            .mapIndexed { index, row -> row.copy(position = index) }
    }

fun moveHabitChoice(
    options: List<HabitChoiceOption>,
    id: String,
    targetIntensity: HabitChoiceIntensity,
    targetPosition: Int,
): List<HabitChoiceOption> {
    val moved = options.firstOrNull { it.id == id } ?: return normalizeHabitChoices(options)
    val remaining = options.filterNot { it.id == id }
    val target = remaining.filter { it.intensity == targetIntensity }.toMutableList()
    target.add(targetPosition.coerceIn(0, target.size), moved.copy(intensity = targetIntensity))
    return normalizeHabitChoices(
        remaining.filter { it.intensity != targetIntensity } + target,
    )
}

fun legacyHabitChoiceSnapshot(
    fieldId: String,
    label: String,
    options: List<HabitChoiceOption>,
): HabitChoiceOption = options.firstOrNull { it.label == label } ?: HabitChoiceOption(
    id = "$fieldId:legacy:${UUID.nameUUIDFromBytes(label.toByteArray(StandardCharsets.UTF_8))}",
    label = label,
    intensity = HabitChoiceIntensity.LIGHT,
    position = 0,
)
