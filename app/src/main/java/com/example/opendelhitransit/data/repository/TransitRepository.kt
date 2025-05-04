package com.example.opendelhitransit.data.repository

import android.util.Log
import com.example.opendelhitransit.data.model.VehicleData
import com.example.opendelhitransit.data.network.TransitApiService
import com.example.opendelhitransit.util.BusLocation
import com.example.opendelhitransit.util.GtfsRtUtil
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

@Singleton
class TransitRepository @Inject constructor(
    private val apiService: TransitApiService
) {
    private val apiKey = TransitApiService.API_KEY
    private val TAG = "TransitRepository"

    suspend fun getAllVehicles(limit: Int = 100): List<VehicleData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Making API request to get vehicle positions")
            val response = apiService.getVehiclePositions(apiKey)

            if (!response.isSuccessful) {
                Log.e(TAG, "API request failed with code: ${response.code()}, message: ${response.message()}")
                throw Exception("API returned error code ${response.code()}: ${response.message()}")
            }

            val responseBody = response.body()
            if (responseBody == null) {
                Log.e(TAG, "Response body is null")
                throw Exception("API returned empty response")
            }

            Log.d(TAG, "Received response, parsing protobuf data")
            val busLocations = GtfsRtUtil.parseVehiclePositions(responseBody)
            Log.d(TAG, "Parsed ${busLocations.size} vehicles from GTFS-RT")

            if (busLocations.isEmpty()) {
                Log.e(TAG, "No vehicles found in feed")
                return@withContext emptyList()
            }

            val vehicleList = busLocations.take(limit).map { bus ->
                VehicleData(
                    id = bus.vehicleId,
                    routeId = bus.routeId,
                    tripId = bus.tripId,
                    startTime = null,
                    startDate = null,
                    latitude = bus.latitude.toFloat(),
                    longitude = bus.longitude.toFloat(),
                    speed = bus.speed,
                    timestamp = bus.timestamp
                )
            }

            return@withContext vehicleList

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle data: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // Function to find buses near the given location
    suspend fun getVehiclesNear(
        location: LatLng,
        radiusKm: Double = 2.0,
        limit: Int = 10
    ): List<VehicleData> = withContext(Dispatchers.IO) {
        val allVehicles = getAllVehicles(100)

        val nearbyVehicles = allVehicles
            .filter { vehicle ->
                vehicle.getLatLng()?.let { vehicleLocation ->
                    calculateDistance(location, vehicleLocation) <= radiusKm
                } ?: false
            }
            .sortedBy { vehicle ->
                vehicle.getLatLng()?.let { vehicleLocation ->
                    calculateDistance(location, vehicleLocation)
                } ?: Double.MAX_VALUE
            }
            .take(limit)

        return@withContext nearbyVehicles
    }

    // Calculate distance between two LatLng points using Haversine formula
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371.0 // Radius of Earth in kilometers

        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val lon1Rad = Math.toRadians(point1.longitude)
        val lon2Rad = Math.toRadians(point2.longitude)

        val x = (lon2Rad - lon1Rad) * cos((lat1Rad + lat2Rad) / 2)
        val y = lat2Rad - lat1Rad
        return earthRadius * Math.sqrt(x * x + y * y)
    }
}