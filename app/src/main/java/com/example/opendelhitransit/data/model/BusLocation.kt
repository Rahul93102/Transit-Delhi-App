package com.example.opendelhitransit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents the real-time location of a bus.
 */
@Entity(tableName = "bus_locations")
data class BusLocation(
    @PrimaryKey
    val vehicleId: String,
    
    val routeId: String,
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val speed: Float,
    val timestamp: Date,
    
    // Created locally to track when data was last updated
    val lastUpdated: Date = Date()
) 