package com.example.opendelhitransit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.opendelhitransit.data.repository.BusRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class FrequentBusUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val busRepository: BusRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "FrequentBusUpdateWorker"
        private const val UPDATE_INTERVAL_SECONDS = 60L // 60 seconds interval
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting frequent bus location update (${System.currentTimeMillis()})")

        try {
            val success = busRepository.refreshBusLocations()

            if (success) {
                Log.i(TAG, "Successfully refreshed bus locations")
                return Result.success()
            } else {
                val runAttemptCount = runAttemptCount
                Log.w(TAG, "Failed to refresh bus locations (attempt $runAttemptCount)")

                if (runAttemptCount < MAX_RETRY_COUNT) {
                    return Result.retry()
                } else {
                    // After MAX_RETRY_COUNT attempts, schedule next update anyway
                    Log.e(TAG, "Max retry count reached, scheduling next update")
                    return Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing bus locations: ${e.message}", e)

            if (runAttemptCount < MAX_RETRY_COUNT) {
                return Result.retry()
            } else {
                // After MAX_RETRY_COUNT attempts, schedule next update anyway
                Log.e(TAG, "Max retry count after exception, scheduling next update")
                return Result.success()
            }
        }
    }

//    private fun scheduleNextUpdate() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        val workRequest = OneTimeWorkRequestBuilder<FrequentBusUpdateWorker>()
//            .setConstraints(constraints)
//            .setInitialDelay(UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS)
//            .addTag("frequent_transit_updates")
//            .build()
//
//        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
//            "FrequentBusUpdateWork",
//            ExistingWorkPolicy.REPLACE,
//            workRequest
//        )
//
//        Log.d(TAG, "Next bus location update scheduled in $UPDATE_INTERVAL_SECONDS seconds")
//    }
}
