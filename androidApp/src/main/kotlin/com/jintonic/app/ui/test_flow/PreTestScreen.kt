package com.jintonic.app.ui.test_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.jintonic.app.R
import com.jintonic.app.core.AppConstants
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.AppIconButton
import com.jintonic.app.ui.components.tappable
import com.jintonic.app.ui.theme.FfdColors

private val FACES = listOf("😣", "🙁", "😐", "🙂", "😄")
private const val FACE_OFF_ALPHA = 0.3f

// Fixed single screen: sleep stepper and mood faces, confirmed with an edge button.
@Composable
fun PreTestScreen(vm: TestFlowViewModel, onConfirm: () -> Unit) {
    val form by vm.form.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.pretest_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FfdColors.OnBackground,
                textAlign = TextAlign.Center,
            )
            SleepRow(hours = form.sleepHours, onChange = vm::setSleep)
            FacesRow(selected = form.mood, onSelect = vm::setMood)
        }
        AppEdgeButton(onClick = onConfirm, modifier = Modifier.align(Alignment.BottomCenter)) {
            Text(stringResource(R.string.action_confirm))
        }
    }
}

@Composable
private fun SleepRow(hours: Float, onChange: (Float) -> Unit) {
    val step = AppConstants.SLEEP_STEP_HOURS
    val lessSleep = stringResource(R.string.sleep_less)
    val moreSleep = stringResource(R.string.sleep_more)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppIconButton(
            onClick = { onChange((hours - step).coerceAtLeast(0f)) },
            modifier = Modifier.size(32.dp).semantics { contentDescription = lessSleep },
        ) { Text("−") }
        Text(
            stringResource(R.string.sleep_hours, hours),
            style = MaterialTheme.typography.titleMedium,
            color = FfdColors.OnBackground,
        )
        AppIconButton(
            onClick = { onChange((hours + step).coerceAtMost(AppConstants.SLEEP_MAX_HOURS)) },
            modifier = Modifier.size(32.dp).semantics { contentDescription = moreSleep },
        ) { Text("+") }
    }
}

// Mood 1..5: the selected face is opaque, the others dimmed.
@Composable
private fun FacesRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        FACES.forEachIndexed { index, face ->
            val mood = index + 1
            val desc = stringResource(R.string.mood_desc, mood)
            Text(
                text = face,
                fontSize = 22.sp,
                modifier = Modifier
                    .alpha(if (mood == selected) 1f else FACE_OFF_ALPHA)
                    .tappable { onSelect(mood) }
                    .semantics { contentDescription = desc },
            )
        }
    }
}
