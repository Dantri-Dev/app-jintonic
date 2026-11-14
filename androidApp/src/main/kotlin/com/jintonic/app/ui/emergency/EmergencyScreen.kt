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
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthView
import com.jintonic.app.ui.components.BodyText
import com.jintonic.app.ui.components.MenuButton
import com.jintonic.app.ui.components.TopLabel
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
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    DisposableEffect(Unit) { onDispose { vm.stopBreathing() } }
    ScreenScaffold(scrollState = listState, timeText = { TopLabel("Emergenza") }) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = it) {
            if (view != null) triageItems(view)
            item { BreathingItem(phase, view?.assessment?.breathingGuide ?: BreathingGuide(), vm) }
            item { MenuButton("Test rapido 30 s", onClick = onQuickTest) }
            item {
                SwitchButton(
                    checked = view?.snapshot?.crashActive == true,
                    onCheckedChange = healthVm::setSpo2Crash,
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
            color = if (a.triage) FfdColors.Red else a.overallLevel.color(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    item { BodyText(timeToCriticalText(view.minutesToCritical)) }
    item { BodyText("HR ${view.snapshot.hrBpm ?: "--"} bpm · SpO2 ${view.snapshot.spo2Pct}%") }
    a.readings.filter { it.level == AlertLevel.CRITICAL }.forEach { reading ->
        item { BodyText("Critico: ${reading.parameter.label()}", color = FfdColors.Red) }
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
        MenuButton("Respirazione guidata", accent = FfdColors.Green) { vm.startBreathing(guide) }
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Box(Modifier.size(100.dp).scale(scale).border(6.dp, FfdColors.Green, CircleShape))
            Text(if (phase == BreathPhase.INHALE) "Inspira" else "Espira", fontWeight = FontWeight.SemiBold)
        }
        MenuButton("Stop") { vm.stopBreathing() }
    }
}
