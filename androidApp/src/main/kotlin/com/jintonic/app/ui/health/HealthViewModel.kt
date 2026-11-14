package com.jintonic.app.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jintonic.app.assessment.FitForDutyEngine
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.HealthEvaluation
import com.jintonic.app.data.HealthView
import com.jintonic.app.data.ManualEntry
import com.jintonic.app.data.SessionStore
import com.jintonic.app.data.sensors.VitalsMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Shared by Parameters, Body map and Emergency: same live health picture.
class HealthViewModel(
    private val store: SessionStore,
    private val vitals: VitalsMonitor,
    engineFactory: () -> FitForDutyEngine,
) : ViewModel() {

    val state: StateFlow<ResultState<HealthView>> =
        combine(store.sessions, store.manual, vitals.state) { sessions, manual, snapshot ->
            ResultState.Success(HealthEvaluation.evaluate(engineFactory(), snapshot, manual, sessions.lastOrNull()))
                as ResultState<HealthView>
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ResultState.Idle)

    fun updateManual(transform: (ManualEntry) -> ManualEntry) {
        viewModelScope.launch { store.saveManual(transform(store.manual.first())) }
    }

    fun setSpo2Crash(active: Boolean) = vitals.setCrash(active)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
