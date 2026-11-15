package com.jintonic.app.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.jintonic.app.R
import com.jintonic.app.core.compose.WearPreview
import com.jintonic.app.core.compose.WearScreenPreview
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.pvt.Status
import com.jintonic.app.ui.components.AppButton
import com.jintonic.app.ui.theme.AppColors
import com.jintonic.app.ui.theme.AppSizes
import org.koin.compose.viewmodel.koinViewModel

private const val SCORE_MAX = 100f
private const val TRACK_ALPHA = 0.2f
// Open ring with the gap centred at the top (270°), active arc runs clockwise around it.
private const val RING_START_ANGLE = 290f
private const val RING_END_ANGLE = 250f
private val RING_INSET = 4.dp
private const val BUTTON_WIDTH_FRACTION = 0.62f

@Composable
fun HomeScreen(
    heartRatePermission: String,
    hasPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onStartTest: () -> Unit,
    vm: HomeViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var granted by remember { mutableStateOf(hasPermission) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val ok = result[heartRatePermission] == true
        granted = ok
        onPermissionResult(ok)
    }
    HomeContent(
        ui = (state as? ResultState.Success)?.data,
        ready = granted,
        onGrant = { launcher.launch(arrayOf(heartRatePermission, Manifest.permission.POST_NOTIFICATIONS)) },
        onStartTest = onStartTest,
    )
}

// Single hero: readiness ring with the score centred and one call-to-action.
// Until the sensor permission is granted the ring stays at 0% and the action grants it; then it starts the test.
@Composable
private fun HomeContent(ui: HomeUi?, ready: Boolean, onGrant: () -> Unit, onStartTest: () -> Unit) {
    val score = if (ready) ui?.readiness ?: 0 else 0
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { score / SCORE_MAX },
            modifier = Modifier.fillMaxSize().padding(RING_INSET),
            startAngle = RING_START_ANGLE,
            endAngle = RING_END_ANGLE,
            strokeWidth = AppSizes.RingStroke,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = AppColors.OnBackground,
                trackColor = AppColors.OnBackground.copy(alpha = TRACK_ALPHA),
            ),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_percent, score),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.OnBackground,
            )
            Text(
                text = stringResource(R.string.home_readiness),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = AppColors.OnBackground,
                modifier = Modifier.padding(top = 2.dp),
            )
            AppButton(
                onClick = if (ready) onStartTest else onGrant,
                modifier = Modifier.fillMaxWidth(BUTTON_WIDTH_FRACTION).padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.OnBackground,
                    contentColor = AppColors.Background,
                ),
            ) {
                Text(
                    text = stringResource(if (ready) R.string.action_do_test else R.string.action_allow),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@WearPreview
@Composable
private fun HomeNotReadyPreview() {
    WearScreenPreview { HomeContent(ui = null, ready = false, onGrant = {}, onStartTest = {}) }
}

@WearPreview
@Composable
private fun HomeReadyPreview() {
    WearScreenPreview {
        HomeContent(
            ui = HomeUi(readiness = 80, status = Status.GREEN, streakDays = 5, pendingSync = 0, hasSessions = true),
            ready = true,
            onGrant = {},
            onStartTest = {},
        )
    }
}
