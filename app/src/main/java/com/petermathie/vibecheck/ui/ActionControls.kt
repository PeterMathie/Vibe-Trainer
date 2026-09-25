package com.petermathie.vibecheck.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.isActive
import com.petermathie.vibecheck.ui.theme.LocalVibeReducedMotion
import com.petermathie.vibecheck.ui.theme.LocalVibeMotion
import com.petermathie.vibecheck.ui.theme.LocalVibePalette

enum class ActionImportance {
    PRIMARY,
    SECONDARY,
    COMPACT,
}

@Composable
fun VibeActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    importance: ActionImportance = ActionImportance.SECONDARY,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val haptics = rememberVibeHaptics()
    val reducedMotion = LocalVibeReducedMotion.current
    val motion = LocalVibeMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateFloatAsState(
        if (pressed && !reducedMotion) 1f else 0f,
        if (reducedMotion) snap() else tween(if (pressed) motion.pressInMillis else motion.pressOutMillis),
        label = "action press depth",
    )
    val buttonModifier = modifier.graphicsLayer { translationY = pressOffset.dp.toPx() }
    val click = {
        if (label == "Edit") haptics.perform(VibeHapticEvent.EDIT)
        onClick()
    }
    val content: @Composable RowScope.() -> Unit = {
        icon?.let { Icon(it, contentDescription = null) }
        Text(label)
    }
    when (importance) {
        ActionImportance.PRIMARY -> Button(click, buttonModifier, enabled, interactionSource = interactionSource, content = content)
        ActionImportance.SECONDARY -> OutlinedButton(click, buttonModifier, enabled, interactionSource = interactionSource, content = content)
        ActionImportance.COMPACT -> FilledTonalButton(click, buttonModifier, enabled, interactionSource = interactionSource, content = content)
    }
}

