package com.jintonic.app.ui.test_flow

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.Constants
import com.jintonic.app.pvt.PvtTrial
import com.jintonic.app.ui.components.TopLabel
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import com.jintonic.app.ui.theme.FfdType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

private const val MS_PER_SEC = 1_000L
private const val SEC_PER_MIN = 60
private const val CLOCK_TICK_MS = 250L
private const val GLOW_ALPHA = 0.55f

@Composable
fun PvtScreen(vm: TestFlowViewModel, onFinished: () -> Unit) {
    var stimulusVisible by remember { mutableStateOf(false) }
    var remainingMs by remember { mutableLongStateOf(vm.mode.durationSec * MS_PER_SEC) }
    val taps = remember { Channel<Long>(Channel.CONFLATED) }
    KeepScreenOn()

    val endAt = remember { SystemClock.uptimeMillis() + vm.mode.durationSec * MS_PER_SEC }
    LaunchedEffect(endAt) {
        while (remainingMs > 0) {
            remainingMs = (endAt - SystemClock.uptimeMillis()).coerceAtLeast(0)
            delay(CLOCK_TICK_MS)
        }
    }
    LaunchedEffect(Unit) {
        val trials = runPvt(
            durationMs = vm.mode.durationSec * MS_PER_SEC,
            nextDelay = vm::nextStimulusDelayMs,
            taps = taps,
            onStimulus = { stimulusVisible = it },
        )
        vm.finish(trials)
        onFinished()
    }

    ScreenScaffold(timeText = { TopLabel("Test · ${formatClock(remainingMs)}") }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val down = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                            if (down != null) taps.trySend(down.uptimeMillis)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (stimulusVisible) Stimulus()
            Text(
                text = "tocca appena appare",
                fontSize = FfdType.BottomLabel,
                color = FfdColors.LabelBottom,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            )
        }
    }
}

// Mockup ".dot": 70dp green disc with a soft green glow.
@Composable
private fun Stimulus() {
    Box(
        modifier = Modifier
            .size(FfdSizes.StimulusDiameter)
            .drawBehind {
                val core = size.minDimension / 2
                val halo = core + FfdSizes.StimulusGlow.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to FfdColors.Primary.copy(alpha = GLOW_ALPHA),
                        core / halo to FfdColors.Primary.copy(alpha = GLOW_ALPHA),
                        1f to Color.Transparent,
                        radius = halo,
                    ),
                    radius = halo,
                )
            }
            .background(FfdColors.Primary, CircleShape),
    )
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

// Runs the whole session on the monotonic clock (SystemClock.uptimeMillis = pointer event timebase).
private suspend fun runPvt(
    durationMs: Long,
    nextDelay: () -> Long,
    taps: Channel<Long>,
    onStimulus: (Boolean) -> Unit,
): List<PvtTrial> {
    val trials = mutableListOf<PvtTrial>()
    val endAt = SystemClock.uptimeMillis() + durationMs
    fun remaining() = (endAt - SystemClock.uptimeMillis()).coerceAtLeast(0)
    while (remaining() > 0) {
        // Waiting phase: any tap here is a false start (anticipation).
        val early = withTimeoutOrNull(minOf(nextDelay(), remaining())) { taps.receive() }
        if (early != null) {
            trials += PvtTrial(stimulusAtMs = early, responseAtMs = early, falseStart = true)
        } else if (remaining() > 0) {
            trials += presentStimulus(taps, onStimulus)
        }
    }
    return trials
}

// Stamp after the frame showing the stimulus has been produced, not when it was requested.
private suspend fun presentStimulus(taps: Channel<Long>, onStimulus: (Boolean) -> Unit): PvtTrial {
    onStimulus(true)
    withFrameNanos { }
    withFrameNanos { }
    val shownAt = SystemClock.uptimeMillis()
    val response = withTimeoutOrNull(Constants.TIMEOUT_MS) { taps.receive() }
    onStimulus(false)
    return PvtTrial(stimulusAtMs = shownAt, responseAtMs = response)
}

private fun formatClock(ms: Long): String {
    val totalSec = (ms + MS_PER_SEC - 1) / MS_PER_SEC
    return "%d:%02d".format(totalSec / SEC_PER_MIN, totalSec % SEC_PER_MIN)
}
