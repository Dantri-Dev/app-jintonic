package com.jintonic.app.presentation.test_flow

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import com.jintonic.app.R
import com.jintonic.app.core.Constants
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.presentation.components.Cookie9Shape
import com.jintonic.app.presentation.theme.AppColors
import com.jintonic.app.presentation.theme.AppSizes
import com.jintonic.app.pvt.PvtTrial
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

private const val MS_PER_SEC = 1_000L
private const val CLOCK_TICK_MS = 100L
private const val RING_START_ANGLE = 290f
private const val RING_END_ANGLE = 250f
private val RING_INSET = 4.dp
private const val PULSE_MIN = 0.9f
private const val PULSE_MAX = 1.08f
private const val PULSE_MS = 850
private const val STIMULUS_ICON_DP = 36

@Composable
fun PvtScreen(vm: TestFlowViewModel, onFinished: () -> Unit) {
    var stimulusVisible by remember { mutableStateOf(false) }
    val totalMs = vm.mode.durationSec * MS_PER_SEC
    var progress by remember { mutableFloatStateOf(0f) }
    val taps = remember { Channel<Long>(Channel.CONFLATED) }
    val haptic = LocalHapticFeedback.current
    KeepScreenOn()

    val endAt = remember { SystemClock.uptimeMillis() + totalMs }
    LaunchedEffect(endAt) {
        while (progress < 1f) {
            val remaining = (endAt - SystemClock.uptimeMillis()).coerceAtLeast(0)
            progress = ((totalMs - remaining).toFloat() / totalMs).coerceIn(0f, 1f)
            delay(CLOCK_TICK_MS)
        }
    }
    LaunchedEffect(Unit) {
        val trials = runPvt(
            durationMs = totalMs,
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
            // Only a tap on a visible stimulus buzzes; taps in the empty wait stay silent.
            if (stimulusVisible) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            taps.trySend(down.uptimeMillis)
        }
    }
    PvtContent(progress = progress, stimulusVisible = stimulusVisible, tapModifier = tapModifier)
}

// Progress ring around the bezel; the stimulus appears at the centre.
@Composable
private fun PvtContent(progress: Float, stimulusVisible: Boolean, tapModifier: Modifier = Modifier) {
    // Read the latest progress inside the draw lambda so the indicator follows the running clock.
    val liveProgress = rememberUpdatedState(progress)
    Box(
        modifier = Modifier.fillMaxSize().then(tapModifier),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { liveProgress.value },
            modifier = Modifier.fillMaxSize().padding(RING_INSET),
            startAngle = RING_START_ANGLE,
            endAngle = RING_END_ANGLE,
            strokeWidth = AppSizes.RingStroke,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = AppColors.OnBackground,
                trackColor = AppColors.TrackDim,
            ),
        )
        if (stimulusVisible) Stimulus()
    }
}

// Nine-lobed cookie that pulses to draw the eye, with a tap glyph telling you to touch it.
@Composable
private fun Stimulus() {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    val shape = remember { Cookie9Shape() }
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = PULSE_MIN,
        targetValue = PULSE_MAX,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier.size(AppSizes.Stimulus).scale(scale).background(AppColors.Tertiary, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.TouchApp,
            contentDescription = stringResource(R.string.cd_tap),
            tint = AppColors.OnBackground,
            modifier = Modifier.size(STIMULUS_ICON_DP.dp),
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

@WearPreview
@Composable
private fun PvtWaitingPreview() {
    WearScreenPreview { PvtContent(progress = 0.35f, stimulusVisible = false) }
}

@WearPreview
@Composable
private fun PvtStimulusPreview() {
    WearScreenPreview { PvtContent(progress = 0.6f, stimulusVisible = true) }
}
