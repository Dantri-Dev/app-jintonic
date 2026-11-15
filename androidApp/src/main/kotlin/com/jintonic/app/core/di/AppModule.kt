package com.jintonic.app.core.di

import android.os.SystemClock
import com.jintonic.app.assessment.DefaultFitForDutyEngine
import com.jintonic.app.assessment.FitForDutyEngine
import com.jintonic.app.data.SessionStore
import com.jintonic.app.data.sensors.HeartRateSource
import com.jintonic.app.data.sensors.VitalsMonitor
import com.jintonic.app.presentation.home.HomeViewModel
import com.jintonic.app.presentation.test_flow.TestFlowViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Manual Koin wiring (no annotations, no codegen).
val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { SessionStore(androidContext()) }
    single { HeartRateSource(androidContext()) }
    single { VitalsMonitor(heartRate = get(), scope = get(), clock = SystemClock::elapsedRealtime) }
    // Fresh seed per session: reproducible within a run, varied across runs.
    factory<FitForDutyEngine> { DefaultFitForDutyEngine(seed = System.currentTimeMillis()) }

    viewModel { HomeViewModel(store = get()) }
    viewModel { TestFlowViewModel(store = get(), vitals = get(), engineFactory = { get() }, context = androidContext()) }
}
