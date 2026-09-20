package com.petermathie.vibetrainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The single source of visual truth. Screens consume semantic tokens from [LocalVibePalette]
 * and never embed colour literals. Adding a palette is one data object plus a registry entry.
 */
@Immutable
data class VibePalette(
    val id: String,
    val displayName: String,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSelected: Color,
    val accent: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textFaint: Color,
    val border: Color,
    val diagramBackground: Color,
    val diagramBody: Color,
    val diagramLine: Color,
    val recencyUnder24: Color,
    val recency24To48: Color,
    val recency48To72: Color,
    val recency3To7: Color,
    val recencyOver7: Color,
    val recencyNever: Color,
    val heatmapNeutral: Color,
    val heatmapOne: Color,
    val heatmapTwo: Color,
    val heatmapThreePlus: Color,
    val danger: Color,
)

object VibePalettes {
    val MidnightLime = VibePalette(
        id = "midnight-lime",
        displayName = "Midnight Lime",
        background = Color(0xFF081017),
        surface = Color(0xFF101B23),
        surfaceRaised = Color(0xFF172630),
        surfaceSelected = Color(0xFF24343E),
        accent = Color(0xFFC2F85A),
        onAccent = Color(0xFF081007),
        textPrimary = Color(0xFFF1F5F3),
        textSecondary = Color(0xFFA8B5BC),
        textFaint = Color(0xFF74838B),
        border = Color(0xFF31434D),
        diagramBackground = Color(0xFFF6F7F3),
        diagramBody = Color(0xFFE5E9E5),
        diagramLine = Color(0xFF8F9A96),
        recencyUnder24 = Color(0xFFF0444F),
        recency24To48 = Color(0xFFF68B42),
        recency48To72 = Color(0xFFF4CA45),
        recency3To7 = Color(0xFF5CBF72),
        recencyOver7 = Color(0xFF5597D1),
        recencyNever = Color(0xFFD2D8D4),
        heatmapNeutral = Color(0xFF24323A),
        heatmapOne = Color(0xFF8CCF91),
        heatmapTwo = Color(0xFF3E9B5B),
        heatmapThreePlus = Color(0xFF126B37),
        danger = Color(0xFFFF6670),
    )

    val GraphiteCoral = MidnightLime.copy(
        id = "graphite-coral",
        displayName = "Graphite Coral",
        accent = Color(0xFFFF766F),
        onAccent = Color(0xFF1A0908),
        surface = Color(0xFF18191D),
        surfaceRaised = Color(0xFF22242A),
        surfaceSelected = Color(0xFF30333A),
        border = Color(0xFF3C3F47),
    )

    val builtIns: Map<String, VibePalette> = listOf(MidnightLime, GraphiteCoral).associateBy { it.id }

    /** Public factory used by future user-authored palettes and design experiments. */
    fun custom(id: String, name: String, base: VibePalette = MidnightLime, transform: VibePalette.() -> VibePalette): VibePalette =
        base.copy(id = id, displayName = name).transform()
}

val LocalVibePalette = staticCompositionLocalOf { VibePalettes.MidnightLime }

object VibeSpacing {
    val xSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val xLarge = 32.dp
}

object VibeShapes {
    val card = 22.dp
    val control = 14.dp
}

private val VibeTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
)

@Composable
fun VibeTrainerTheme(
    palette: VibePalette = VibePalettes.MidnightLime,
    content: @Composable () -> Unit,
) {
    val scheme = darkColorScheme(
        primary = palette.accent,
        onPrimary = palette.onAccent,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceRaised,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.border,
        error = palette.danger,
    )
    androidx.compose.runtime.CompositionLocalProvider(LocalVibePalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = VibeTypography, content = content)
    }
}
