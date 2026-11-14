package com.jintonic.app

import android.app.Application
import com.jintonic.app.core.di.appModule
import com.jintonic.app.data.reminders.Reminders
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class JintonicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@JintonicApplication)
            modules(appModule)
        }
        Reminders.createChannel(this)
        Reminders.scheduleNext(this)
    }
}
