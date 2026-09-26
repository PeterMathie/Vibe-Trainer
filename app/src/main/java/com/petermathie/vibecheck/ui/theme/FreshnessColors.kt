package com.petermathie.vibecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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
    val ageStops: List<Color>
        get() = listOf(recentlyTrained, recovering, intermediate, rested, fresh)

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
): Color {
    val amount = fraction.coerceIn(0f, 1f)
    if (amount == 0f) return from
    if (amount == 1f) return to
    return lerp(
        from.convert(ColorSpaces.Oklab),
        to.convert(ColorSpaces.Oklab),
        amount,
    ).convert(ColorSpaces.Srgb)
}

internal fun interpolateFreshnessBandColor(
    from: MuscleRecencyBand,
    to: MuscleRecencyBand,
    fraction: Float,
    colors: FreshnessColors,
): Color {
    if (from == MuscleRecencyBand.NEVER || to == MuscleRecencyBand.NEVER) {
        return colors.forBand(if (fraction < 0.5f) from else to)
    }
    return interpolateFreshnessColor(colors.forBand(from), colors.forBand(to), fraction)
}

@Immutable
data class HabitHeatmapColors(
    val neutral: Color,
    val low: Color,
    val medium: Color,
    val strong: Color,
) {
    fun forLevel(level: Int): Color = when (level) {
        1 -> low
        2 -> medium
        3 -> strong
        else -> neutral
    }
}

internal fun VibePalette.habitHeatmapColors(activityColor: Color? = null): HabitHeatmapColors {
    val safeActivity = activityColor?.takeIf { contrastRatio(it, surface) >= 3.0 } ?: heatmapThreePlus
    if (activityColor == null || safeActivity == heatmapThreePlus) {
        return HabitHeatmapColors(heatmapNeutral, heatmapOne, heatmapTwo, heatmapThreePlus)
    }
    return HabitHeatmapColors(
        neutral = heatmapNeutral,
        low = interpolateFreshnessColor(heatmapNeutral, safeActivity, 0.35f),
        medium = interpolateFreshnessColor(heatmapNeutral, safeActivity, 0.68f),
        strong = safeActivity,
    )
}

internal fun VibePalette.heatmapOutlineColor(fill: Color): Color =
    listOf(accent, textPrimary, background, onAccent).maxBy { contrastRatio(it, fill) }

internal fun contrastRatio(first: Color, second: Color): Double {
    val lighter = maxOf(first.luminance(), second.luminance()).toDouble()
    val darker = minOf(first.luminance(), second.luminance()).toDouble()
    return (lighter + 0.05) / (darker + 0.05)
}
