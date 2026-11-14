package com.jintonic.app.ui.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jintonic.app.data.haptics.Haptics
import com.jintonic.app.vitals.BreathingGuide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BreathPhase { IDLE, INHALE, EXHALE }

// Guided breathing countermeasure: cadence from the engine, pulses on the wrist.
class EmergencyViewModel(private val haptics: Haptics) : ViewModel() {

    private val _phase = MutableStateFlow(BreathPhase.IDLE)
    val phase: StateFlow<BreathPhase> = _phase.asStateFlow()

    private var job: Job? = null

    fun startBreathing(guide: BreathingGuide = BreathingGuide()) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            while (isActive) {
                _phase.value = BreathPhase.INHALE
                haptics.inhale()
                delay(guide.inhaleSec * MS_PER_SEC)
                _phase.value = BreathPhase.EXHALE
                haptics.exhale()
                delay(guide.exhaleSec * MS_PER_SEC)
            }
        }
    }

    fun stopBreathing() {
        job?.cancel()
        _phase.value = BreathPhase.IDLE
    }

    override fun onCleared() {
        stopBreathing()
    }

    private companion object {
        const val MS_PER_SEC = 1_000L
    }
}
