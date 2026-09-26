package com.petermathie.vibecheck.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.petermathie.vibecheck.ui.theme.LocalVibePalette

@Composable
fun TechnicalBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalVibePalette.current
    Box(
        modifier.drawWithCache {
            val spacing = 32.dp.toPx()
            val stroke = 1f
            val grid = palette.textPrimary.copy(alpha = if (palette.isDark) 0.025f else 0.02f)
            onDrawBehind {
                var x = 0f
                while (x <= size.width) {
                    drawLine(grid, Offset(x, 0f), Offset(x, size.height), stroke)
                    x += spacing
                }
                var y = 0f
                while (y <= size.height) {
                    drawLine(grid, Offset(0f, y), Offset(size.width, y), stroke)
                    y += spacing
                }
            }
        },
        content = content,
    )
}
