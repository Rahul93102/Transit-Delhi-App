package com.example.opendelhitransit.data.repository

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.opendelhitransit.data.model.MetroLine
import com.example.opendelhitransit.data.model.MetroPath
import com.example.opendelhitransit.data.model.MetroStation
import com.example.opendelhitransit.data.native.MetroNativeLib
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class to manage interactions with the native metro code
 */
@Singleton
class MetroRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val metroNativeLib = MetroNativeLib()
    private val TAG = "MetroRepository"
    
    /**
     * Maps station IDs to station objects for quick lookup
     */
    private val stationMap = mutableMapOf<Int, MetroStation>()
    
    /**
     * Maps line IDs to line objects for quick lookup
     */
    private val lineMap = mutableMapOf<Int, MetroLine>()
    
    /**
     * Initialize the metro graph with data from the GTFS files
     */
    suspend fun initializeMetroGraph(assetManager: AssetManager): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val result = metroNativeLib.initMetroGraph(assetManager)
                Log.d(TAG, "Metro graph initialization result: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing metro graph", e)
                false
            }
        }
    }
    
    /**
     * Get all station names from the metro graph
     */
    suspend fun getAllStationNames(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val names = metroNativeLib.getAllStationNames()
                Log.d(TAG, "Got ${names.size} station names")
                names.toList()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting station names", e)
                emptyList()
            }
        }
    }
    
    /**
     * Find the shortest path between two stations by name
     */
    suspend fun findShortestPath(sourceStation: String, targetStation: String): MetroPath {
        return withContext(Dispatchers.IO) {
            try {
                metroNativeLib.findShortestPathByNames(sourceStation, targetStation) ?: MetroPath()
            } catch (e: Exception) {
                Log.e(TAG, "Error finding shortest path", e)
                MetroPath()
            }
        }
    }
    
    /**
     * Find the fastest path between two stations by name
     */
    suspend fun findFastestPath(sourceStation: String, targetStation: String): MetroPath {
        return withContext(Dispatchers.IO) {
            try {
                metroNativeLib.findFastestPathByNames(sourceStation, targetStation) ?: MetroPath()
            } catch (e: Exception) {
                Log.e(TAG, "Error finding fastest path", e)
                MetroPath()
            }
        }
    }
    
    /**
     * Release native resources when the repository is no longer needed
     */
    suspend fun releaseResources(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                metroNativeLib.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing resources", e)
                false
            }
        }
    }
} 