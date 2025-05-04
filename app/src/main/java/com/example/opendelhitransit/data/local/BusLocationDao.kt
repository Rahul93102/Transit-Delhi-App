package com.example.opendelhitransit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.opendelhitransit.data.model.BusLocation
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BusLocationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(buses: List<BusLocation>)
    
    @Query("SELECT * FROM bus_locations")
    fun getAllBusLocations(): Flow<List<BusLocation>>
    
    @Query("SELECT * FROM bus_locations WHERE routeId = :routeId")
    fun getBusLocationsByRoute(routeId: String): Flow<List<BusLocation>>
    
    @Query("SELECT * FROM bus_locations WHERE vehicleId = :vehicleId")
    fun getBusLocationByVehicleId(vehicleId: String): Flow<BusLocation?>
    
    @Query("SELECT DISTINCT routeId FROM bus_locations")
    fun getAllRouteIds(): Flow<List<String>>
    
    @Query("DELETE FROM bus_locations")
    suspend fun deleteAll()
    
    @Query("DELETE FROM bus_locations WHERE timestamp < :timestamp")
    suspend fun deleteOldEntries(timestamp: Date)
    
    @Query("SELECT MAX(timestamp) FROM bus_locations")
    suspend fun getLatestTimestamp(): Long?
} 