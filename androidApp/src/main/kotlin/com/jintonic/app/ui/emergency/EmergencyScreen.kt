package com.jintonic.app.ui.emergency

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.R
import com.jintonic.app.core.compose.PreviewData
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthView
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.AppFilledTonalButton
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.health.HealthViewModel
import com.jintonic.app.ui.theme.AppColors
import com.jintonic.app.ui.theme.color
import com.jintonic.app.ui.theme.labelRes
import com.jintonic.app.vitals.BreathingGuide
import org.koin.compose.viewmodel.koinViewModel

private const val BREATH_SCALE_MIN = 0.6f
private const val MS_PER_SEC = 1_000

@Composable
fun EmergencyScreen(
    healthVm: HealthViewModel,
    onQuickTest: () -> Unit,
    vm: EmergencyViewModel = koinViewModel(),
) {
    val state by healthVm.state.collectAsStateWithLifecycle()
    val phase by vm.phase.collectAsStateWithLifecycle()
    DisposableEffect(Unit) { onDispose { vm.stopBreathing() } }
    EmergencyContent(
        view = (state as? ResultState.Success)?.data,
        phase = phase,
        onQuickTest = onQuickTest,
        onStartBreathing = vm::startBreathing,
        onStopBreathing = vm::stopBreathing,
    )
}

@Composable
private fun EmergencyContent(
    view: HealthView?,
    phase: BreathPhase,
    onQuickTest: () -> Unit,
    onStartBreathing: (BreathingGuide) -> Unit,
    onStopBreathing: () -> Unit,
) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = listState,
        edgeButton = { AppEdgeButton(onClick = onQuickTest) { Text(stringResource(R.string.action_quick_test)) } },
    ) { contentPadding ->
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.nav_emergency)) } }
            if (view != null) statusItems(view)
            item {
                BreathingItem(
                    phase = phase,
                    guide = view?.assessment?.breathingGuide ?: BreathingGuide(),
                    onStartBreathing = onStartBreathing,
                    onStopBreathing = onStopBreathing,
                )
            }
        }
    }
}

private fun ScalingLazyListScope.statusItems(view: HealthView) {
    val a = view.assessment
    item {
        val text = if (a.triage) {
            stringResource(R.string.triage_active)
        } else {
            stringResource(R.string.params_level, stringResource(a.overallLevel.labelRes()))
        }
        Text(
            text = text,
            color = if (a.triage) AppColors.AccentStrong else a.overallLevel.color(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    item {
        val hr = view.snapshot.hrBpm?.toString() ?: stringResource(R.string.value_dash)
        Hint(stringResource(R.string.vitals_line, hr, view.snapshot.spo2Pct))
    }
}

@Composable
private fun BreathingItem(
    phase: BreathPhase,
    guide: BreathingGuide,
    onStartBreathing: (BreathingGuide) -> Unit,
    onStopBreathing: () -> Unit,
) {
    val target = if (phase == BreathPhase.INHALE) 1f else BREATH_SCALE_MIN
    val duration = (if (phase == BreathPhase.INHALE) guide.inhaleSec else guide.exhaleSec) * MS_PER_SEC
    val scale by animateFloatAsState(target, tween(duration), label = "breath")
    if (phase == BreathPhase.IDLE) {
        AppFilledTonalButton(onClick = { onStartBreathing(guide) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.breathing_guided))
        }
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Box(Modifier.size(100.dp).scale(scale).border(6.dp, AppColors.Primary, CircleShape))
            Text(
                stringResource(if (phase == BreathPhase.INHALE) R.string.breathe_in else R.string.breathe_out),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        AppFilledTonalButton(onClick = { onStopBreathing() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_stop))
        }
    }
}

@WearPreview
@Composable
private fun EmergencyIdlePreview() {
    WearScreenPreview {
        EmergencyContent(
            view = PreviewData.healthView(AlertLevel.WARNING),
            phase = BreathPhase.IDLE,
            onQuickTest = {},
            onStartBreathing = {},
            onStopBreathing = {},
        )
    }
}

@WearPreview
@Composable
private fun EmergencyBreathingPreview() {
    WearScreenPreview {
        EmergencyContent(
            view = PreviewData.healthView(AlertLevel.CRITICAL),
            phase = BreathPhase.INHALE,
            onQuickTest = {},
            onStartBreathing = {},
            onStopBreathing = {},
        )
    }
}