class ReorderState internal constructor(
    keys: List<Any>,
    private val onMove: (key: Any, fromIndex: Int, toIndex: Int) -> Unit,
) {
    private val orderedKeys = mutableStateListOf<Any>().apply { addAll(keys) }
    private var sourceKeys = keys
    private var dragSourceKeys = keys
    private var awaitingCommittedKeys = false
    private var draggingKey by mutableStateOf<Any?>(null)
    private var startIndex by mutableIntStateOf(-1)
    private var pointerY by mutableFloatStateOf(Float.NaN)
    private var grabOffsetY by mutableFloatStateOf(0f)
    private var fallbackDragDistance by mutableFloatStateOf(0f)
    private var lastMoveDirection by mutableIntStateOf(0)
    private val itemBounds = mutableMapOf<Any, ItemBounds>()
    private var geometryRevision by mutableIntStateOf(0)

    private data class ItemBounds(val top: Float, val bottom: Float) {
        val height get() = bottom - top
        val center get() = (top + bottom) / 2f
    }

    fun update(keys: List<Any>) {
        if (awaitingCommittedKeys && keys == dragSourceKeys) return
        awaitingCommittedKeys = false
        sourceKeys = keys
        if (draggingKey == null && orderedKeys.toList() != keys) {
            orderedKeys.clear()
            orderedKeys.addAll(keys)
        }
    }

    fun <T> ordered(items: List<T>, key: (T) -> Any): List<T> {
        val byKey = items.associateBy(key)
        return orderedKeys.mapNotNull(byKey::get)
    }

    fun keysSnapshot(): List<Any> = orderedKeys.toList()

    fun begin(key: Any, pointerRootY: Float = Float.NaN) {
        draggingKey = key
        dragSourceKeys = sourceKeys
        startIndex = orderedKeys.indexOf(key)
        pointerY = pointerRootY
        grabOffsetY = itemBounds[key]?.let { pointerRootY - it.top } ?: 0f
        fallbackDragDistance = 0f
        lastMoveDirection = 0
    }

    fun dragBy(delta: Float, threshold: Float): Int {
        val key = draggingKey ?: return 0
        if (pointerY.isFinite()) pointerY += delta
        fallbackDragDistance += delta
        return reorderAtPointer(key, threshold, delta.compareTo(0f))
    }

    private fun reorderAtPointer(key: Any, fallbackThreshold: Float, direction: Int): Int {
        var crossings = 0
        var index = orderedKeys.indexOf(key)
        val draggedBounds = itemBounds[key]
        if (!pointerY.isFinite() || draggedBounds == null) {
            val downThreshold = if (lastMoveDirection < 0) fallbackThreshold * 1.5f else fallbackThreshold
            val upThreshold = if (lastMoveDirection > 0) fallbackThreshold * 1.5f else fallbackThreshold
            while (fallbackDragDistance >= downThreshold && index < orderedKeys.lastIndex) {
                orderedKeys[index] = orderedKeys[index + 1].also { orderedKeys[index + 1] = key }
                fallbackDragDistance -= fallbackThreshold
                lastMoveDirection = 1
                index++
                crossings++
            }
            while (fallbackDragDistance <= -upThreshold && index > 0) {
                orderedKeys[index] = orderedKeys[index - 1].also { orderedKeys[index - 1] = key }
                fallbackDragDistance += fallbackThreshold
                lastMoveDirection = -1
                index--
                crossings++
            }
            return crossings
        }
        val draggedTop = pointerY - grabOffsetY
        val draggedBottom = draggedTop + draggedBounds.height
        while (direction > 0 && index < orderedKeys.lastIndex) {
            val next = itemBounds[orderedKeys[index + 1]]
            if (next == null) {
                if (fallbackDragDistance < fallbackThreshold) break
                orderedKeys[index] = orderedKeys[index + 1].also { orderedKeys[index + 1] = key }
                fallbackDragDistance -= fallbackThreshold
                lastMoveDirection = 1
                index++
                crossings++
                break
            }
            val deadZone = if (lastMoveDirection < 0) fallbackThreshold * 0.5f else 0f
            val crossing = next.top + (next.height / 2f).coerceAtMost(fallbackThreshold)
            val crossedByGeometry = draggedBottom > crossing + deadZone
            val crossedByTravel = fallbackDragDistance >= fallbackThreshold + deadZone
            if (!crossedByGeometry && !crossedByTravel) break
            orderedKeys[index] = orderedKeys[index + 1].also { orderedKeys[index + 1] = key }
            lastMoveDirection = 1
            fallbackDragDistance = 0f
            index++
            crossings++
        }
        while (direction < 0 && index > 0) {
            val previous = itemBounds[orderedKeys[index - 1]]
            if (previous == null) {
                if (fallbackDragDistance > -fallbackThreshold) break
                orderedKeys[index] = orderedKeys[index - 1].also { orderedKeys[index - 1] = key }
                fallbackDragDistance += fallbackThreshold
                lastMoveDirection = -1
                index--
                crossings++
                break
            }
            val deadZone = if (lastMoveDirection > 0) fallbackThreshold * 0.5f else 0f
            val crossing = previous.bottom - (previous.height / 2f).coerceAtMost(fallbackThreshold)
            val crossedByGeometry = draggedTop < crossing - deadZone
            val crossedByTravel = fallbackDragDistance <= -fallbackThreshold - deadZone
            if (!crossedByGeometry && !crossedByTravel) break
            orderedKeys[index] = orderedKeys[index - 1].also { orderedKeys[index - 1] = key }
            lastMoveDirection = -1
            fallbackDragDistance = 0f
            index--
            crossings++
        }
        return crossings
    }

    fun end(): Boolean {
        val key = draggingKey ?: return false
        val endIndex = orderedKeys.indexOf(key)
        val initialIndex = startIndex
        draggingKey = null
        pointerY = Float.NaN
        fallbackDragDistance = 0f
        startIndex = -1
        lastMoveDirection = 0
        if (initialIndex >= 0 && endIndex >= 0 && initialIndex != endIndex) {
            awaitingCommittedKeys = true
            onMove(key, initialIndex, endIndex)
            return true
        }
        return false
    }

    fun cancel() {
        draggingKey = null
        pointerY = Float.NaN
        fallbackDragDistance = 0f
        startIndex = -1
        lastMoveDirection = 0
        orderedKeys.clear()
        orderedKeys.addAll(sourceKeys)
    }

    fun accessibilityMove(key: Any, delta: Int): Boolean {
        val from = orderedKeys.indexOf(key)
        val to = (from + delta).coerceIn(0, orderedKeys.lastIndex)
        if (from < 0 || from == to) return false
        orderedKeys[from] = orderedKeys[to].also { orderedKeys[to] = key }
        dragSourceKeys = sourceKeys
        awaitingCommittedKeys = true
        onMove(key, from, to)
        return true
    }

    fun canMove(key: Any, delta: Int): Boolean {
        val index = orderedKeys.indexOf(key)
        return index >= 0 && (index + delta) in orderedKeys.indices
    }

    fun isDragging(key: Any): Boolean = draggingKey == key

    fun registerItemBounds(key: Any, top: Float, bottom: Float): Float {
        val updated = ItemBounds(top, bottom)
        val previousTop = itemBounds[key]?.top
        if (itemBounds[key] != updated) {
            itemBounds[key] = updated
            geometryRevision++
        }
        return previousTop?.minus(top) ?: 0f
    }

    fun reorderAfterLayout(fallbackThreshold: Float, direction: Int): Int {
        val key = draggingKey ?: return 0
        return reorderAtPointer(key, fallbackThreshold, direction)
    }

    fun dragOffset(key: Any): Float {
        geometryRevision
        if (draggingKey != key) return 0f
        val bounds = itemBounds[key]
        return if (pointerY.isFinite() && bounds != null) pointerY - grabOffsetY - bounds.top else fallbackDragDistance
    }

    fun pointerRootY(): Float = pointerY
}

