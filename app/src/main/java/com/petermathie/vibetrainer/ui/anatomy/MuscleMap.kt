package com.petermathie.vibetrainer.ui.anatomy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.petermathie.vibetrainer.domain.model.AnatomySex
import com.petermathie.vibetrainer.ui.theme.VibeColors
import kotlin.math.min

enum class AnatomyView { FRONT, BACK }

private data class ParsedOutline(
    val def: OutlinePathDef,
    val path: Path,
)

private data class ParsedMuscle(
    val def: MusclePathDef,
    val path: Path,
)

@Composable
fun MuscleMap(
    sex: AnatomySex,
    view: AnatomyView,
    groupScores: Map<String, Float>,
    modifier: Modifier = Modifier,
) {
    val diagram = when (sex to view) {
        AnatomySex.MALE to AnatomyView.FRONT -> MuscleDiagrams.MaleFront
        AnatomySex.MALE to AnatomyView.BACK -> MuscleDiagrams.MaleBack
        AnatomySex.FEMALE to AnatomyView.FRONT -> MuscleDiagrams.FemaleFront
        AnatomySex.FEMALE to AnatomyView.BACK -> MuscleDiagrams.FemaleBack
        else -> MuscleDiagrams.MaleFront
    }

    val outlines = remember(diagram.id) {
        diagram.outline.map {
            ParsedOutline(it, PathParser().parsePathString(it.pathData).toPath())
        }
    }
    val muscles = remember(diagram.id) {
        diagram.muscles.map {
            ParsedMuscle(it, PathParser().parsePathString(it.pathData).toPath())
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(diagram.viewBoxWidth / diagram.viewBoxHeight),
    ) {
        val scale = min(
            size.width / diagram.viewBoxWidth,
            size.height / diagram.viewBoxHeight,
        )
        val drawWidth = diagram.viewBoxWidth * scale
        val drawHeight = diagram.viewBoxHeight * scale
        val offsetX = (size.width - drawWidth) / 2f
        val offsetY = (size.height - drawHeight) / 2f

        withTransform({
            translate(left = offsetX, top = offsetY)
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            outlines.forEach { item ->
                drawPathWithMirror(
                    path = item.path,
                    side = item.def.side,
                    centerX = diagram.centerX,
                    color = VibeColors.DiagramBody,
                    strokeColor = VibeColors.DiagramLine,
                )
            }

            muscles.forEach { item ->
                val score = groupScores[item.def.group] ?: 0f
                drawPathWithMirror(
                    path = item.path,
                    side = item.def.side,
                    centerX = diagram.centerX,
                    color = muscleColor(score),
                    strokeColor = VibeColors.DiagramLine.copy(alpha = 0.34f),
                    strokeWidth = 0.65f,
                )
            }
        }
    }
}

private fun muscleColor(score: Float): Color = when {
    score >= 0.75f -> VibeColors.RecencyUnder24
    score >= 0.50f -> VibeColors.Recency24To48
    score >= 0.25f -> VibeColors.Recency48To72
    else -> VibeColors.DiagramMuscleNeutral
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
        drawPath(path = path, color = color)
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round),
        )
    }

    drawCurrent()

    if (side == BodySide.LEFT) {
        withTransform({
            scale(
                scaleX = -1f,
                scaleY = 1f,
                pivot = Offset(centerX, 0f),
            )
        }) {
            drawCurrent()
        }
    }
}
