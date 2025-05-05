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

    // Maps to store which stations belong to which lines
    private val lineToStations = mutableMapOf<Int, MutableList<Int>>()

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

                // Try to load trip data to associate stations with lines
                try {
                    loadTripData(assetManager)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load trip data, creating dummy mappings", e)
                    createDummyLineStationMappings()
                }

                // Always verify we have valid line-station mappings
                if (lineToStations.isEmpty()) {
                    Log.w(TAG, "No line-station mappings created, trying backup method")
                    createDummyLineStationMappings()
                }

                // Log station-line mapping statistics
                val totalMappings = lineToStations.values.sumOf { it.size }
                val linetoStationMapping = lineToStations
                Log.d(TAG, "Created $linetoStationMapping")
                Log.d(TAG, "Created $totalMappings station-line mappings across ${lineToStations.size} lines")

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
     * Load trip data to associate stations with lines
     */
    private fun loadTripData(assetManager: AssetManager) {
        try {
            // This is a simplified implementation. In a real app, you'd parse stop_times.txt and trips.txt
            // to build accurate line-to-station mappings.

            try {
                val inputStream = assetManager.open("DMRC_GTFS/stop_times.txt")
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Skip header line
                reader.readLine()

                // Map to store trip -> stops
                val tripStops = mutableMapOf<String, MutableList<Int>>()

                // Read all stop times
                var line: String? = null
                while (reader.readLine()?.also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 4) {
                        try {
                            val tripId = parts[0]
                            val stopId = parts[3].toInt()

                            tripStops.getOrPut(tripId) { mutableListOf() }.add(stopId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing stop times data: $line", e)
                        }
                    }
                }

                // Now associate trips with routes/lines
                val tripsInputStream = assetManager.open("DMRC_GTFS/trips.txt")
                val tripsReader = BufferedReader(InputStreamReader(tripsInputStream))

                // Skip header line
                tripsReader.readLine()

                // Read all trips
                line = null  // Reset the line variable
                while (tripsReader.readLine()?.also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 3) {
                        try {
                            val routeId = parts[0].toInt()
                            val tripId = parts[2]

                            // Get stops for this trip and assign to route
                            val stops = tripStops[tripId] ?: continue

                            // Add these stops to the line
                            lineToStations.getOrPut(routeId) { mutableListOf() }.addAll(stops)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing trip data: $line", e)
                        }
                    }
                }

                // Make sure each line has a unique list of stations
                lineToStations.forEach { (lineId, stations) ->
                    lineToStations[lineId] = stations.distinct().toMutableList()
                }

                Log.d(TAG, "Loaded station associations for ${lineToStations.size} lines")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GTFS trip data", e)
                createDummyLineStationMappings()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading trip data", e)
            createDummyLineStationMappings()
        }
    }

    /**
     * Create dummy line-station mappings for testing
     */
    private fun createDummyLineStationMappings() {
        Log.d(TAG, "Creating dummy line-station mappings")

        // Get all station IDs
        val allStationIds = stationMap.keys.toList()

        if (allStationIds.isEmpty() || lineMap.isEmpty()) {
            Log.e(TAG, "Cannot create dummy mappings - no stations (${allStationIds.size}) or lines (${lineMap.size})")
            return
        }

        // Instead of just assigning random stations to lines, create realistic routes
        // For each line, create a continuous path through stations
        lineMap.keys.forEachIndexed { index, lineId ->
            // Create a more organized distribution of stations based on line ID
            // This ensures lines have distinct geographical paths rather than random stations
            val lineFactor = index.toDouble() / lineMap.size

            // Sort stations based on a geographical pattern (diagonal across the map)
            // This creates the effect of lines running in different directions
            val sortedByPattern = allStationIds.sortedBy { stationId ->
                val station = stationMap[stationId]
                if (station != null) {
                    // Create different sorting patterns for each line
                    when (index % 4) {
                        0 -> station.latitude + station.longitude * lineFactor // Diagonal NW-SE
                        1 -> station.latitude - station.longitude * lineFactor // Diagonal NE-SW
                        2 -> station.latitude * lineFactor                     // Horizontal
                        3 -> station.longitude * lineFactor                    // Vertical
                        else -> station.latitude + station.longitude
                    }
                } else 0.0
            }

            // Take a portion of stations based on line index to ensure different lines
            // have different stations but with some overlap for interchanges
            val startIdx = (allStationIds.size * (index % 3) / 6) % allStationIds.size
            val stationCount = allStationIds.size / (lineMap.size + 1) + 5

            val stationsForLine = sortedByPattern
                .subList(startIdx, minOf(startIdx + stationCount, sortedByPattern.size))
                .toMutableList()

            if (stationsForLine.isNotEmpty()) {
                lineToStations[lineId] = stationsForLine
                Log.d(TAG, "Created dummy line $lineId with ${stationsForLine.size} stations")
            }
        }

        // Ensure each line has at least some stations
        lineMap.keys.forEach { lineId ->
            if (lineToStations[lineId].isNullOrEmpty()) {
                lineToStations[lineId] = allStationIds.take(5).toMutableList()
            }
        }

        // Log summary of the mappings
        Log.d(TAG, "Created mappings for ${lineToStations.size} lines")
        lineToStations.forEach { (lineId, stations) ->
            Log.d(TAG, "Line $lineId: ${stations.size} stations")
        }
    }

    /**
     * Get all metro stations
     */
    fun getAllStations(): List<MetroStation> {
        Log.d(TAG, "All Stations ${stationMap.values.toList()}")
        return stationMap.values.toList()
    }

    /**
     * Get all metro lines
     */
    fun getAllLines(): List<MetroLine> {
        val result = lineMap.values.toList()
        Log.d(TAG, "All Lines (${result.size}): ${result.take(5).joinToString(", ") { it.name }}")
        return result
    }

    /**
     * Get all station IDs that belong to a specific line
     */
    fun getStationsForLine(lineId: Int): List<Int> {
        val stations = lineToStations[lineId]

        // Add more detailed logging to help diagnose the issue
        if (stations.isNullOrEmpty()) {
            Log.w(TAG, "No stations found for line $lineId")
        } else if (lineId % 5 == 0) {  // Log only some lines to avoid spam
            Log.d(TAG, "Line $lineId has ${stations.size} stations: ${stations.take(5)} ...")
        }

        return stations ?: emptyList()
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