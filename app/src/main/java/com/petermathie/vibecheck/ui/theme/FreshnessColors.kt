package com.petermathie.vibecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.petermathie.vibecheck.domain.model.MuscleRecencyBand

@Immutable
data class FreshnessColors(
    val recentlyTrained: Color,
    val recovering: Color,
    val intermediate: Color,
    val rested: Color,
    val fresh: Color,
    val noData: Color,
    val selection: Color,
) {
    fun forBand(band: MuscleRecencyBand): Color = when (band) {
        MuscleRecencyBand.UNDER_24_HOURS -> recentlyTrained
        MuscleRecencyBand.HOURS_24_TO_48 -> recovering
        MuscleRecencyBand.HOURS_48_TO_72 -> intermediate
        MuscleRecencyBand.DAYS_3_TO_7 -> rested
        MuscleRecencyBand.OVER_7_DAYS -> fresh
        MuscleRecencyBand.NEVER -> noData
    }
}

fun VibePalette.freshnessColors(): FreshnessColors = FreshnessColors(
    recentlyTrained = recencyUnder24,
    recovering = recency24To48,
    intermediate = recency48To72,
    rested = recency3To7,
    fresh = recencyOver7,
    noData = recencyNever,
    selection = background,
)

internal fun interpolateFreshnessColor(
    from: Color,
    to: Color,
    fraction: Float,
): Color = lerp(from, to, fraction.coerceIn(0f, 1f))
