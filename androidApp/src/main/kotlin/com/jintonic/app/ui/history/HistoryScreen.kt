package com.jintonic.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.SessionRecord
import com.jintonic.app.pvt.Status
import com.jintonic.app.ui.components.BodyText
import com.jintonic.app.ui.components.TopLabel
import com.jintonic.app.ui.theme.FfdType
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
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    ScreenScaffold(scrollState = listState, timeText = { TopLabel("Storico") }) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = it) {
            if (sessions.isEmpty()) {
                item { BodyText("Nessuna sessione. Fai il primo test dalla Home.") }
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
                    fontSize = FfdType.Title,
                    fontWeight = FontWeight.SemiBold,
                )
                val time = Instant.ofEpochMilli(session.timestampMs).atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
                Text(time, fontSize = FfdType.Body, color = FfdColors.TextSecondary)
                session.readinessScore?.let { Text("Readiness $it", fontSize = FfdType.Body, color = FfdColors.TextSecondary) }
                if (session.escalated) Text("Escalation", fontSize = FfdType.Body, color = FfdColors.Red)
                if (session.pendingSync) Text("In coda per la Terra", fontSize = FfdType.Body, color = FfdColors.Yellow)
            }
        }
    }
}
