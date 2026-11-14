package com.jintonic.app.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.ui.components.AppEdgeButton
import com.jintonic.app.ui.components.Hint
import com.jintonic.app.ui.theme.FfdColors

// Explains the app and requests the heart-rate permission with context on first run.
@Composable
fun OnboardingScreen(
    heartRatePermission: String,
    onPermissionResult: (Boolean) -> Unit,
    onContinue: () -> Unit,
) {
    val listState = rememberScalingLazyListState()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        onPermissionResult(result[heartRatePermission] == true)
        onContinue()
    }
    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            AppEdgeButton(onClick = { launcher.launch(arrayOf(heartRatePermission, Manifest.permission.POST_NOTIFICATIONS)) }) {
                Text("Consenti")
            }
        },
    ) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "jintonic",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FfdColors.Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            item { Hint("Misura lucidità e parametri vitali per dirti se sei in grado di lavorare.") }
            item { Hint("Serve l'accesso alla frequenza cardiaca. Se neghi, viene simulata.", color = FfdColors.AccentSoft) }
        }
    }
}
