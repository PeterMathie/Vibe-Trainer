package com.petermathie.vibetrainer.ui

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
internal fun HoldTimerButton(
    nowMillis: () -> Long = SystemClock::elapsedRealtime,
    tickMillis: Long = 100,
    onStopped: (String) -> Unit,
) {
    var timerStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var elapsed by rememberSaveable { mutableStateOf(0L) }
    LaunchedEffect(timerStart) {
        while (timerStart != null) {
            elapsed = nowMillis() - timerStart!!
            delay(tickMillis)
        }
    }
    VibeActionButton(
        label = if (timerStart == null) "Start hold timer" else "Stop · ${elapsed / 1000.0}s",
        importance = ActionImportance.COMPACT,
        onClick = {
            val startedAt = timerStart
            if (startedAt == null) {
                timerStart = nowMillis()
            } else {
                val seconds = (nowMillis() - startedAt).coerceAtLeast(0) / 1000.0
                elapsed = (seconds * 1000).toLong()
                timerStart = null
                onStopped(seconds.toString())
            }
        },
    )
}
