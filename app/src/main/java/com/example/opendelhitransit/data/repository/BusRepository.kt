package com.example.opendelhitransit.data.repository

import android.util.Log
import com.example.opendelhitransit.data.local.BusLocationDao
import com.example.opendelhitransit.data.model.BusLocation
import com.example.opendelhitransit.data.network.TransitApiService
import com.example.opendelhitransit.data.util.GtfsRtUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class BusRepository @Inject constructor(
    private val transitApiService: TransitApiService,
    private val busLocationDao: BusLocationDao
) {
    private val TAG = "BusRepository"
    
    fun getAllBusLocations(): Flow<List<BusLocation>> {
        return busLocationDao.getAllBusLocations()
    }
    
    fun getBusLocationsByRoute(routeId: String): Flow<List<BusLocation>> {
        return busLocationDao.getBusLocationsByRoute(routeId)
    }
    
    fun getBusLocationByVehicleId(vehicleId: String): Flow<BusLocation?> {
        return busLocationDao.getBusLocationByVehicleId(vehicleId)
    }
    
    fun getAllRouteIds(): Flow<List<String>> {
        return busLocationDao.getAllRouteIds()
    }
    
    suspend fun refreshBusLocations(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching bus locations from API")
            val response = transitApiService.getVehiclePositions(TransitApiService.API_KEY)
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    // Parse the Protocol Buffer response
                    val busLocations = GtfsRtUtil.parseVehiclePositions(responseBody)
                    
                    Log.d(TAG, "Parsed ${busLocations.size} bus locations from Protocol Buffers")
                    
                    if (busLocations.isNotEmpty()) {
                        // Clean old entries that are older than 30 minutes
                        val thirtyMinutesAgo = Date(System.currentTimeMillis() - 30 * 60 * 1000)
                        busLocationDao.deleteOldEntries(thirtyMinutesAgo)
                        
                        // Insert new data
                        busLocationDao.insertAll(busLocations)
                        Log.d(TAG, "Inserted ${busLocations.size} bus locations into database")
                        return@withContext true
                    } else {
                        Log.w(TAG, "No valid bus locations found in the response")
                        return@withContext false
                    }
                } else {
                    Log.e(TAG, "Response body is null")
                    return@withContext false
                }
            } else {
                Log.e(TAG, "API error: ${response.code()} ${response.message()}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }
    
    suspend fun getDataAgeMinutes(): Long = withContext(Dispatchers.IO) {
        val latestTimestamp = busLocationDao.getLatestTimestamp() ?: 0
        if (latestTimestamp == 0L) return@withContext Long.MAX_VALUE
        
        val currentTime = System.currentTimeMillis() / 1000
        val diffSeconds = currentTime - latestTimestamp
        return@withContext diffSeconds / 60
    }
} 