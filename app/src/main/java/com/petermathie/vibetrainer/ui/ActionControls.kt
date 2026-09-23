package com.petermathie.vibetrainer.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

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
    val content: @Composable RowScope.() -> Unit = {
        icon?.let { Icon(it, contentDescription = null) }
        Text(label)
    }
    when (importance) {
        ActionImportance.PRIMARY -> Button(onClick, modifier, enabled, content = content)
        ActionImportance.SECONDARY -> OutlinedButton(onClick, modifier, enabled, content = content)
        ActionImportance.COMPACT -> FilledTonalButton(onClick, modifier, enabled, content = content)
    }
}

class ReorderState internal constructor(
    keys: List<Any>,
    private val onMove: (key: Any, fromIndex: Int, toIndex: Int) -> Unit,
) {
    private val orderedKeys = mutableStateListOf<Any>().apply { addAll(keys) }
    private var sourceKeys = keys
    private var draggingKey by mutableStateOf<Any?>(null)
    private var startIndex by mutableIntStateOf(-1)
    private var dragDistance by mutableFloatStateOf(0f)

    fun update(keys: List<Any>) {
        sourceKeys = keys
        if (draggingKey == null && orderedKeys != keys) {
            orderedKeys.clear()
            orderedKeys.addAll(keys)
        }
    }

    fun <T> ordered(items: List<T>, key: (T) -> Any): List<T> {
        val byKey = items.associateBy(key)
        return orderedKeys.mapNotNull(byKey::get)
    }

    fun begin(key: Any) {
        draggingKey = key
        startIndex = orderedKeys.indexOf(key)
        dragDistance = 0f
    }

    fun dragBy(delta: Float, threshold: Float) {
        val key = draggingKey ?: return
        dragDistance += delta
        var index = orderedKeys.indexOf(key)
        while (dragDistance >= threshold && index < orderedKeys.lastIndex) {
            orderedKeys[index] = orderedKeys[index + 1].also { orderedKeys[index + 1] = key }
            dragDistance -= threshold
            index++
        }
        while (dragDistance <= -threshold && index > 0) {
            orderedKeys[index] = orderedKeys[index - 1].also { orderedKeys[index - 1] = key }
            dragDistance += threshold
            index--
        }
    }

    fun end() {
        val key = draggingKey ?: return
        val endIndex = orderedKeys.indexOf(key)
        val initialIndex = startIndex
        draggingKey = null
        dragDistance = 0f
        startIndex = -1
        if (initialIndex >= 0 && endIndex >= 0 && initialIndex != endIndex) {
            onMove(key, initialIndex, endIndex)
        }
    }

    fun cancel() {
        draggingKey = null
        dragDistance = 0f
        startIndex = -1
        orderedKeys.clear()
        orderedKeys.addAll(sourceKeys)
    }

    fun accessibilityMove(key: Any, delta: Int): Boolean {
        val from = orderedKeys.indexOf(key)
        val to = (from + delta).coerceIn(0, orderedKeys.lastIndex)
        if (from < 0 || from == to) return false
        orderedKeys[from] = orderedKeys[to].also { orderedKeys[to] = key }
        onMove(key, from, to)
        return true
    }

    fun canMove(key: Any, delta: Int): Boolean {
        val index = orderedKeys.indexOf(key)
        return index >= 0 && (index + delta) in orderedKeys.indices
    }
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(48.dp, 48.dp)
            .semantics {
                contentDescription = "Reorder $itemLabel"
                customActions = buildList {
                    if (state.canMove(itemKey, -1)) {
                        add(CustomAccessibilityAction("Move earlier") { state.accessibilityMove(itemKey, -1) })
                    }
                    if (state.canMove(itemKey, 1)) {
                        add(CustomAccessibilityAction("Move later") { state.accessibilityMove(itemKey, 1) })
                    }
                }
            }
            .pointerInput(state, itemKey, enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { state.begin(itemKey) },
                    onDragEnd = state::end,
                    onDragCancel = state::cancel,
                    onDrag = { change, dragAmount: Offset ->
                        change.consume()
                        state.dragBy(dragAmount.y, threshold)
                    },
                )
            },
    ) {
        Icon(Icons.Outlined.DragIndicator, contentDescription = null)
    }
}
