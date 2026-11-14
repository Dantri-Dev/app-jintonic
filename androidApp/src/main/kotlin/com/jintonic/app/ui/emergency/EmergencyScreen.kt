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
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthView
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.AppFilledTonalButton
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.components.hapticToggle
import com.jintonic.app.ui.health.HealthViewModel
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.color
import com.jintonic.app.ui.theme.label
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.BreathingGuide
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

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
    val view = (state as? ResultState.Success)?.data
    val listState = rememberScalingLazyListState()
    DisposableEffect(Unit) { onDispose { vm.stopBreathing() } }
    ScreenScaffold(
        scrollState = listState,
        edgeButton = { AppEdgeButton(onClick = onQuickTest) { Text("Test rapido 30 s") } },
    ) { contentPadding ->
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = contentPadding) {
            item { ListHeader { Text("Emergenza") } }
            if (view != null) triageItems(view)
            item { BreathingItem(phase, view?.assessment?.breathingGuide ?: BreathingGuide(), vm) }
            item {
                SwitchButton(
                    checked = view?.snapshot?.crashActive == true,
                    onCheckedChange = hapticToggle(healthVm::setSpo2Crash),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Simula calo SpO2") }
            }
        }
    }
}

private fun ScalingLazyListScope.triageItems(view: HealthView) {
    val a = view.assessment
    item {
        Text(
            text = if (a.triage) "TRIAGE ATTIVO" else "Parametri: ${a.overallLevel.label()}",
            color = if (a.triage) FfdColors.AccentStrong else a.overallLevel.color(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    item { Hint(timeToCriticalText(view.minutesToCritical)) }
    item { Hint("HR ${view.snapshot.hrBpm ?: "--"} bpm · SpO2 ${view.snapshot.spo2Pct}%") }
    a.readings.filter { it.level == AlertLevel.CRITICAL }.forEach { reading ->
        item { Hint("Critico: ${reading.parameter.label()}", color = FfdColors.AccentStrong) }
    }
}

private fun timeToCriticalText(minutes: Double?): String = when {
    minutes == null -> "Andamento stabile"
    minutes <= 0.0 -> "Soglia critica raggiunta"
    else -> "Time-to-Critical ≈ ${minutes.roundToInt().coerceAtLeast(1)} min"
}

@Composable
private fun BreathingItem(phase: BreathPhase, guide: BreathingGuide, vm: EmergencyViewModel) {
    val target = if (phase == BreathPhase.INHALE) 1f else BREATH_SCALE_MIN
    val duration = (if (phase == BreathPhase.INHALE) guide.inhaleSec else guide.exhaleSec) * MS_PER_SEC
    val scale by animateFloatAsState(target, tween(duration), label = "breath")
    if (phase == BreathPhase.IDLE) {
        AppFilledTonalButton(onClick = { vm.startBreathing(guide) }, modifier = Modifier.fillMaxWidth()) {
            Text("Respirazione guidata")
        }
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Box(Modifier.size(100.dp).scale(scale).border(6.dp, FfdColors.Primary, CircleShape))
            Text(
                if (phase == BreathPhase.INHALE) "Inspira" else "Espira",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        AppFilledTonalButton(onClick = { vm.stopBreathing() }, modifier = Modifier.fillMaxWidth()) { Text("Stop") }
    }
}
