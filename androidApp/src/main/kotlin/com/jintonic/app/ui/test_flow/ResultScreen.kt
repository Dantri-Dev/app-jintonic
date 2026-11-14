package com.jintonic.app.ui.test_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.advice.AdviceMessages
import com.jintonic.app.assessment.CognitiveAssessment
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.pvt.Status
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.AppFilledTonalButton
import com.jintonic.app.ui.components.CenteredColumn
import com.jintonic.app.ui.components.EmptyState
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.components.ScoreArc
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.color

private fun Status.headline(): String = when (this) {
    Status.GREEN -> "Sei pronto. Buon lavoro."
    Status.YELLOW -> "Fai una pausa di 10 minuti."
    Status.RED -> "Evita compiti critici. Riposa."
}

@Composable
fun ResultScreen(vm: TestFlowViewModel, onDone: () -> Unit, onRetry: () -> Unit, onHistory: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    ScreenScaffold(
        scrollState = listState,
        edgeButton = { AppEdgeButton(onClick = onDone) { Text("Fatto") } },
    ) { contentPadding ->
        when (val s = state) {
            is ResultState.Success -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = contentPadding,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val cognitive = s.data.cognitive
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(screenHeight), contentAlignment = Alignment.Center) {
                        ScoreArc(score = cognitive.score, label = "Cognitivo", caption = "", color = cognitive.status.color())
                    }
                }
                item { ResultDetails(cognitive, onRetry, onHistory) }
            }
            is ResultState.Failure -> CenteredColumn {
                EmptyState(Icons.Outlined.ErrorOutline, s.message, tint = FfdColors.AccentStrong)
            }
            else -> CenteredColumn { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun ResultDetails(cognitive: CognitiveAssessment, onRetry: () -> Unit, onHistory: () -> Unit) {
    val headline = cognitive.status.headline()
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Hint(headline, color = FfdColors.OnBackground)
        if (cognitive.escalateToSurgeon) Hint("Contatta il flight surgeon", color = FfdColors.AccentStrong)
        if (cognitive.perceivedMismatch) Hint("Ti senti bene, ma i riflessi dicono il contrario", color = FfdColors.AccentSoft)
        if (cognitive.sleepWarning) Hint("Sonno insufficiente", color = FfdColors.AccentSoft)
        cognitive.advice.map { AdviceMessages.text(it) }.filter { it != headline }.forEach { Hint(it) }
        AppFilledTonalButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Rifai") }
        AppFilledTonalButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("Storico") }
    }
}
