package com.petermathie.vibecheck.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal data class CompletionAnimationPlan(
    val muscleStaggerMillis: Long,
    val highlightHoldMillis: Long,
    val showConfetti: Boolean,
)

internal fun completionAnimationPlan(reducedMotion: Boolean): CompletionAnimationPlan =
    if (reducedMotion) CompletionAnimationPlan(0, 240, false)
    else CompletionAnimationPlan(55, 420, true)

@Composable
fun CompletionConfetti(
    eventId: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalVibePalette.current
    val progress = remember(eventId) { Animatable(0f) }
    val colors = remember(palette) {
        listOf(palette.accent, palette.secondary, palette.tertiary, palette.danger)
    }
    LaunchedEffect(eventId) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 650, easing = LinearEasing))
    }
    Canvas(modifier.clearAndSetSemantics { }) {
        repeat(18) { index ->
            val angle = (index * 137.5) * PI / 180.0
            val distance = size.minDimension * (0.08f + 0.42f * progress.value)
            val centre = Offset(
                x = size.width / 2f + cos(angle).toFloat() * distance,
                y = size.height * 0.42f + sin(angle).toFloat() * distance + size.height * 0.18f * progress.value,
            )
            drawCircle(
                color = colors[index % colors.size].copy(alpha = 1f - progress.value),
                radius = 3f + (index % 3),
                center = centre,
            )
        }
    }
}
