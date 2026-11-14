package com.jintonic.app.ui.test_flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.AppConstants
import com.jintonic.app.ui.components.BodyText
import com.jintonic.app.ui.components.PillButton
import com.jintonic.app.ui.components.TitleText
import com.jintonic.app.ui.components.TopLabel
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import com.jintonic.app.ui.theme.FfdType
import java.util.Locale

private val FACES = listOf("😣", "🙁", "😐", "🙂", "😄")
private const val FACE_OFF_ALPHA = 0.3f

@Composable
fun PreTestScreen(vm: TestFlowViewModel, onConfirm: () -> Unit) {
    val form by vm.form.collectAsStateWithLifecycle()
    ScreenScaffold(timeText = { TopLabel("Pre-test") }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FfdSizes.ScreenGap),
            ) {
                TitleText("Sonno · umore")
                SleepRow(hours = form.sleepHours, onChange = vm::setSleep)
                FacesRow(selected = form.mood, onSelect = vm::setMood)
                PillButton(text = "Conferma", onClick = onConfirm)
            }
        }
    }
}

@Composable
private fun SleepRow(hours: Float, onChange: (Float) -> Unit) {
    val step = AppConstants.SLEEP_STEP_HOURS
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton(
            onClick = { onChange((hours - step).coerceAtLeast(0f)) },
            modifier = Modifier.size(28.dp).semantics { contentDescription = "Meno sonno" },
        ) { Text("−") }
        BodyText(String.format(Locale.ROOT, "%.1f h", hours), color = FfdColors.TextSecondary)
        FilledTonalIconButton(
            onClick = { onChange((hours + step).coerceAtMost(AppConstants.SLEEP_MAX_HOURS)) },
            modifier = Modifier.size(28.dp).semantics { contentDescription = "Più sonno" },
        ) { Text("+") }
    }
}

// Mood 1..5: the selected face is opaque, the others at 30% (mockup ".faces .off").
@Composable
private fun FacesRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        FACES.forEachIndexed { index, face ->
            val mood = index + 1
            Text(
                text = face,
                fontSize = FfdType.Faces,
                modifier = Modifier
                    .alpha(if (mood == selected) 1f else FACE_OFF_ALPHA)
                    .clickable { onSelect(mood) }
                    .semantics { contentDescription = "Umore $mood su 5" },
            )
        }
    }
}
