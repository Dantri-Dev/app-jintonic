package com.jintonic.app.ui.test_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ScreenScaffold
import com.jintonic.app.advice.AdviceMessages
import com.jintonic.app.assessment.CognitiveAssessment
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.pvt.Status
import com.jintonic.app.ui.components.BodyText
import com.jintonic.app.ui.components.MenuButton
import com.jintonic.app.ui.components.ScoreRing
import com.jintonic.app.ui.components.TopLabel
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import com.jintonic.app.ui.theme.color

// Headline copy per outcome, verbatim from the mockup; the engine's full advice follows below.
private fun Status.headline(): String = when (this) {
    Status.GREEN -> "Sei pronto. Buon lavoro."
    Status.YELLOW -> "Fai una pausa di 10 minuti."
    Status.RED -> "Evita compiti critici. Riposa."
}

@Composable
fun ResultScreen(vm: TestFlowViewModel, onDone: () -> Unit, onRetry: () -> Unit, onHistory: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    ScreenScaffold(scrollState = scroll, timeText = { TopLabel("Risultato") }) {
        when (val s = state) {
            is ResultState.Success -> Column(
                modifier = Modifier.verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ResultHero(s.data.cognitive)
                ResultDetails(s.data.cognitive, onDone, onRetry, onHistory)
            }
            is ResultState.Failure -> CenteredBox { BodyText(s.message, color = FfdColors.Red) }
            else -> CenteredBox { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun ResultHero(cognitive: CognitiveAssessment) {
    CenteredBox {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FfdSizes.ScreenGap),
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            ScoreRing(score = cognitive.score, caption = "Cognitivo", ringColor = cognitive.status.color())
            BodyText(cognitive.status.headline())
        }
    }
}

@Composable
private fun ResultDetails(
    cognitive: CognitiveAssessment,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    onHistory: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (cognitive.escalateToSurgeon) BodyText("«Contatta il flight surgeon»", color = FfdColors.Red)
        if (cognitive.perceivedMismatch) BodyText("«Ti senti bene, ma i riflessi dicono il contrario»", color = FfdColors.Yellow)
        if (cognitive.sleepWarning) BodyText("Sonno insufficiente", color = FfdColors.Yellow)
        cognitive.advice.forEach { BodyText(AdviceMessages.text(it)) }
        MenuButton("Fatto", onClick = onDone)
        MenuButton("Rifai", onClick = onRetry)
        MenuButton("Storico", onClick = onHistory)
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(LocalConfiguration.current.screenHeightDp.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
