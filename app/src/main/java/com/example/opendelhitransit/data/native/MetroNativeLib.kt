package com.example.opendelhitransit.data.native

import android.content.res.AssetManager
import android.util.Log
import com.example.opendelhitransit.data.model.MetroPath

/**
 * Native library interface for Delhi Metro pathfinding algorithms.
 * This class provides native methods to initialize the metro graph and find paths.
 */
class MetroNativeLib {
    companion object {
        private const val TAG = "MetroNativeLib"
        
        // Load the native library
        init {
            System.loadLibrary("metro_path_finder")
            Log.d(TAG, "Native library loaded successfully")
        }
    }
    
    /**
     * Initialize the metro graph with data from the GTFS files
     * @param assetManager Asset manager to access GTFS files
     * @return true if initialization successful, false otherwise
     */
    external fun initMetroGraphNative(assetManager: AssetManager): Boolean
    
    /**
     * Find the shortest path between two stations by their IDs
     * @param sourceId Source station ID
     * @param targetId Target station ID
     * @return MetroPath object containing the path details
     */
    external fun findShortestPathNative(sourceId: Int, targetId: Int): MetroPath?
    
    /**
     * Find the fastest path between two stations by their IDs
     * @param sourceId Source station ID
     * @param targetId Target station ID
     * @return MetroPath object containing the path details
     */
    external fun findFastestPathNative(sourceId: Int, targetId: Int): MetroPath?
    
    /**
     * Find the shortest path between two stations by their names
     * @param sourceName Source station name
     * @param targetName Target station name
     * @return MetroPath object containing the path details
     */
    external fun findShortestPathByNamesNative(sourceName: String, targetName: String): MetroPath?
    
    /**
     * Find the fastest path between two stations by their names
     * @param sourceName Source station name
     * @param targetName Target station name
     * @return MetroPath object containing the path details
     */
    external fun findFastestPathByNamesNative(sourceName: String, targetName: String): MetroPath?
    
    /**
     * Get all station names in the metro network
     * @return Array of station names
     */
    external fun getAllStationNamesNative(): Array<String>
    
    /**
     * Release native resources
     */
    external fun releaseResources(): Boolean
    
    // Public methods that call the native functions
    
    /**
     * Initialize the metro graph
     */
    fun initMetroGraph(assetManager: AssetManager): Boolean {
        return initMetroGraphNative(assetManager)
    }
    
    /**
     * Find shortest path by station IDs
     */
    fun findShortestPath(sourceId: Int, targetId: Int): MetroPath? {
        return findShortestPathNative(sourceId, targetId)
    }
    
    /**
     * Find fastest path by station IDs
     */
    fun findFastestPath(sourceId: Int, targetId: Int): MetroPath? {
        return findFastestPathNative(sourceId, targetId)
    }
    
    /**
     * Find shortest path by station names
     */
    fun findShortestPathByNames(sourceName: String, targetName: String): MetroPath? {
        return findShortestPathByNamesNative(sourceName, targetName)
    }
    
    /**
     * Find fastest path by station names
     */
    fun findFastestPathByNames(sourceName: String, targetName: String): MetroPath? {
        return findFastestPathByNamesNative(sourceName, targetName)
    }
    
    /**
     * Get all station names
     */
    fun getAllStationNames(): Array<String> {
        return getAllStationNamesNative()
    }
    
    /**
     * Release resources
     */
    fun release(): Boolean {
        return releaseResources()
    }
} 