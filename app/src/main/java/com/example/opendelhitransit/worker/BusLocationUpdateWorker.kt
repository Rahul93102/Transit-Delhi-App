package com.example.opendelhitransit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.opendelhitransit.data.repository.BusRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BusLocationUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val busRepository: BusRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "BusLocationUpdateWorker"
        private const val MAX_RETRY_COUNT = 3
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Refreshing bus locations")
        
        return try {
            val success = busRepository.refreshBusLocations()
            
            if (success) {
                Log.d(TAG, "Successfully refreshed bus locations")
                Result.success()
            } else {
                val runAttemptCount = runAttemptCount
                Log.w(TAG, "Failed to refresh bus locations (attempt $runAttemptCount)")
                
                if (runAttemptCount < MAX_RETRY_COUNT) {
                    Result.retry()
                } else {
                    // After MAX_RETRY_COUNT attempts, we'll still count it as success
                    // to avoid stopping the periodic work completely
                    Log.e(TAG, "Max retry count reached, giving up this cycle")
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing bus locations", e)
            
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                // After MAX_RETRY_COUNT attempts, we'll still count it as success
                // to avoid stopping the periodic work completely
                Log.e(TAG, "Max retry count reached after exception, giving up this cycle")
                Result.success()
            }
        }
    }
} 