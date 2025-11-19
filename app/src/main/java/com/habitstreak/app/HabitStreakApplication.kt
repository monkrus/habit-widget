package com.habitstreak.app

import android.app.Application
import timber.log.Timber

class HabitStreakApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging (only in debug builds)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
