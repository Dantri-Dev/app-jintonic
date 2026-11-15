package com.jintonic.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val APPEAR_MS = 280
private val APPEAR_RISE = 8.dp

// Subtle one-shot entrance: fades in while rising a few pixels. Runs once per composition.
@Composable
fun Modifier.appearOnce(durationMs: Int = APPEAR_MS, rise: Dp = APPEAR_RISE): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        label = "appear",
    )
    val risePx = with(LocalDensity.current) { rise.toPx() }
    return graphicsLayer {
        alpha = appear
        translationY = (1f - appear) * risePx
    }
}
