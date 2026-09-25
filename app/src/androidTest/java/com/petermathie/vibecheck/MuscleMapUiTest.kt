package com.petermathie.vibecheck

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petermathie.vibecheck.domain.model.AnatomySex
import com.petermathie.vibecheck.ui.anatomy.AnatomyView
import com.petermathie.vibecheck.ui.anatomy.MuscleMap
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

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
                    "${sex.name.lowercase()} ${view.name.lowercase()} freshness map",
                )
                map.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "No muscle selected"))
                val labels = map.fetchSemanticsNode().config[SemanticsActions.CustomActions]
                    .map { it.label.removePrefix("Inspect ").substringBefore(':').uppercase() }
                    .toSet()
                assertEquals(if (view == AnatomyView.FRONT) expectedFront else expectedBack, labels)
            }
        }

        val front = compose.onNodeWithContentDescription("male front freshness map")
        val inspectChest = front.fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label.startsWith("Inspect CHEST") }
        compose.runOnIdle { inspectChest.action() }
        front.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected chest"))

        val back = compose.onNodeWithContentDescription("male back freshness map")
        val inspectLats = back.fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label.startsWith("Inspect LATS") }
        compose.runOnIdle { inspectLats.action() }
        back.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Selected lats"))
        front.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "No muscle selected"))
    }

    @Test
    fun neutralOutlineLayerRendersTheCompleteSilhouette() {
        compose.setContent {
            VibeCheckTheme {
                MuscleMap(
                    sex = AnatomySex.MALE,
                    view = AnatomyView.FRONT,
                    states = emptyMap(),
                    onMuscleTap = {},
                    modifier = androidx.compose.ui.Modifier.width(240.dp),
                )
            }
        }

        val pixels = compose.onNodeWithContentDescription("male front freshness map").captureToImage().toPixelMap()
        val background = pixels[0, 0]
        var headPixels = 0
        var feetPixels = 0
        for (x in 0 until pixels.width) {
            for (y in 0 until pixels.height) {
                val pixel = pixels[x, y]
                val differsFromBackground =
                    abs(pixel.red - background.red) > 0.04f ||
                        abs(pixel.green - background.green) > 0.04f ||
                        abs(pixel.blue - background.blue) > 0.04f ||
                        abs(pixel.alpha - background.alpha) > 0.04f
                if (differsFromBackground) {
                    if (y < pixels.height * 0.15f) headPixels++
                    if (y > pixels.height * 0.87f) feetPixels++
                }
            }
        }
        assertTrue("Expected rendered head outline, found $headPixels pixels", headPixels > 20)
        assertTrue("Expected rendered feet outline, found $feetPixels pixels", feetPixels > 20)
    }
}
