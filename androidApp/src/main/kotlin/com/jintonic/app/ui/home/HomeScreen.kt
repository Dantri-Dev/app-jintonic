package com.jintonic.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.R
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.ui.components.AppButton
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.AppFilledTonalButton
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.components.ScoreArc
import com.jintonic.app.ui.navigation.Routes
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import com.jintonic.app.ui.theme.color
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(onStartTest: () -> Unit, onNavigate: (String) -> Unit, vm: HomeViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ui = (state as? ResultState.Success)?.data
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    ScreenScaffold(
        scrollState = listState,
        edgeButton = { AppEdgeButton(onClick = onStartTest) { Text(stringResource(R.string.action_do_test)) } },
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(screenHeight), contentAlignment = Alignment.Center) {
                    if (ui == null) {
                        CircularProgressIndicator(modifier = Modifier.size(FfdSizes.Ring))
                    } else {
                        val streak = ui.streakDays
                        val caption = if (streak > 0) pluralStringResource(R.plurals.streak_days, streak, streak) else ""
                        ScoreArc(
                            score = ui.readiness,
                            label = stringResource(R.string.title_readiness),
                            caption = caption,
                            color = ui.status.color(),
                        )
                    }
                }
            }
            item { HomeMenu(pendingSync = ui?.pendingSync ?: 0, onNavigate = onNavigate) }
        }
    }
}

@Composable
private fun HomeMenu(pendingSync: Int, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavButton(R.string.nav_history, Routes.HISTORY, onNavigate)
        NavButton(R.string.nav_body, Routes.BODY, onNavigate)
        NavButton(R.string.nav_params, Routes.PARAMS, onNavigate)
        AppButton(
            onClick = { onNavigate(Routes.EMERGENCY) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FfdColors.AccentStrong, contentColor = FfdColors.Background),
        ) { Text(stringResource(R.string.nav_emergency)) }
        if (pendingSync > 0) Hint(stringResource(R.string.pending_sync, pendingSync), color = FfdColors.AccentSoft)
    }
}

@Composable
private fun NavButton(@StringRes textRes: Int, route: String, onNavigate: (String) -> Unit) {
    AppFilledTonalButton(onClick = { onNavigate(route) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(textRes))
    }
}
