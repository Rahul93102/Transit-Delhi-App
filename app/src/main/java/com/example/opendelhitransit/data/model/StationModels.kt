package com.example.opendelhitransit.data.model

import com.google.gson.annotations.SerializedName

// Basic Station model used in the app
data class Station(
    val name: String,
    val line: String,
    val index: Int
) {
    // Convert to MetroStation model
    fun toMetroStation(): MetroStation {
        return MetroStation(
            id = index,
            code = "$line$index",
            name = name,
            latitude = 0.0, // Default value since old model doesn't have coordinates
            longitude = 0.0
        )
    }
}

// Use the MetroLine from MetroModels.kt
// Route model for path finding
data class Route(
    val source: Station,
    val destination: Station,
    val path: List<Station>,
    val distance: Double = 0.0, // in km
    val time: Int = 0, // in minutes
    val fare: Double = 0.0, // in rupees
    val interchanges: Int = 0
) {
    // Convert to MetroPath
    fun toMetroPath(): MetroPath {
        val stationNames = path.map { it.name }
        val lineNames = path.map { it.line }
        // Count interchanges - line changes between consecutive stations
        val interchangeStations = path.mapIndexedNotNull { index, station ->
            if (index > 0 && station.line != path[index - 1].line) station.name else null
        }
        
        return MetroPath(
            stations = stationNames,
            lines = lineNames,
            interchanges = interchangeStations,
            distance = distance,
            time = time.toDouble(),
            interchangeCount = interchanges
        )
    }
}

// Models for JSON parsing

// Station Entity from station_entity.json
data class StationEntity(
    val value: String,
    val synonyms: List<String>
)

// Station from line JSON files (yellow.json, blue.json, etc.)
data class LineStation(
    val English: String?,
    val Hindi: String?,
    val Phase: String?,
    val Opening: String?,
    @SerializedName("Interchange\nConnection") val interchange: String?,
    @SerializedName("Station Layout") val stationLayout: String?,
    @SerializedName("Depot Connection") val depotConnection: String?,
    @SerializedName("Depot Layout") val depotLayout: String?
) {
    // Fallback name resolution
    fun getStationName(): String {
        // Try Hindi as primary name, fall back to English name
        // For the Hindi name, strip numeric prefixes that might be in the data
        val hindiBestName = Hindi?.replace(Regex("^\\d+\\s*"), "")?.trim()
        return when {
            !hindiBestName.isNullOrBlank() -> hindiBestName
            !English.isNullOrBlank() && !English.matches(Regex("^\\d+$")) -> English.trim()
            !Phase.isNullOrBlank() -> Phase.trim()
            else -> "Unknown Station"
        }
    }
    
    // Check if this is an interchange station
    fun isInterchange(): Boolean {
        return !interchange.isNullOrBlank() && 
               !interchange.equals("None", ignoreCase = true) &&
               !interchange.equals("No", ignoreCase = true)
    }
    
    // Convert to normalized Station with provided line name and index
    fun toStation(lineName: String, index: Int): Station {
        return Station(
            name = getStationName().trim(),
            line = lineName.trim(),
            index = index
        )
    }
}

// Station from JSON file format
data class JsonStation(
    val name: String?,
    val index: Int?,
    val line: String?
) {
    // Convert to normalized Station
    fun toStation(): Station? {
        return if (name != null && line != null && index != null) {
            Station(
                name = name.trim(),
                line = line.trim(),
                index = index
            )
        } else {
            null
        }
    }
}

// Graph node for Dijkstra's algorithm
data class GraphNode(
    val station: Station,
    var distance: Int = Int.MAX_VALUE,  // Using index difference as distance
    var previousNode: GraphNode? = null,
    var visited: Boolean = false
) {
    // For priority queue comparison
    fun compareTo(other: GraphNode): Int {
        return distance.compareTo(other.distance)
    }
} 