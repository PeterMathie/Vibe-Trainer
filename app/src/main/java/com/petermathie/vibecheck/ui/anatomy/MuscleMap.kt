package com.petermathie.vibecheck.ui.anatomy

import android.graphics.Region
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import com.petermathie.vibecheck.domain.model.AnatomySex
import com.petermathie.vibecheck.domain.model.MuscleRecencyBand
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import com.petermathie.vibecheck.ui.theme.LocalVibeReducedMotion
import androidx.compose.ui.semantics.stateDescription
import kotlin.math.min

enum class AnatomyView { FRONT, BACK }

internal const val MUSCLE_COLOR_TRANSITION_MILLIS = 70

private data class ParsedOutline(val def: OutlinePathDef, val path: Path)
private data class ParsedMuscle(val def: MusclePathDef, val path: Path, val region: Region)

@Composable
fun MuscleMap(
    sex: AnatomySex,
    view: AnatomyView,
    states: Map<String, MuscleRecencyBand>,
    onMuscleTap: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedMuscleId: String? = null,
) {
    val palette = LocalVibePalette.current
    val reducedMotion = LocalVibeReducedMotion.current
    val diagram = when (sex to view) {
        AnatomySex.MALE to AnatomyView.FRONT -> MuscleDiagrams.MaleFront
        AnatomySex.MALE to AnatomyView.BACK -> MuscleDiagrams.MaleBack
        AnatomySex.FEMALE to AnatomyView.FRONT -> MuscleDiagrams.FemaleFront
        AnatomySex.FEMALE to AnatomyView.BACK -> MuscleDiagrams.FemaleBack
        else -> MuscleDiagrams.MaleFront
    }
    val outlines = remember(diagram.id) {
        diagram.outline.map { ParsedOutline(it, PathParser().parsePathString(it.pathData).toPath()) }
    }
    val muscles = remember(diagram.id) {
        diagram.muscles.map { definition ->
            val path = PathParser().parsePathString(definition.pathData).toPath()
            val clip = Region(0, 0, diagram.viewBoxWidth.toInt() + 1, diagram.viewBoxHeight.toInt() + 1)
            ParsedMuscle(definition, path, Region().apply { setPath(path.asAndroidPath(), clip) })
        }
    }
    val groups = muscles.map { it.def.group }.distinct()
    val selectedGroup = selectedMuscleId?.takeIf(groups::contains)
    val animatedColors = groups.associateWith { group ->
        val targetColor = when (states[group] ?: MuscleRecencyBand.NEVER) {
            MuscleRecencyBand.UNDER_24_HOURS -> palette.recencyUnder24
            MuscleRecencyBand.HOURS_24_TO_48 -> palette.recency24To48
            MuscleRecencyBand.HOURS_48_TO_72 -> palette.recency48To72
            MuscleRecencyBand.DAYS_3_TO_7 -> palette.recency3To7
            MuscleRecencyBand.OVER_7_DAYS -> palette.recencyOver7
            MuscleRecencyBand.NEVER -> palette.recencyNever
        }
        val color by animateColorAsState(
            targetValue = targetColor,
            animationSpec = if (reducedMotion) snap() else tween(durationMillis = MUSCLE_COLOR_TRANSITION_MILLIS),
            label = "muscle recency $group",
        )
        color
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(diagram.viewBoxWidth / diagram.viewBoxHeight)
            .semantics {
                contentDescription = "${sex.name.lowercase()} ${view.name.lowercase()} muscle recency map"
                stateDescription = selectedGroup?.let { "Selected ${it.replace('_', ' ').lowercase()}" }
                    ?: "No muscle selected"
                customActions = groups.map { group ->
                    CustomAccessibilityAction("Inspect ${group.replace('_', ' ')}: ${(states[group] ?: MuscleRecencyBand.NEVER).name.lowercase().replace('_', ' ')}") {
                        onMuscleTap(group)
                        true
                    }
                }
            }
            .pointerInput(diagram.id, muscles) {
                detectTapGestures { tap ->
                    val scale = min(size.width / diagram.viewBoxWidth, size.height / diagram.viewBoxHeight)
                    val offsetX = (size.width - diagram.viewBoxWidth * scale) / 2f
                    val offsetY = (size.height - diagram.viewBoxHeight * scale) / 2f
                    val vectorX = (tap.x - offsetX) / scale
                    val vectorY = (tap.y - offsetY) / scale
                    val hit = muscles.lastOrNull { item ->
                        val direct = item.region.contains(vectorX.toInt(), vectorY.toInt())
                        val mirroredX = 2f * diagram.centerX - vectorX
                        direct || (item.def.side == BodySide.LEFT && item.region.contains(mirroredX.toInt(), vectorY.toInt()))
                    }
                    hit?.def?.group?.let(onMuscleTap)
                }
            },
    ) {
        val scale = min(size.width / diagram.viewBoxWidth, size.height / diagram.viewBoxHeight)
        val offsetX = (size.width - diagram.viewBoxWidth * scale) / 2f
        val offsetY = (size.height - diagram.viewBoxHeight * scale) / 2f
        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, Offset.Zero)
        }) {
            outlines.forEach { item ->
                drawPathWithMirror(item.path, item.def.side, diagram.centerX, Color.Transparent, palette.diagramLine)
            }
            muscles.forEach { item ->
                val color = animatedColors.getValue(item.def.group)
                val selected = item.def.group == selectedMuscleId
                drawPathWithMirror(
                    path = item.path,
                    side = item.def.side,
                    centerX = diagram.centerX,
                    color = color,
                    strokeColor = if (selected) palette.accent else color,
                    strokeWidth = if (selected) 3f else 2.5f,
                )
            }
        }
    }
}

private fun DrawScope.drawPathWithMirror(
    path: Path,
    side: BodySide,
    centerX: Float,
    color: Color,
    strokeColor: Color,
    strokeWidth: Float = 1f,
) {
    fun drawCurrent() {
        drawPath(path, color)
        drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
    drawCurrent()
    if (side == BodySide.LEFT) {
        withTransform({ scale(-1f, 1f, Offset(centerX, 0f)) }) { drawCurrent() }
    }
}
