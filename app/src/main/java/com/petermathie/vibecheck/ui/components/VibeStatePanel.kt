package com.petermathie.vibecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import com.petermathie.vibecheck.ui.theme.VibeShapes
import com.petermathie.vibecheck.ui.theme.VibeSpacing

@Composable
fun VibeStatePanel(message: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    val palette = LocalVibePalette.current
    Column(modifier.fillMaxWidth().padding(VibeSpacing.medium), verticalArrangement = Arrangement.spacedBy(VibeSpacing.small)) {
        Text(
            if (isError) "Unable to load" else "No data yet",
            color = if (isError) palette.danger else palette.textPrimary,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(message, color = palette.textSecondary)
    }
}

@Composable
fun VibeSkeleton(description: String, modifier: Modifier = Modifier) {
    val palette = LocalVibePalette.current
    Box(
        modifier
            .semantics { contentDescription = description }
            .background(palette.surfaceRaised, androidx.compose.foundation.shape.RoundedCornerShape(VibeShapes.control)),
    )
}
