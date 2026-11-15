package com.jintonic.app.ui.test_flow

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.Constants
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.pvt.PvtTrial
import com.jintonic.app.ui.theme.AppColors
import com.jintonic.app.ui.theme.AppSizes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

private const val MS_PER_SEC = 1_000L
private const val SEC_PER_MIN = 60
private const val CLOCK_TICK_MS = 250L
private const val PULSE_MIN = 0.86f
private const val PULSE_MAX = 1.1f
private const val PULSE_MS = 850

@Composable
fun PvtScreen(vm: TestFlowViewModel, onFinished: () -> Unit) {
    var stimulusVisible by remember { mutableStateOf(false) }
    var remainingMs by remember { mutableLongStateOf(vm.mode.durationSec * MS_PER_SEC) }
    val taps = remember { Channel<Long>(Channel.CONFLATED) }
    val haptic = LocalHapticFeedback.current
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

    val tapModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // Only a tap on a visible ball buzzes; taps in the empty wait stay silent.
            if (stimulusVisible) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            taps.trySend(down.uptimeMillis)
        }
    }
    PvtContent(clockText = formatClock(remainingMs), stimulusVisible = stimulusVisible, tapModifier = tapModifier)
}

// timeText blanked so only the test countdown is shown, not the system clock.
@Composable
private fun PvtContent(clockText: String, stimulusVisible: Boolean, tapModifier: Modifier = Modifier) {
    ScreenScaffold(timeText = {}) {
        Box(
            modifier = Modifier.fillMaxSize().then(tapModifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = clockText,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextSecondary,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            )
            if (stimulusVisible) Stimulus()
        }
    }
}

// Solid ball that pulses slowly to draw the eye, with a tap glyph telling you to touch it.
@Composable
private fun Stimulus() {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = PULSE_MIN,
        targetValue = PULSE_MAX,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier.size(AppSizes.Stimulus).scale(scale).background(AppColors.Primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.TouchApp,
            contentDescription = null,
            tint = AppColors.OnBackground,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

// Runs the session on the monotonic clock shared with pointer events, so reaction times are exact.
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
        // A tap while waiting is a false start.
        val early = withTimeoutOrNull(minOf(nextDelay(), remaining())) { taps.receive() }
        if (early != null) {
            trials += PvtTrial(stimulusAtMs = early, responseAtMs = early, falseStart = true)
        } else if (remaining() > 0) {
            trials += presentStimulus(taps, onStimulus)
        }
    }
    return trials
}

// Stamps the stimulus after its frame is drawn, not when it is requested.
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

@WearPreview
@Composable
private fun PvtWaitingPreview() {
    WearScreenPreview { PvtContent(clockText = "0:08", stimulusVisible = false) }
}

@WearPreview
@Composable
private fun PvtStimulusPreview() {
    WearScreenPreview { PvtContent(clockText = "0:05", stimulusVisible = true) }
}
