package com.jintonic.app.ui.test_flow

import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.R
import com.jintonic.app.assessment.CognitiveAssessment
import com.jintonic.app.core.compose.PreviewData
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.pvt.PvtResult
import com.jintonic.app.pvt.Status
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.CenteredColumn
import com.jintonic.app.ui.components.EmptyState
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.components.ScoreArc
import com.jintonic.app.ui.theme.AppColors
import com.jintonic.app.ui.theme.color

@StringRes
private fun Status.headlineRes(): Int = when (this) {
    Status.GREEN -> R.string.headline_green
    Status.YELLOW -> R.string.headline_yellow
    Status.RED -> R.string.headline_red
}

@Composable
fun ResultScreen(vm: TestFlowViewModel, onDone: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    ResultContent(state = state, onDone = onDone)
}

@Composable
private fun ResultContent(state: ResultState<TestOutcome>, onDone: () -> Unit) {
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    ScreenScaffold(
        scrollState = listState,
        edgeButton = { AppEdgeButton(onClick = onDone) { Text(stringResource(R.string.action_done)) } },
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
                        ScoreArc(
                            score = cognitive.score,
                            label = stringResource(R.string.title_cognitive),
                            caption = "",
                            color = cognitive.status.color(),
                        )
                    }
                }
                item { ResultDetails(cognitive) }
            }
            is ResultState.Failure -> CenteredColumn {
                EmptyState(Icons.Outlined.ErrorOutline, s.message, tint = AppColors.AccentStrong)
            }
            else -> CenteredColumn { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun ResultDetails(cognitive: CognitiveAssessment) {
    val alert: Pair<Int, Color>? = when {
        cognitive.escalateToSurgeon -> R.string.alert_surgeon to AppColors.AccentStrong
        cognitive.perceivedMismatch -> R.string.alert_mismatch to AppColors.AccentSoft
        cognitive.sleepWarning -> R.string.alert_sleep to AppColors.AccentSoft
        else -> null
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Hint(stringResource(cognitive.status.headlineRes()), color = AppColors.OnBackground)
        alert?.let { (res, color) -> Hint(stringResource(res), color = color) }
    }
}

private val PREVIEW_PVT = PvtResult(medianRtMs = 280.0, meanSpeed = 3.6, lapses = 1, falseStarts = 0, validTrials = 20)

@WearPreview
@Composable
private fun ResultGreenPreview() {
    WearScreenPreview {
        ResultContent(
            state = ResultState.Success(TestOutcome(PreviewData.cognitive(Status.GREEN), PREVIEW_PVT)),
            onDone = {},
        )
    }
}

@WearPreview
@Composable
private fun ResultEscalatePreview() {
    WearScreenPreview {
        ResultContent(
            state = ResultState.Success(
                TestOutcome(PreviewData.cognitive(Status.RED, escalate = true), PREVIEW_PVT),
            ),
            onDone = {},
        )
    }
}

@WearPreview
@Composable
private fun ResultFailurePreview() {
    WearScreenPreview {
        ResultContent(state = ResultState.Failure(stringResource(R.string.session_save_failed)), onDone = {})
    }
}

@WearPreview
@Composable
private fun ResultLoadingPreview() {
    WearScreenPreview { ResultContent(state = ResultState.Loading, onDone = {}) }
}