internal class ReorderScrollContext(val listState: LazyListState) {
    var viewportTop by mutableFloatStateOf(Float.NaN)
    var viewportBottom by mutableFloatStateOf(Float.NaN)
}

internal val LocalReorderScrollContext = compositionLocalOf<ReorderScrollContext?> { null }

@Composable
internal fun rememberReorderScrollContext(listState: LazyListState): ReorderScrollContext =
    remember(listState) { ReorderScrollContext(listState) }

internal fun Modifier.reorderScrollViewport(context: ReorderScrollContext): Modifier =
    onGloballyPositioned {
        val bounds = it.boundsInRoot()
        context.viewportTop = bounds.top
        context.viewportBottom = bounds.bottom
    }

internal fun edgeAutoScrollVelocity(
    pointerY: Float,
    viewportTop: Float,
    viewportBottom: Float,
    edgeSize: Float,
    maxPixelsPerSecond: Float,
): Float {
    if (!pointerY.isFinite() || viewportBottom <= viewportTop || edgeSize <= 0f) return 0f
    val topProximity = ((viewportTop + edgeSize - pointerY) / edgeSize).coerceIn(0f, 1f)
    val bottomProximity = ((pointerY - (viewportBottom - edgeSize)) / edgeSize).coerceIn(0f, 1f)
    return (bottomProximity * bottomProximity - topProximity * topProximity) * maxPixelsPerSecond
}

@Composable
fun rememberReorderState(
    keys: List<Any>,
    onMove: (key: Any, fromIndex: Int, toIndex: Int) -> Unit,
): ReorderState {
    val state = remember { ReorderState(keys, onMove) }
    SideEffect { state.update(keys) }
    return state
}

