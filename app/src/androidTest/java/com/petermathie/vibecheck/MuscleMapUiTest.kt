package com.petermathie.vibecheck

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibecheck.domain.model.AnatomySex
import com.petermathie.vibecheck.ui.anatomy.AnatomyView
import com.petermathie.vibecheck.ui.anatomy.MuscleMap
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuscleMapUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun allMapVariantsExposeRegionsAndSelectionToAccessibility() {
        var selected = mutableStateOf<String?>(null)
        compose.setContent {
            selected = remember { mutableStateOf(null) }
            VibeCheckTheme {
                Column {
                    AnatomySex.entries.forEach { sex ->
                        AnatomyView.entries.forEach { view ->
                            MuscleMap(
                                sex = sex,
                                view = view,
                                states = emptyMap(),
                                onMuscleTap = { selected.value = it },
                                modifier = androidx.compose.ui.Modifier.width(100.dp),
                                selectedMuscleId = selected.value,
                            )
                        }
                    }
                }
            }
        }

        val expectedFront = setOf(
            "TRAPEZIUS", "CHEST", "SHOULDERS FRONT", "SHOULDERS SIDE", "BICEPS", "TRICEPS",
            "FOREARMS", "OBLIQUES", "CORE", "ABDUCTORS", "QUADS", "CALVES", "ADDUCTORS",
        )
        val expectedBack = setOf(
            "TRAPEZIUS", "SHOULDERS REAR", "SHOULDERS SIDE", "TRICEPS", "FOREARMS", "RHOMBOIDS",
            "LATS", "BACK LOWER", "OBLIQUES", "GLUTES", "ABDUCTORS", "ADDUCTORS", "HAMSTRINGS", "CALVES",
        )

        AnatomySex.entries.forEach { sex ->
            AnatomyView.entries.forEach { view ->
                val map = compose.onNodeWithContentDescription(
                    "${sex.name.lowercase()} ${view.name.lowercase()} muscle recency map",
                )
                map.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "No muscle selected"))
                val labels = map.fetchSemanticsNode().config[SemanticsActions.CustomActions]
                    .map { it.label.removePrefix("Inspect ").substringBefore(':').uppercase() }
                    .toSet()
                assertEquals(if (view == AnatomyView.FRONT) expectedFront else expectedBack, labels)
            }
        }

        val front = compose.onNodeWithContentDescription("male front muscle recency map")
        val inspectChest = front.fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label.startsWith("Inspect CHEST") }
        compose.runOnIdle { inspectChest.action() }
        front.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected chest"))

        val back = compose.onNodeWithContentDescription("male back muscle recency map")
        val inspectLats = back.fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label.startsWith("Inspect LATS") }
        compose.runOnIdle { inspectLats.action() }
        back.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected lats"))
        front.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "No muscle selected"))
    }
}
