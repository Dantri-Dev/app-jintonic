package com.jintonic.app.ui.test_flow

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.Constants
import com.jintonic.app.pvt.PvtTrial
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val MS_PER_SEC = 1_000L
private const val SEC_PER_MIN = 60
private const val CLOCK_TICK_MS = 250L
private const val GLOW_ALPHA = 0.5f

// A different shape is shown on each stimulus so no two presentations look alike.
private enum class StimulusShape { CIRCLE, SQUARE, TRIANGLE, DIAMOND, HEXAGON, STAR }

@Composable
fun PvtScreen(vm: TestFlowViewModel, onFinished: () -> Unit) {
    var shape by remember { mutableStateOf<StimulusShape?>(null) }
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
            onStimulus = { shape = it },
        )
        vm.finish(trials)
        onFinished()
    }

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val down = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                            if (down != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                taps.trySend(down.uptimeMillis)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatClock(remainingMs),
                style = MaterialTheme.typography.labelMedium,
                color = FfdColors.TextSecondary,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            )
            shape?.let { Stimulus(it) }
            Text(
                text = "tocca appena appare",
                style = MaterialTheme.typography.labelMedium,
                color = FfdColors.TextSecondary,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
            )
        }
    }
}

// Filled shape with a soft glow behind it.
@Composable
private fun Stimulus(shape: StimulusShape) {
    val color = FfdColors.Primary
    Canvas(modifier = Modifier.size(FfdSizes.Stimulus + FfdSizes.StimulusGlow * 2)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = FfdSizes.Stimulus.toPx() / 2f
        drawCircle(
            brush = Brush.radialGradient(
                0f to color.copy(alpha = GLOW_ALPHA),
                radius / (radius + FfdSizes.StimulusGlow.toPx()) to color.copy(alpha = GLOW_ALPHA),
                1f to Color.Transparent,
                center = center,
                radius = radius + FfdSizes.StimulusGlow.toPx(),
            ),
            radius = radius + FfdSizes.StimulusGlow.toPx(),
            center = center,
        )
        drawShape(shape, center, radius, color)
    }
}

private fun DrawScope.drawShape(shape: StimulusShape, center: Offset, radius: Float, color: Color) {
    when (shape) {
        StimulusShape.CIRCLE -> drawCircle(color, radius * 0.82f, center)
        StimulusShape.SQUARE -> {
            val half = radius * 0.72f
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(half * 2f, half * 2f),
                cornerRadius = CornerRadius(radius * 0.22f),
            )
        }
        StimulusShape.TRIANGLE -> drawPath(polygon(center, radius * 0.95f, 3), color)
        StimulusShape.DIAMOND -> drawPath(polygon(center, radius * 0.92f, 4), color)
        StimulusShape.HEXAGON -> drawPath(polygon(center, radius * 0.9f, 6), color)
        StimulusShape.STAR -> drawPath(star(center, radius * 0.95f, radius * 0.42f, 5), color)
    }
}

private fun polygon(center: Offset, radius: Float, sides: Int): Path {
    val path = Path()
    for (i in 0 until sides) {
        val a = (-90f + i * 360f / sides) * PI.toFloat() / 180f
        val x = center.x + radius * cos(a)
        val y = center.y + radius * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun star(center: Offset, outer: Float, inner: Float, points: Int): Path {
    val path = Path()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outer else inner
        val a = (-90f + i * 180f / points) * PI.toFloat() / 180f
        val x = center.x + r * cos(a)
        val y = center.y + r * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
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
    onStimulus: (StimulusShape?) -> Unit,
): List<PvtTrial> {
    val trials = mutableListOf<PvtTrial>()
    val shapes = StimulusShape.entries
    var shapeIndex = 0
    val endAt = SystemClock.uptimeMillis() + durationMs
    fun remaining() = (endAt - SystemClock.uptimeMillis()).coerceAtLeast(0)
    while (remaining() > 0) {
        // A tap while waiting is a false start.
        val early = withTimeoutOrNull(minOf(nextDelay(), remaining())) { taps.receive() }
        if (early != null) {
            trials += PvtTrial(stimulusAtMs = early, responseAtMs = early, falseStart = true)
        } else if (remaining() > 0) {
            trials += presentStimulus(taps, shapes[shapeIndex % shapes.size], onStimulus)
            shapeIndex++
        }
    }
    return trials
}

// Stamps the stimulus after its frame is drawn, not when it is requested.
private suspend fun presentStimulus(taps: Channel<Long>, shape: StimulusShape, onStimulus: (StimulusShape?) -> Unit): PvtTrial {
    onStimulus(shape)
    withFrameNanos { }
    withFrameNanos { }
    val shownAt = SystemClock.uptimeMillis()
    val response = withTimeoutOrNull(Constants.TIMEOUT_MS) { taps.receive() }
    onStimulus(null)
    return PvtTrial(stimulusAtMs = shownAt, responseAtMs = response)
}

private fun formatClock(ms: Long): String {
    val totalSec = (ms + MS_PER_SEC - 1) / MS_PER_SEC
    return "%d:%02d".format(totalSec / SEC_PER_MIN, totalSec % SEC_PER_MIN)
}
