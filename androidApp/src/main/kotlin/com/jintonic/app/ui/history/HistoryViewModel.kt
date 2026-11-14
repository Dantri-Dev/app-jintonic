package com.jintonic.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.data.SessionRecord
import com.jintonic.app.data.SessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(store: SessionStore) : ViewModel() {

    // Newest first.
    val state: StateFlow<ResultState<List<SessionRecord>>> = store.sessions
        .map { ResultState.Success(it.asReversed()) as ResultState<List<SessionRecord>> }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ResultState.Idle)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
