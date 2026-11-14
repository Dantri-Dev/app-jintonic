package com.jintonic.app.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthView
import com.jintonic.app.data.ManualEntry
import com.jintonic.app.data.sensors.HrSource
import com.jintonic.app.ui.components.BodyText
import com.jintonic.app.ui.components.InfoRow
import com.jintonic.app.ui.components.TopLabel
import com.jintonic.app.ui.theme.FfdType
import com.jintonic.app.scoring.CognitiveScoring
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.color
import com.jintonic.app.ui.theme.label
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.HealthParameter

@Composable
fun HealthScreen(vm: HealthViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val view = (state as? ResultState.Success)?.data
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    ScreenScaffold(scrollState = listState, timeText = { TopLabel("Parametri") }) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = it) {
            if (view == null) {
                item { BodyText("Lettura in corso…") }
            } else {
                vitalsItems(view, vm)
                manualItems(view, vm)
            }
        }
    }
}

private fun HealthView.levelOf(parameter: HealthParameter): AlertLevel? =
    assessment.readings.firstOrNull { it.parameter == parameter }?.level

private fun ScalingLazyListScope.vitalsItems(view: HealthView, vm: HealthViewModel) {
    item {
        val score = view.assessment.readinessScore
        InfoRow("Readiness", "$score", CognitiveScoring.status(score).color())
    }
    item { InfoRow("Livello complessivo", view.assessment.overallLevel.label(), view.assessment.overallLevel.color()) }
    item {
        val source = when (view.snapshot.hrSource) {
            HrSource.SENSOR -> "HR"
            HrSource.SIMULATED -> "HR (simulata)"
            HrSource.SIMULATED_OFF_BODY -> "HR (simulata · non al polso)"
        }
        InfoRow(source, view.snapshot.hrBpm?.let { "$it bpm" } ?: "--", view.levelOf(HealthParameter.HR)?.color() ?: FfdColors.OnBackground)
    }
    item { InfoRow("SpO2 (nominale)", "${view.snapshot.spo2Pct}%", view.levelOf(HealthParameter.SPO2)?.color() ?: FfdColors.OnBackground) }
    item {
        SwitchButton(
            checked = view.snapshot.crashActive,
            onCheckedChange = vm::setSpo2Crash,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Simula calo SpO2") }
    }
}

private fun ScalingLazyListScope.manualItems(view: HealthView, vm: HealthViewModel) {
    val m = view.manual
    item { BodyText("Inserimento manuale", color = FfdColors.LabelTop) }
    item {
        StepperLine("Glicemia", m.glucoseMgDl?.let { "$it mg/dL" }, view.levelOf(HealthParameter.GLUCOSE)) { delta ->
            vm.updateManual { it.copy(glucoseMgDl = ((it.glucoseMgDl ?: GLUCOSE_DEFAULT) + delta * GLUCOSE_STEP).coerceIn(0, GLUCOSE_MAX)) }
        }
    }
    item {
        StepperLine("Stress", m.stress?.let { "$it / 5" }, view.levelOf(HealthParameter.STRESS)) { delta ->
            vm.updateManual { it.copy(stress = ((it.stress ?: SCALE_DEFAULT) + delta).coerceIn(1, SCALE_MAX)) }
        }
    }
    item {
        StepperLine("Radiazioni", m.radiationMsvPerDay?.let { "$it mSv/g" }, view.levelOf(HealthParameter.RADIATION)) { delta ->
            vm.updateManual { it.copy(radiationMsvPerDay = stepRadiation(it, delta)) }
        }
    }
    item {
        StepperLine("Esercizio", m.exerciseMinPerDay?.let { "$it min/g" }, view.levelOf(HealthParameter.EXERCISE)) { delta ->
            vm.updateManual {
                it.copy(exerciseMinPerDay = ((it.exerciseMinPerDay ?: EXERCISE_DEFAULT) + delta * EXERCISE_STEP).coerceIn(0, EXERCISE_MAX))
            }
        }
    }
}

private fun stepRadiation(entry: ManualEntry, delta: Int): Double {
    val next = (entry.radiationMsvPerDay ?: RADIATION_DEFAULT) + delta * RADIATION_STEP
    return (Math.round(next.coerceAtLeast(0.0) * RADIATION_ROUND) / RADIATION_ROUND)
}

@Composable
private fun StepperLine(label: String, value: String?, level: AlertLevel?, onStep: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = FfdType.Body, color = FfdColors.TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalIconButton(
                onClick = { onStep(-1) },
                modifier = Modifier.size(28.dp).semantics { contentDescription = "Diminuisci $label" },
            ) { Text("−") }
            Text(
                value ?: "non inserito",
                fontSize = FfdType.Title,
                color = level?.color() ?: FfdColors.OnBackground,
                fontWeight = FontWeight.SemiBold,
            )
            FilledTonalIconButton(
                onClick = { onStep(1) },
                modifier = Modifier.size(28.dp).semantics { contentDescription = "Aumenta $label" },
            ) { Text("+") }
        }
    }
}

private const val GLUCOSE_DEFAULT = 100
private const val GLUCOSE_STEP = 10
private const val GLUCOSE_MAX = 600
private const val SCALE_DEFAULT = 2
private const val SCALE_MAX = 5
private const val RADIATION_DEFAULT = 0.5
private const val RADIATION_STEP = 0.5
private const val RADIATION_ROUND = 10.0
private const val EXERCISE_DEFAULT = 90
private const val EXERCISE_STEP = 10
private const val EXERCISE_MAX = 600
