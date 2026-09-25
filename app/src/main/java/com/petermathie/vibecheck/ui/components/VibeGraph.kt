package com.petermathie.vibecheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.petermathie.vibecheck.ui.theme.LocalVibePalette
import com.petermathie.vibecheck.ui.theme.VibeDashboardTypography
import com.petermathie.vibecheck.ui.theme.VibeSpacing
import com.petermathie.vibecheck.ui.theme.VibeSurfaceLevel
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

enum class VibeGraphStyle { LINE, BARS }

sealed interface VibeGraphState {
    data object Data : VibeGraphState
    data object Loading : VibeGraphState
    data class Empty(val message: String) : VibeGraphState
    data class Error(val message: String) : VibeGraphState
}

internal data class GraphDomain(val minimum: Double, val maximum: Double) {
    val span: Double get() = (maximum - minimum).coerceAtLeast(1.0)
}

internal fun graphDomain(values: List<Double>, fixedRange: ClosedFloatingPointRange<Double>? = null): GraphDomain? {
    fixedRange?.let { return GraphDomain(it.start, it.endInclusive) }
    val finite = values.filter(Double::isFinite)
    if (finite.isEmpty()) return null
    return GraphDomain(finite.minOrNull() ?: 0.0, finite.maxOrNull() ?: 1.0)
}

internal fun nearestGraphIndex(x: Float, width: Float, count: Int): Int {
    if (count <= 1 || width <= 0f) return 0
    return ((x / width) * (count - 1)).roundToInt().coerceIn(0, count - 1)
}

internal fun graphPoint(
    index: Int,
    value: Double,
    count: Int,
    width: Float,
    height: Float,
    domain: GraphDomain,
    padding: Float,
): Offset {
    val plotWidth = (width - padding * 2f).coerceAtLeast(1f)
    val plotHeight = (height - padding * 2f).coerceAtLeast(1f)
    return Offset(
        x = if (count <= 1) width / 2f else padding + plotWidth * index / (count - 1),
        y = height - padding - ((value - domain.minimum) / domain.span * plotHeight).toFloat(),
    )
}

