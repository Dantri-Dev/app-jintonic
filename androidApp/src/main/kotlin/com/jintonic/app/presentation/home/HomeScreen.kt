package com.jintonic.app.presentation.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.jintonic.app.R
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.presentation.components.AppButton
import com.jintonic.app.presentation.components.debouncedHaptic
import com.jintonic.app.presentation.components.pressBounceObserving
import com.jintonic.app.presentation.theme.AppColors
import com.jintonic.app.presentation.theme.AppSizes
import com.jintonic.app.presentation.theme.RammettoOne
import org.koin.compose.viewmodel.koinViewModel

private const val SCORE_MAX = 100f
// Open ring with the gap centred at the top (270°), active arc runs clockwise around it.
private const val RING_START_ANGLE = 290f
private const val RING_END_ANGLE = 250f
private val RING_INSET = 4.dp
private const val BUTTON_WIDTH_FRACTION = 0.52f
// At 100% the score is three digits; shrink the font by 10% so it never touches the edges.
private const val FULL_SCORE_FONT_SCALE = 0.9f

@Composable
fun HomeScreen(
    permissions: Array<String>,
    sensorPermission: String,
    hasPermission: () -> Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onEnterPreflight: () -> Unit,
    onEnterTest: () -> Unit,
    vm: HomeViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    // Read the live grant state on entry and on every resume, so returning from the flow
    // (or from system settings) never shows a stale "missing permissions".
    var ready by remember { mutableStateOf(hasPermission()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) ready = hasPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        ready = permissions.all { result[it] == true }
        onPermissionResult(result[sensorPermission] == true)
    }
    HomeContent(
        ui = (state as? ResultState.Success)?.data,
        ready = ready,
        onGrant = { launcher.launch(permissions) },
        onEnterPreflight = onEnterPreflight,
        onEnterTest = onEnterTest,
    )
}

// Single hero. Missing permissions: no ring, 0% and a grant action. Ready: readiness ring, score and the test action.
@Composable
private fun HomeContent(
    ui: HomeUi?,
    ready: Boolean,
    onGrant: () -> Unit,
    onEnterPreflight: () -> Unit,
    onEnterTest: () -> Unit,
) {
    var askNewValues by remember { mutableStateOf(false) }
    val score = if (ready) ui?.readiness ?: 0 else 0
    val label = when {
        !ready -> stringResource(R.string.home_permissions_missing)
        ui?.hasResult == true -> stringResource(R.string.home_readiness)
        else -> stringResource(R.string.home_readiness_idle)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Mount the ring only once the score is known, so the indicator composes with its real value.
        if (ready && ui != null) {
            CircularProgressIndicator(
                progress = { score / SCORE_MAX },
                modifier = Modifier.fillMaxSize().padding(RING_INSET),
                startAngle = RING_START_ANGLE,
                endAngle = RING_END_ANGLE,
                strokeWidth = AppSizes.RingStroke,
                colors = ProgressIndicatorDefaults.colors(
                    indicatorColor = AppColors.OnBackground,
                    trackColor = AppColors.TrackDim,
                ),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ScorePercent(score)
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = AppColors.OnBackground,
                modifier = Modifier.padding(top = 2.dp),
            )
            AppButton(
                onClick = if (ready) ({ askNewValues = true }) else onGrant,
                modifier = Modifier.fillMaxWidth(BUTTON_WIDTH_FRACTION).padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.OnBackground,
                    contentColor = AppColors.Background,
                ),
            ) {
                Text(
                    text = stringResource(if (ready) R.string.action_do_test else R.string.action_grant),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    NewValuesDialog(
        show = askNewValues,
        onDismiss = { askNewValues = false },
        onConfirm = { askNewValues = false; onEnterPreflight() },
        onSkip = { askNewValues = false; onEnterTest() },
    )
}

// The big readiness figure; at full score the three digits shrink a touch so they never touch the edges.
@Composable
private fun ScorePercent(score: Int) {
    val style = MaterialTheme.typography.displayMedium
    Text(
        text = stringResource(R.string.home_percent, score),
        style = style,
        fontSize = if (score >= SCORE_MAX.toInt()) style.fontSize * FULL_SCORE_FONT_SCALE else style.fontSize,
        fontFamily = RammettoOne,
        color = AppColors.OnBackground,
    )
}

// Asks whether to set new parameters before the test: confirm opens the pre-flight, dismiss goes straight to the test.
@Composable
private fun NewValuesDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        visible = show,
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Error, contentDescription = null) },
        title = { Text(stringResource(R.string.dialog_new_values), textAlign = TextAlign.Center) },
        confirmButton = {
            AlertDialogDefaults.ConfirmButton(
                onClick = debouncedHaptic(onConfirm),
                modifier = Modifier.pressBounceObserving(),
            )
        },
        dismissButton = {
            AlertDialogDefaults.DismissButton(
                onClick = debouncedHaptic(onSkip),
                modifier = Modifier.pressBounceObserving(),
            )
        },
    )
}

@WearPreview
@Composable
private fun HomeNotReadyPreview() {
    WearScreenPreview { HomeContent(ui = null, ready = false, onGrant = {}, onEnterPreflight = {}, onEnterTest = {}) }
}

@WearPreview
@Composable
private fun HomeReadyPreview() {
    WearScreenPreview {
        HomeContent(
            ui = HomeUi(readiness = 80, hasResult = true),
            ready = true,
            onGrant = {},
            onEnterPreflight = {},
            onEnterTest = {},
        )
    }
}

@WearPreview
@Composable
private fun HomeEmptyPreview() {
    WearScreenPreview {
        HomeContent(
            ui = HomeUi(readiness = 0, hasResult = false),
            ready = true,
            onGrant = {},
            onEnterPreflight = {},
            onEnterTest = {},
        )
    }
}