@Composable
fun ReorderHandle(
    state: ReorderState,
    itemKey: Any,
    itemLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val threshold = with(LocalDensity.current) { 48.dp.toPx() }
    val edgeSize = with(LocalDensity.current) { 72.dp.toPx() }
    val maxScrollSpeed = with(LocalDensity.current) { 720.dp.toPx() }
    val reducedMotion = LocalVibeReducedMotion.current
    val haptics = rememberVibeHaptics()
    val scrollContext = LocalReorderScrollContext.current
    val dragging = state.isDragging(itemKey)
    BackHandler(enabled = dragging) { state.cancel() }
    var handleTop by remember { mutableFloatStateOf(Float.NaN) }
    val dragInteractions = remember { MutableInteractionSource() }
    val highlight by animateColorAsState(
        if (dragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        if (reducedMotion) snap() else tween(120),
        label = "reorder handle highlight",
    )
    val scale by animateFloatAsState(
        if (dragging) 1.015f else 1f,
        if (reducedMotion) snap() else tween(120),
        label = "reorder handle scale",
    )
    val dragState = rememberDraggableState { delta ->
        repeat(state.dragBy(delta, threshold)) { haptics.perform(VibeHapticEvent.DRAG_CROSS) }
    }
    LaunchedEffect(dragInteractions) {
        dragInteractions.interactions.collect {
            if (it is DragInteraction.Cancel) state.cancel()
        }
    }
    LaunchedEffect(dragging, scrollContext) {
        if (!dragging || scrollContext == null) return@LaunchedEffect
        var previousFrame = withFrameNanos { it }
        while (isActive && state.isDragging(itemKey)) {
            val frame = withFrameNanos { it }
            val seconds = ((frame - previousFrame) / 1_000_000_000f).coerceAtMost(0.05f)
            previousFrame = frame
            val velocity = edgeAutoScrollVelocity(
                state.pointerRootY(),
                scrollContext.viewportTop,
                scrollContext.viewportBottom,
                edgeSize,
                maxScrollSpeed,
            )
            if (velocity != 0f) {
                scrollContext.listState.scrollBy(velocity * seconds)
                withFrameNanos { }
                repeat(state.reorderAfterLayout(threshold, velocity.compareTo(0f))) {
                    haptics.perform(VibeHapticEvent.DRAG_CROSS)
                }
            }
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(48.dp, 48.dp)
            .onGloballyPositioned { handleTop = it.boundsInRoot().top }
            .semantics {
                contentDescription = "Reorder $itemLabel"
                stateDescription = if (dragging) "Dragging" else "Ready to drag"
                customActions = buildList {
                    if (state.canMove(itemKey, -1)) {
                        add(CustomAccessibilityAction("Move earlier") {
                            state.accessibilityMove(itemKey, -1).also {
                                if (it) haptics.perform(VibeHapticEvent.DRAG_DROP)
                            }
                        })
                    }
                    if (state.canMove(itemKey, 1)) {
                        add(CustomAccessibilityAction("Move later") {
                            state.accessibilityMove(itemKey, 1).also {
                                if (it) haptics.perform(VibeHapticEvent.DRAG_DROP)
                            }
                        })
                    }
                }
            }
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                enabled = enabled,
                interactionSource = dragInteractions,
                onDragStarted = {
                    state.begin(itemKey, handleTop + it.y)
                    haptics.perform(VibeHapticEvent.DRAG_START)
                },
                onDragStopped = {
                    if (state.end()) haptics.perform(VibeHapticEvent.DRAG_DROP)
                },
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(highlight),
        ) {
            Icon(
                Icons.Outlined.DragIndicator,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun Modifier.reorderItemFeedback(
    state: ReorderState,
    itemKey: Any,
    index: Int,
): Modifier {
    val reducedMotion = LocalVibeReducedMotion.current
    val palette = LocalVibePalette.current
    val placementOffset = remember(itemKey) { Animatable(0f) }
    var placementRequest by remember(itemKey) { mutableFloatStateOf(0f) }
    LaunchedEffect(placementRequest, reducedMotion) {
        if (placementRequest != 0f && !state.isDragging(itemKey) && !reducedMotion) {
            placementOffset.snapTo(placementRequest)
            placementOffset.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
        } else {
            placementOffset.snapTo(0f)
        }
    }
    val dragging = state.isDragging(itemKey)
    val background by animateColorAsState(
        if (dragging) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
        if (reducedMotion) snap() else tween(120),
        label = "dragged item background",
    )
    return this
        .onGloballyPositioned {
            val bounds = it.boundsInRoot()
            val displacement = state.registerItemBounds(itemKey, bounds.top, bounds.bottom)
            if (displacement != 0f && !state.isDragging(itemKey)) placementRequest = displacement
        }
        .zIndex(if (dragging) 1f else 0f)
        .graphicsLayer {
            translationY = state.dragOffset(itemKey) + placementOffset.value
            scaleX = if (dragging && !reducedMotion) 1.015f else 1f
            scaleY = if (dragging && !reducedMotion) 1.015f else 1f
            shadowElevation = if (dragging) 8.dp.toPx() else 0f
            shape = RoundedCornerShape(12.dp)
            ambientShadowColor = Color.Black.copy(alpha = if (palette.isDark) 0.24f else 0.20f)
            spotShadowColor = Color.Black.copy(alpha = if (palette.isDark) 0.36f else 0.28f)
        }
        .clip(RoundedCornerShape(12.dp))
        .background(background)
        .then(
            if (dragging) {
                Modifier
                    .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .drawBehind {
                        val y = if (state.dragOffset(itemKey) >= 0f) size.height else 0f
                        drawLine(palette.accent, Offset(6.dp.toPx(), y), Offset(size.width - 6.dp.toPx(), y), 2.dp.toPx())
                    }
            } else {
                Modifier
            },
        )
}
