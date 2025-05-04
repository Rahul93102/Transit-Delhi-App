package com.example.opendelhitransit

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.opendelhitransit.data.ThemePreferences
import com.example.opendelhitransit.worker.BusLocationUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OpenDelhiTransitApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    // Theme preferences
    @Inject
    lateinit var themePreferences: ThemePreferences
    
    companion object {
        // Static instance for easy access to theme preferences
        private lateinit var appInstance: OpenDelhiTransitApp
        
        fun getInstance(): OpenDelhiTransitApp {
            return appInstance
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        appInstance = this
        
        // Schedule periodic bus location updates
        schedulePeriodicBusLocationUpdates()
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
    
    private fun schedulePeriodicBusLocationUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<BusLocationUpdateWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
        .setConstraints(constraints)
        .addTag("transit_updates")
        .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BusLocationUpdateWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
} 