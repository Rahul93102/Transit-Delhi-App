package com.example.opendelhitransit.viewmodel

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.model.MetroLine
import com.example.opendelhitransit.data.model.MetroPath
import com.example.opendelhitransit.data.model.MetroStation
import com.example.opendelhitransit.data.repository.MetroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * ViewModel for the Metro Path Finder app
 */
@HiltViewModel
class MetroViewModel @Inject constructor(
    private val repository: MetroRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MetroUiState())
    val uiState: StateFlow<MetroUiState> = _uiState.asStateFlow()

    private val TAG = "MetroViewModel"
    
    // Maps to store station and line data from GTFS files
    private val stationMap = mutableMapOf<Int, MetroStation>()
    private val lineMap = mutableMapOf<Int, MetroLine>()

    /**
     * Initialize the metro graph with data from GTFS files
     */
    fun initializeMetroGraph(assetManager: AssetManager) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                
                Log.d(TAG, "Starting metro data initialization")
                
                // Load station data from stops.txt
                val stationsLoaded = loadStationsFromGTFS(assetManager)
                Log.d(TAG, "Loaded $stationsLoaded stations from GTFS data")
                
                // Load line data from routes.txt
                val linesLoaded = loadLinesFromGTFS(assetManager)
                Log.d(TAG, "Loaded $linesLoaded lines from GTFS data")
                
                // Initialize the native metro graph
                val success = repository.initializeMetroGraph(assetManager)
                
                if (success) {
                    // Get all station names from the native library
                    val stationNames = repository.getAllStationNames().toMutableList()
                    
                    // Log the first few station names for verification
                    val sampleStations = stationNames.take(10).joinToString(", ")
                    Log.d(TAG, "Sample stations: $sampleStations")
                    
                    // Make sure the list is alphabetically sorted for better browsing
                    stationNames.sort()
                    
                    _uiState.update { 
                        it.copy(
                            stationNames = stationNames,
                            isLoading = false
                        )
                    }
                    
                    Log.d(TAG, "Metro graph successfully initialized with ${stationNames.size} stations")
                    
                    // If we didn't get any station names, that's a problem
                    if (stationNames.isEmpty()) {
                        Log.e(TAG, "No station names were returned from the native library")
                        _uiState.update { 
                            it.copy(
                                errorMessage = "No stations loaded. Please restart the app."
                            )
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to initialize metro graph in the native library")
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Failed to initialize metro graph",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception initializing metro graph", e)
                _uiState.update { 
                    it.copy(
                        errorMessage = "Error: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Load station data from stops.txt
     * @return number of stations loaded
     */
    private fun loadStationsFromGTFS(assetManager: AssetManager): Int {
        try {
            val inputStream = assetManager.open("DMRC_GTFS/stops.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header line
            var line = reader.readLine()
            Log.d(TAG, "Stops header: $line")
            
            var stationCount = 0
            
            // Read and parse each line
            while (reader.readLine()?.also { line = it } != null) {
                val parts = line.split(",")
                if (parts.size >= 6) {
                    try {
                        val id = parts[0].toInt()
                        val code = parts[1]
                        val name = parts[2]
                        val lat = parts[4].toDoubleOrNull() ?: 0.0
                        val lon = parts[5].toDoubleOrNull() ?: 0.0
                        
                        stationMap[id] = MetroStation(id, code, name, lat, lon)
                        stationCount++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing station data: $line", e)
                    }
                }
            }
            
            reader.close()
            return stationCount
        } catch (e: Exception) {
            Log.e(TAG, "Error loading stations from GTFS", e)
            return 0
        }
    }
    
    /**
     * Load line data from routes.txt
     */
    private fun loadLinesFromGTFS(assetManager: AssetManager) {
        try {
            val inputStream = assetManager.open("DMRC_GTFS/routes.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header line
            var line = reader.readLine()
            
            // Read and parse each line
            while (reader.readLine()?.also { line = it } != null) {
                val parts = line.split(",")
                if (parts.size >= 4) {
                    try {
                        val id = parts[0].toInt()
                        val routeName = if (parts.size >= 4) parts[3] else ""
                        
                        // Extract color from route_long_name (like "YELLOW_Huda City Centre to Samaypur Badli")
                        val color = when {
                            routeName.startsWith("RED") -> "E53935"
                            routeName.startsWith("YELLOW") -> "FDD835"
                            routeName.startsWith("BLUE") -> "1E88E5"
                            routeName.startsWith("GREEN") -> "43A047"
                            routeName.startsWith("VIOLET") -> "8E24AA"
                            routeName.startsWith("PINK") -> "EC407A"
                            routeName.startsWith("MAGENTA") -> "9C27B0"
                            routeName.startsWith("GRAY") -> "9E9E9E"
                            routeName.startsWith("AQUA") -> "00BCD4"
                            routeName.startsWith("ORANGE") || routeName.startsWith("AIRPORT") -> "FF9800"
                            routeName.startsWith("RAPID") -> "00BCD4"
                            else -> getDefaultLineColor(id)
                        }
                        
                        // Extract line name from the route name (take part before underscore)
                        val lineName = when {
                            routeName.contains("_") -> routeName.substringBefore("_") + " Line"
                            routeName.isNotEmpty() -> routeName + " Line"
                            else -> "Delhi Metro Line $id"
                        }
                        
                        lineMap[id] = MetroLine(id, lineName, color)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing line data: $line", e)
                    }
                }
            }
            
            reader.close()
            Log.d(TAG, "Loaded ${lineMap.size} lines from GTFS data")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading lines from GTFS", e)
        }
    }

    /**
     * Find path between two stations
     */
    fun findPath(sourceStation: String, targetStation: String, isShortestPath: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSearching = true) }
                
                val path = if (isShortestPath) {
                    repository.findShortestPath(sourceStation, targetStation)
                } else {
                    repository.findFastestPath(sourceStation, targetStation)
                }
                
                _uiState.update { 
                    it.copy(
                        currentPath = path,
                        isSearching = false,
                        pathErrorMessage = if (path != null && !path.isValid()) "No path found between these stations" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        pathErrorMessage = "Error finding path: ${e.message}",
                        isSearching = false
                    )
                }
                Log.e(TAG, "Error finding path", e)
            }
        }
    }

    /**
     * Update selected source station
     */
    fun updateSourceStation(station: String) {
        _uiState.update { it.copy(sourceStation = station) }
    }

    /**
     * Update selected target station
     */
    fun updateTargetStation(station: String) {
        _uiState.update { it.copy(targetStation = station) }
    }

    /**
     * Update path type selection
     */
    fun updatePathType(isShortestPath: Boolean) {
        _uiState.update { it.copy(showShortestPath = isShortestPath) }
    }
    
    /**
     * Get a MetroStation by its ID
     */
    fun getStationById(id: Int): MetroStation? {
        // First try to get from our loaded data
        val station = stationMap[id]
        if (station != null) {
            return station
        }
        
        // Return a fallback if not found
        return MetroStation(
            id = id,
            code = "ST$id",
            name = "Station $id",
            latitude = 0.0,
            longitude = 0.0
        )
    }
    
    /**
     * Get a MetroLine by its ID
     */
    fun getLineById(id: Int): MetroLine? {
        // First try to get from our loaded data
        val line = lineMap[id]
        if (line != null) {
            return line
        }
        
        // Return a fallback if not found
        return MetroLine(
            id = id,
            name = "Delhi Metro Line $id",
            color = getDefaultLineColor(id)
        )
    }
    
    /**
     * Get a default color for a line based on its ID
     */
    private fun getDefaultLineColor(id: Int): String {
        // Cycle through a set of distinct colors based on ID
        return when (id % 8) {
            0 -> "E53935" // Red
            1 -> "FDD835" // Yellow
            2 -> "1E88E5" // Blue
            3 -> "43A047" // Green
            4 -> "8E24AA" // Violet
            5 -> "EC407A" // Pink
            6 -> "9C27B0" // Magenta
            7 -> "FF9800" // Orange
            else -> "9E9E9E" // Gray
        }
    }
}

/**
 * State class for the Metro UI
 */
data class MetroUiState(
    val stationNames: List<String> = emptyList(),
    val sourceStation: String = "",
    val targetStation: String = "",
    val showShortestPath: Boolean = true,
    val currentPath: MetroPath? = null,
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val pathErrorMessage: String? = null
) 