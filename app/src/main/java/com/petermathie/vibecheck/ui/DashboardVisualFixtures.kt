package com.petermathie.vibecheck.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import com.petermathie.vibecheck.ui.theme.VibeCheckTheme
import com.petermathie.vibecheck.ui.theme.VibePalette
import com.petermathie.vibecheck.ui.theme.VibePalettes
import com.petermathie.vibecheck.ui.theme.VibeSpacing

internal object DashboardVisualFixtures {
    val palettes: List<VibePalette> = VibePalettes.presets.flatMap { listOf(it.light, it.dark) }
    const val metric = "72.4"
    const val annotation = "kg · 25 Sep 2026"
}

@Composable
internal fun DashboardTokenPreview(
    palette: VibePalette,
    modifier: Modifier = Modifier,
) {
    VibeCheckTheme(palette) {
        Column(
            modifier
                .background(palette.background)
                .padding(VibeSpacing.medium)
                .semantics {
                    contentDescription = "${palette.displayName} ${if (palette.isDark) "dark" else "light"} dashboard tokens"
                },
            verticalArrangement = Arrangement.spacedBy(VibeSpacing.medium),
        ) {
            Text("FRESHNESS", color = palette.accent, style = MaterialTheme.typography.labelLarge)
            VibeCard {
                Text(DashboardVisualFixtures.metric, style = MaterialTheme.typography.headlineLarge)
                Text(DashboardVisualFixtures.annotation, color = palette.textSecondary)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VibeSpacing.small),
                ) {
                    listOf(
                        palette.recencyUnder24,
                        palette.recency24To48,
                        palette.recency48To72,
                        palette.recency3To7,
                        palette.recencyOver7,
                        palette.recencyNever,
                    ).forEach { colour ->
                        androidx.compose.foundation.layout.Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colour),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Mono dark", widthDp = 411, heightDp = 300)
@Composable
private fun MonoDarkDashboardTokenPreview() {
    DashboardTokenPreview(VibePalettes.Mono.dark)
}

@Preview(name = "Mono light", widthDp = 411, heightDp = 300)
@Composable
private fun MonoLightDashboardTokenPreview() {
    DashboardTokenPreview(VibePalettes.Mono.light)
}
