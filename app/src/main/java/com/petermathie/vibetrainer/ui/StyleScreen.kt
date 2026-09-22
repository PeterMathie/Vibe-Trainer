package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.petermathie.vibetrainer.domain.style.PaletteContrast
import com.petermathie.vibetrainer.ui.theme.LocalVibePalette
import com.petermathie.vibetrainer.ui.theme.VibePalette
import com.petermathie.vibetrainer.ui.theme.VibePalettes
import com.petermathie.vibetrainer.ui.theme.VibeShapes

@Composable
internal fun StyleScreen(selectedId: String, onSelect: (String) -> Unit, onRemoveDemo: () -> Unit) {
    val prefs = LocalContext.current.getSharedPreferences("settings", 0)
    fun storedHex(key: String, fallback: Int) = String.format("#%06X", 0xFFFFFF and prefs.getInt(key, fallback))
    var accent by remember { mutableStateOf(storedHex("accent", 0xFFC2F85A.toInt())) }
    var background by remember { mutableStateOf(storedHex("background", 0xFF081017.toInt())) }
    var surface by remember { mutableStateOf(storedHex("surface", 0xFF101B23.toInt())) }
    var error by remember { mutableStateOf<String?>(null) }
    var applied by remember { mutableStateOf(false) }
    ScreenList {
        item {
            Text("Style", style = MaterialTheme.typography.headlineLarge)
            Text("Every screen uses semantic design tokens. New palettes require no screen changes.", color = LocalVibePalette.current.textSecondary)
        }
        items(VibePalettes.builtIns.values.toList(), key = { it.id }) { palette ->
            PaletteCard(palette, selectedId == palette.id) { onSelect(palette.id) }
        }
        item {
            VibeCard {
                Text("Custom palette")
                EditField("Accent hex", accent) { accent = it }
                EditField("Background hex", background) { background = it }
                EditField("Surface hex", surface) { surface = it }
                Button(onClick = {
                    try {
                        val accentValue = android.graphics.Color.parseColor(accent)
                        val backgroundValue = android.graphics.Color.parseColor(background)
                        val surfaceValue = android.graphics.Color.parseColor(surface)
                        error = PaletteContrast.customPaletteError(accentValue, backgroundValue, surfaceValue)
                        if (error == null) {
                            prefs.edit()
                                .putInt("accent", accentValue)
                                .putInt("background", backgroundValue)
                                .putInt("surface", surfaceValue)
                                .apply()
                            onSelect("custom")
                            applied = true
                        }
                    } catch (_: IllegalArgumentException) {
                        error = "Use valid hex colours, for example #C2F85A"
                    }
                }) { Text("Apply custom palette") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (applied && error == null) Text("Custom palette applied")
            }
        }
        item {
            VibeCard {
                Text("Development data", style = MaterialTheme.typography.titleLarge)
                Text("Remove demo workouts, programmes and trackers while keeping the exercise database.", color = LocalVibePalette.current.textSecondary)
                OutlinedButton(onClick = onRemoveDemo, modifier = Modifier.fillMaxWidth()) { Text("Remove demo data") }
            }
        }
    }
}

@Composable
private fun PaletteCard(palette: VibePalette, selected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = LocalVibePalette.current.surface),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LocalVibePalette.current.accent else LocalVibePalette.current.border),
        shape = RoundedCornerShape(VibeShapes.card),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            listOf(palette.accent, palette.recencyUnder24, palette.heatmapThreePlus).forEach { color ->
                Box(Modifier.size(28.dp).background(color, RoundedCornerShape(50)))
            }
            Text(palette.displayName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            if (selected) Icon(Icons.Outlined.Check, "Selected")
        }
    }
}