@Composable
fun VibeGraph(
    values: List<Double>,
    dates: List<Long>,
    unit: String,
    modifier: Modifier = Modifier,
    secondaryValues: List<Double?> = emptyList(),
    style: VibeGraphStyle = VibeGraphStyle.LINE,
    graphState: VibeGraphState = if (values.any(Double::isFinite)) VibeGraphState.Data else VibeGraphState.Empty("Complete a valid entry to see this chart."),
    fixedRange: ClosedFloatingPointRange<Double>? = null,
    onSelect: (Int) -> Unit = {},
) {
    val palette = LocalVibePalette.current
    val domain = remember(values, fixedRange) { graphDomain(values, fixedRange) }
    val graphPaddingPx = with(LocalDensity.current) { 12.dp.toPx() }
    var selectedIndex by remember(values) { mutableIntStateOf(-1) }
    val select: (Float, Float) -> Unit = { x, width ->
        if (values.isNotEmpty()) {
            val plotX = (x - graphPaddingPx).coerceAtLeast(0f)
            val plotWidth = (width - graphPaddingPx * 2f).coerceAtLeast(1f)
            (if (style == VibeGraphStyle.BARS) {
                (plotX / plotWidth * values.size).toInt().coerceIn(0, values.lastIndex)
            } else {
                nearestGraphIndex(plotX, plotWidth, values.size)
            })
                .let { candidate ->
                    if (values[candidate].isFinite()) candidate
                    else values.indices.minByOrNull { kotlin.math.abs(it - candidate) + if (values[it].isFinite()) 0 else values.size } ?: candidate
                }
                .also {
                    selectedIndex = it
                    onSelect(it)
                }
        }
    }
    val description = domain?.let {
        val minimum = if (fixedRange == null) it.minimum.toString() else formatGraphAxis(it.minimum)
        val maximum = if (fixedRange == null) it.maximum.toString() else formatGraphAxis(it.maximum)
        "Progress chart from ${formatGraphDate(dates.firstOrNull())} to ${formatGraphDate(dates.lastOrNull())}, $minimum to $maximum $unit"
    } ?: "Progress chart, no data"

    VibeSurface(VibeSurfaceLevel.INSET, modifier = modifier.fillMaxWidth()) {
        when (graphState) {
            VibeGraphState.Loading -> VibeSkeleton("Loading chart", Modifier.fillMaxWidth().height(178.dp))
            is VibeGraphState.Empty -> VibeStatePanel(graphState.message)
            is VibeGraphState.Error -> VibeStatePanel(graphState.message, isError = true)
            VibeGraphState.Data -> if (domain != null) {
                Column(Modifier.fillMaxWidth().padding(VibeSpacing.compact)) {
                    Text("${formatGraphAxis(domain.maximum)} $unit", style = VibeDashboardTypography.annotation)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(138.dp)
                            .pointerInput(values) {
                                detectTapGestures { select(it.x, size.width.toFloat()) }
                            }
                            .pointerInput(values) {
                                detectDragGestures { change, _ ->
                                    select(change.position.x, size.width.toFloat())
                                }
                            }
                            .semantics {
                                contentDescription = description
                                selectedIndex.takeIf { it in values.indices }?.let {
                                    stateDescription = "${formatGraphDate(dates.getOrNull(it))}, ${formatGraphAxis(values[it])} $unit"
                                }
                                customActions = listOf(
                                    CustomAccessibilityAction("Previous data point") {
                                        val start = selectedIndex.takeIf { it in values.indices } ?: values.size
                                        val previous = (start - 1 downTo 0).firstOrNull { values[it].isFinite() }
                                        if (previous == null) false else {
                                            selectedIndex = previous
                                            onSelect(selectedIndex)
                                            true
                                        }
                                    },
                                    CustomAccessibilityAction("Next data point") {
                                        val next = ((selectedIndex + 1).coerceAtLeast(0)..values.lastIndex)
                                            .firstOrNull { values[it].isFinite() }
                                        if (next == null) false else {
                                            selectedIndex = next
                                            onSelect(selectedIndex)
                                            true
                                        }
                                    },
                                )
                            },
                    ) {
                        val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) }
                        Canvas(
                            Modifier
                                .matchParentSize()
                                .drawWithCache {
                                    val padding = 12.dp.toPx()
                                    val major = palette.textPrimary.copy(alpha = if (palette.isDark) 0.09f else 0.12f)
                                    val primaryPath = Path()
                                    var primaryStarted = false
                                    val primaryPoints = values.mapIndexedNotNull { index, value ->
                                        value.takeIf(Double::isFinite)?.let {
                                            graphPoint(index, it, values.size, size.width, size.height, domain, padding)
                                        }
                                    }
                                    values.forEachIndexed { index, value ->
                                        if (!value.isFinite()) {
                                            primaryStarted = false
                                        } else {
                                            val point = graphPoint(index, value, values.size, size.width, size.height, domain, padding)
                                            if (primaryStarted) primaryPath.lineTo(point.x, point.y) else primaryPath.moveTo(point.x, point.y)
                                            primaryStarted = true
                                        }
                                    }
                                    val secondarySegments = secondaryValues.mapIndexedNotNull { index, value ->
                                        val previous = secondaryValues.getOrNull(index - 1)
                                        if (value != null && value.isFinite() && previous != null && previous.isFinite()) {
                                            graphPoint(index - 1, previous, values.size, size.width, size.height, domain, padding) to
                                                graphPoint(index, value, values.size, size.width, size.height, domain, padding)
                                        } else null
                                    }
                                    onDrawBehind {
                                        repeat(5) { row ->
                                            val y = padding + (size.height - padding * 2f) * row / 4f
                                            drawLine(major, Offset(padding, y), Offset(size.width - padding, y), 1.dp.toPx())
                                        }
                                        repeat(4) { column ->
                                            val x = padding + (size.width - padding * 2f) * column / 3f
                                            drawLine(major, Offset(x, padding), Offset(x, size.height - padding), 1.dp.toPx())
                                        }
                                        if (style == VibeGraphStyle.LINE) {
                                            drawPath(primaryPath, palette.accent, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                                            primaryPoints.forEach { drawCircle(palette.accent, 2.5.dp.toPx(), it) }
                                        } else {
                                            val slot = (size.width - padding * 2f) / values.size.coerceAtLeast(1)
                                            values.forEachIndexed { index, value ->
                                                if (value.isFinite()) {
                                                    val point = graphPoint(index, value, values.size, size.width, size.height, domain, padding)
                                                        .copy(x = padding + slot * (index + 0.5f))
                                                    drawRect(
                                                        palette.accent,
                                                        topLeft = Offset(point.x - slot * 0.3f, point.y),
                                                        size = androidx.compose.ui.geometry.Size(slot * 0.6f, size.height - padding - point.y),
                                                    )
                                                }
                                            }
                                        }
                                        secondarySegments.forEach { (from, to) ->
                                            drawLine(palette.secondary, from, to, 2.5.dp.toPx(), pathEffect = dashEffect)
                                        }
                                    }
                                }
                                ,
                        ) {
                            val index = selectedIndex
                            if (index in values.indices && values[index].isFinite()) {
                                val padding = 12.dp.toPx()
                                val linePoint = graphPoint(index, values[index], values.size, size.width, size.height, domain, padding)
                                val point = if (style == VibeGraphStyle.BARS) {
                                    val slot = (size.width - padding * 2f) / values.size.coerceAtLeast(1)
                                    linePoint.copy(x = padding + slot * (index + 0.5f))
                                } else {
                                    linePoint
                                }
                                drawCircle(palette.accent, 3.5.dp.toPx(), point)
                                drawCircle(palette.surfaceInset, 1.dp.toPx(), point)
                                drawLine(
                                    palette.textSecondary.copy(alpha = 0.6f),
                                    Offset(point.x, padding),
                                    Offset(point.x, size.height - padding),
                                    1.dp.toPx(),
                                    pathEffect = dashEffect,
                                )
                            }
                        }
                        selectedIndex.takeIf { it in values.indices && values[it].isFinite() }?.let { index ->
                            VibeGraphTooltip(
                                value = "${formatGraphAxis(values[index])} $unit",
                                date = formatGraphDate(dates.getOrNull(index)),
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text(formatGraphDate(dates.firstOrNull()), style = VibeDashboardTypography.annotation, modifier = Modifier.weight(1f))
                        Text(formatGraphDate(dates.lastOrNull()), style = VibeDashboardTypography.annotation, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                    Text("${formatGraphAxis(domain.minimum)} $unit", style = VibeDashboardTypography.annotation)
                }
            }
        }
    }
}

@Composable
fun VibeGraphTooltip(value: String, date: String, modifier: Modifier = Modifier) {
    VibeSurface(VibeSurfaceLevel.FLOATING, modifier.widthIn(max = 180.dp)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(value, style = VibeDashboardTypography.metricCompact)
            Text(date, color = LocalVibePalette.current.textSecondary, style = VibeDashboardTypography.annotation)
        }
    }
}

internal fun formatGraphDate(value: Long?): String = value?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString()
}.orEmpty()

internal fun formatGraphAxis(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else "%.1f".format(value)
