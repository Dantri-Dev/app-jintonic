package com.jintonic.app.presentation.test_flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.FailureConfirmationDialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SuccessConfirmationDialog
import androidx.wear.compose.material3.confirmationDialogCurvedText
import com.jintonic.app.R
import com.jintonic.app.assessment.CognitiveAssessment
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.presentation.theme.AppColors
import com.jintonic.app.pvt.PvtResult
import com.jintonic.app.pvt.Status

@Composable
fun ResultScreen(vm: TestFlowViewModel, onDone: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    ResultContent(state = state, onDone = onDone)
}

// Native confirmation: a reassuring white success, or a red at-risk failure.
@Composable
private fun ResultContent(state: ResultState<TestOutcome>, onDone: () -> Unit) {
    when (state) {
        is ResultState.Success -> {
            val cognitive = state.data.cognitive
            val atRisk = cognitive.status == Status.RED || cognitive.escalateToSurgeon
            if (atRisk) {
                FailureResult(text = stringResource(R.string.result_at_risk), onDone = onDone)
            } else {
                SuccessResult(text = stringResource(R.string.result_ok), onDone = onDone)
            }
        }

        is ResultState.Failure -> FailureResult(text = state.message, onDone = onDone)

        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SuccessResult(text: String, onDone: () -> Unit) {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    SuccessConfirmationDialog(
        visible = true,
        onDismissRequest = onDone,
        curvedText = { confirmationDialogCurvedText(text, style) },
        colors = ConfirmationDialogDefaults.colors(
            iconColor = AppColors.Background,
            iconContainerColor = AppColors.OnBackground,
            textColor = AppColors.OnBackground,
        ),
    )
}

@Composable
private fun FailureResult(text: String, onDone: () -> Unit) {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    FailureConfirmationDialog(
        visible = true,
        onDismissRequest = onDone,
        curvedText = { confirmationDialogCurvedText(text, style) },
        colors = ConfirmationDialogDefaults.colors(
            iconColor = AppColors.OnBackground,
            iconContainerColor = AppColors.Error,
            textColor = AppColors.OnBackground,
        ),
    ) {
        Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = null)
    }
}

private val PREVIEW_PVT = PvtResult(medianRtMs = 280.0, meanSpeed = 3.6, lapses = 1, falseStarts = 0, validTrials = 20)

private fun previewOutcome(status: Status, escalate: Boolean = false) = TestOutcome(
    cognitive = CognitiveAssessment(
        score = if (status == Status.RED) 38 else 86,
        status = status,
        perceivedMismatch = false,
        sleepWarning = false,
        advice = emptyList(),
        escalateToSurgeon = escalate,
    ),
    result = PREVIEW_PVT,
)

@WearPreview
@Composable
private fun ResultSuccessPreview() {
    WearScreenPreview { ResultContent(state = ResultState.Success(previewOutcome(Status.GREEN)), onDone = {}) }
}

@WearPreview
@Composable
private fun ResultFailurePreview() {
    WearScreenPreview {
        ResultContent(state = ResultState.Success(previewOutcome(Status.RED, escalate = true)), onDone = {})
    }
}
