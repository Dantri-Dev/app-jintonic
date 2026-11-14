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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.jintonic.app.R
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthView
import com.jintonic.app.data.ManualEntry
import com.jintonic.app.data.sensors.HrSource
import com.jintonic.app.scoring.CognitiveScoring
import com.jintonic.app.ui.components.AppIconButton
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.components.InfoRow
import com.jintonic.app.ui.components.hapticToggle
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.color
import com.jintonic.app.ui.theme.labelRes
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.HealthParameter

@Composable
fun HealthScreen(vm: HealthViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val view = (state as? ResultState.Success)?.data
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.nav_params)) } }
            if (view == null) {
                item { Hint(stringResource(R.string.loading)) }
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
        InfoRow(stringResource(R.string.title_readiness), "$score", CognitiveScoring.status(score).color())
    }
    item {
        InfoRow(
            stringResource(R.string.overall_level),
            stringResource(view.assessment.overallLevel.labelRes()),
            view.assessment.overallLevel.color(),
        )
    }
    item {
        val source = when (view.snapshot.hrSource) {
            HrSource.SENSOR -> stringResource(R.string.hr)
            HrSource.SIMULATED -> stringResource(R.string.hr_simulated)
            HrSource.SIMULATED_OFF_BODY -> stringResource(R.string.hr_simulated_off)
        }
        val value = view.snapshot.hrBpm?.let { stringResource(R.string.hr_value, it) } ?: stringResource(R.string.value_dash)
        InfoRow(source, value, view.levelOf(HealthParameter.HR)?.color() ?: FfdColors.OnBackground)
    }
    item {
        InfoRow(
            stringResource(R.string.spo2_nominal),
            stringResource(R.string.percent, view.snapshot.spo2Pct),
            view.levelOf(HealthParameter.SPO2)?.color() ?: FfdColors.OnBackground,
        )
    }
    item {
        SwitchButton(
            checked = view.snapshot.crashActive,
            onCheckedChange = hapticToggle(vm::setSpo2Crash),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.sim_spo2_drop)) }
    }
}

private fun ScalingLazyListScope.manualItems(view: HealthView, vm: HealthViewModel) {
    val m = view.manual
    item { ListHeader { Text(stringResource(R.string.manual_entry)) } }
    item {
        val value = m.glucoseMgDl?.let { stringResource(R.string.glucose_value, it) }
        StepperLine(stringResource(R.string.param_glucose), value, view.levelOf(HealthParameter.GLUCOSE)) { delta ->
            vm.updateManual { it.copy(glucoseMgDl = ((it.glucoseMgDl ?: GLUCOSE_DEFAULT) + delta * GLUCOSE_STEP).coerceIn(0, GLUCOSE_MAX)) }
        }
    }
    item {
        val value = m.stress?.let { stringResource(R.string.stress_value, it) }
        StepperLine(stringResource(R.string.param_stress), value, view.levelOf(HealthParameter.STRESS)) { delta ->
            vm.updateManual { it.copy(stress = ((it.stress ?: SCALE_DEFAULT) + delta).coerceIn(1, SCALE_MAX)) }
        }
    }
    item {
        val value = m.radiationMsvPerDay?.let { stringResource(R.string.radiation_value, it.toString()) }
        StepperLine(stringResource(R.string.param_radiation), value, view.levelOf(HealthParameter.RADIATION)) { delta ->
            vm.updateManual { it.copy(radiationMsvPerDay = stepRadiation(it, delta)) }
        }
    }
    item {
        val value = m.exerciseMinPerDay?.let { stringResource(R.string.exercise_value, it) }
        StepperLine(stringResource(R.string.param_exercise), value, view.levelOf(HealthParameter.EXERCISE)) { delta ->
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
    val decDesc = stringResource(R.string.stepper_decrease, label)
    val incDesc = stringResource(R.string.stepper_increase, label)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = FfdColors.TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppIconButton(
                onClick = { onStep(-1) },
                modifier = Modifier.size(32.dp).semantics { contentDescription = decDesc },
            ) { Text("−") }
            Text(
                value ?: stringResource(R.string.value_not_set),
                style = MaterialTheme.typography.titleMedium,
                color = level?.color() ?: FfdColors.OnBackground,
                fontWeight = FontWeight.SemiBold,
            )
            AppIconButton(
                onClick = { onStep(1) },
                modifier = Modifier.size(32.dp).semantics { contentDescription = incDesc },
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
