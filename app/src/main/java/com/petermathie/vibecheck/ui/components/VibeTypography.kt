package com.petermathie.vibecheck.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import com.petermathie.vibecheck.ui.theme.VibeDashboardTypography

@Composable
fun VibeMetric(
    value: String,
    modifier: Modifier = Modifier,
    accessibleValue: String = value,
    compact: Boolean = false,
) {
    Text(
        value,
        modifier.clearAndSetSemantics { contentDescription = accessibleValue },
        style = if (compact) VibeDashboardTypography.metricCompact else VibeDashboardTypography.metric,
    )
}

@Composable
fun VibeMicroLabel(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = LocalVibePalette.current.textSecondary,
) {
    val rendered = if (label.length <= 18) label.uppercase() else label
    Text(rendered, modifier, color = color, style = VibeDashboardTypography.microLabel, overflow = TextOverflow.Visible)
}
