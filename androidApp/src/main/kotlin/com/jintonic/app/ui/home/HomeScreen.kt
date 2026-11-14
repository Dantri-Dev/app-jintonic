package com.jintonic.app.ui.home

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
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.ui.components.BodyText
import com.jintonic.app.ui.components.MenuButton
import com.jintonic.app.ui.components.PillButton
import com.jintonic.app.ui.components.ScoreRing
import com.jintonic.app.ui.components.TopLabel
import com.jintonic.app.ui.navigation.Routes
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import com.jintonic.app.ui.theme.FfdType
import com.jintonic.app.ui.theme.color
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(onStartTest: () -> Unit, onNavigate: (String) -> Unit, vm: HomeViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ui = (state as? ResultState.Success)?.data
    val scroll = rememberScrollState()
    ScreenScaffold(scrollState = scroll, timeText = { TopLabel("Fit-for-Duty") }) {
        Column(
            modifier = Modifier.verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeHero(ui, onStartTest)
            HomeMenu(ui, onNavigate)
        }
    }
}

// First viewport = the mockup: ring, "Fai il test", streak.
@Composable
private fun HomeHero(ui: HomeUi?, onStartTest: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(LocalConfiguration.current.screenHeightDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FfdSizes.ScreenGap),
        ) {
            ScoreRing(score = ui?.readiness, caption = "Readiness", ringColor = ui?.status?.color() ?: FfdColors.LabelTop)
            PillButton(text = "Fai il test", onClick = onStartTest)
            val streak = ui?.streakDays ?: 0
            Text(
                text = if (streak > 0) "🔥 $streak ${if (streak == 1) "giorno" else "giorni"}" else "Primo test di oggi",
                fontSize = FfdType.Streak,
                color = FfdColors.AccentSoft,
            )
        }
    }
}

@Composable
private fun HomeMenu(ui: HomeUi?, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MenuButton("Storico") { onNavigate(Routes.HISTORY) }
        MenuButton("Corpo") { onNavigate(Routes.BODY) }
        MenuButton("Parametri") { onNavigate(Routes.PARAMS) }
        MenuButton("Emergenza", accent = FfdColors.AccentStrong) { onNavigate(Routes.EMERGENCY) }
        val pending = ui?.pendingSync ?: 0
        if (pending > 0) BodyText("$pending in coda per la Terra")
    }
}
