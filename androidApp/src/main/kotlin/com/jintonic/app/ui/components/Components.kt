package com.jintonic.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val SCORE_MAX = 100f
private const val TRACK_ALPHA = 0.2f

// Wavy edge ring: gap at the bottom for the edge button, active arc runs clockwise from there.
private const val GAP_DEG = 72f
private const val TRACK_START_DEG = 90f + GAP_DEG / 2f
private const val TRACK_SWEEP_DEG = 360f - GAP_DEG
private const val WAVELENGTH_DEG = 24f
private val WAVE_AMPLITUDE = 5.dp

// Full-screen score ring that follows the bezel as an expressive wave, score centred. Screen hero.
@Composable
fun ScoreArc(score: Int?, label: String, caption: String, color: Color, modifier: Modifier = Modifier) {
    val progress = ((score ?: 0) / SCORE_MAX).coerceIn(0f, 1f)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            val stroke = FfdSizes.RingStroke.toPx()
            val amp = WAVE_AMPLITUDE.toPx()
            val radius = size.minDimension / 2f - stroke / 2f - amp
            val center = Offset(size.width / 2f, size.height / 2f)
            drawTrack(center, radius, stroke, color.copy(alpha = TRACK_ALPHA))
            if (progress > 0f) drawWave(center, radius, amp, stroke, color, progress)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score?.toString() ?: "--",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = FfdColors.OnBackground,
            )
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = FfdColors.TextSecondary)
            if (caption.isNotEmpty()) {
                Text(caption, style = MaterialTheme.typography.labelSmall, color = FfdColors.AccentSoft)
            }
        }
    }
}

private fun DrawScope.drawTrack(center: Offset, radius: Float, stroke: Float, color: Color) {
    drawArc(
        color = color,
        startAngle = TRACK_START_DEG,
        sweepAngle = TRACK_SWEEP_DEG,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawWave(center: Offset, radius: Float, amp: Float, stroke: Float, color: Color, progress: Float) {
    val sweep = TRACK_SWEEP_DEG * progress
    val steps = (sweep / 2f).toInt().coerceAtLeast(2)
    val path = Path()
    for (i in 0..steps) {
        val deg = TRACK_START_DEG + sweep * (i / steps.toFloat())
        val offsetDeg = deg - TRACK_START_DEG
        val r = radius + amp * sin(offsetDeg / WAVELENGTH_DEG * 2f * PI.toFloat())
        val rad = deg * PI.toFloat() / 180f
        val x = center.x + r * cos(rad)
        val y = center.y + r * sin(rad)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path = path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

// Label over value, centred: long labels never squeeze or split the value on a round face.
@Composable
fun InfoRow(label: String, value: String, valueColor: Color = FfdColors.OnBackground) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = FfdColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.Center,
        )
    }
}

// One-line explanation under a hero or at the foot of a list.
@Composable
fun Hint(text: String, modifier: Modifier = Modifier, color: Color = FfdColors.TextSecondary) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp),
        style = MaterialTheme.typography.bodySmall,
        color = color,
        textAlign = TextAlign.Center,
    )
}

// Empty / error state: an outlined icon over one explanatory line.
@Composable
fun EmptyState(icon: ImageVector, text: String, tint: Color = FfdColors.TextSecondary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        Hint(text, color = tint)
    }
}

// Centred single-screen content (loaders, errors).
@Composable
fun CenteredColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = { content() },
    )
}
