package com.jintonic.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jintonic.app.assessment.FitForDutyEngine
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthEvaluation
import com.jintonic.app.data.SessionRecord
import com.jintonic.app.data.SessionStore
import com.jintonic.app.data.sensors.VitalsMonitor
import com.jintonic.app.pvt.Status
import com.jintonic.app.scoring.CognitiveScoring
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HomeUi(
    val readiness: Int,
    val status: Status, // same bands as the mockup legend, applied to the readiness score
    val streakDays: Int,
    val pendingSync: Int,
    val hasSessions: Boolean,
)

class HomeViewModel(
    store: SessionStore,
    vitals: VitalsMonitor,
    engineFactory: () -> FitForDutyEngine,
) : ViewModel() {

    val state: StateFlow<ResultState<HomeUi>> =
        combine(store.sessions, store.manual, vitals.state) { sessions, manual, snapshot ->
            val health = HealthEvaluation.evaluate(engineFactory(), snapshot, manual, sessions.lastOrNull())
            ResultState.Success(
                HomeUi(
                    readiness = health.assessment.readinessScore,
                    status = CognitiveScoring.status(health.assessment.readinessScore),
                    streakDays = streak(sessions),
                    pendingSync = sessions.count { it.pendingSync },
                    hasSessions = sessions.isNotEmpty(),
                ),
            ) as ResultState<HomeUi>
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ResultState.Idle)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

// Consecutive days with at least one session, ending today (or yesterday if not tested yet today).
internal fun streak(sessions: List<SessionRecord>, today: LocalDate = LocalDate.now()): Int {
    val days = sessions.map { Instant.ofEpochMilli(it.timestampMs).atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
    var cursor = if (today in days) today else today.minusDays(1)
    var count = 0
    while (cursor in days) {
        count++
        cursor = cursor.minusDays(1)
    }
    return count
}
