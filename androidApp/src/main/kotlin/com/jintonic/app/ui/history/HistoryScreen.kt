package com.jintonic.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.SessionRecord
import com.jintonic.app.pvt.Status
import com.jintonic.app.ui.components.EmptyState
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.color
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm")

@Composable
fun HistoryScreen(vm: HistoryViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val sessions = (state as? ResultState.Success)?.data.orEmpty()
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = contentPadding) {
            item { ListHeader { Text("Storico") } }
            if (sessions.isEmpty()) {
                item { EmptyState(Icons.Outlined.History, "Nessuna sessione. Fai il primo test dalla Home.") }
            }
            items(sessions) { SessionCard(it) }
        }
    }
}

@Composable
private fun SessionCard(session: SessionRecord) {
    val status = runCatching { Status.valueOf(session.status) }.getOrDefault(Status.YELLOW)
    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).background(status.color(), CircleShape))
            Column {
                Text(
                    text = "Cognitivo ${session.cognitiveScore}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                val time = Instant.ofEpochMilli(session.timestampMs).atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
                Text(time, style = MaterialTheme.typography.bodySmall, color = FfdColors.TextSecondary)
                session.readinessScore?.let {
                    Text("Readiness $it", style = MaterialTheme.typography.bodySmall, color = FfdColors.TextSecondary)
                }
                if (session.escalated) {
                    Text("Escalation", style = MaterialTheme.typography.bodySmall, color = FfdColors.AccentStrong)
                }
                if (session.pendingSync) {
                    Text("In coda per la Terra", style = MaterialTheme.typography.bodySmall, color = FfdColors.AccentSoft)
                }
            }
        }
    }
}
