package com.example.opendelhitransit.data.model

/**
 * Represents a metro line in the Delhi Metro network
 */
data class MetroLine(
    val id: Int,
    val name: String,
    val color: String = "" // Added color field with default value
)

/**
 * Represents a metro station in the Delhi Metro network
 */
data class MetroStation(
    val id: Int,
    val name: String,
    val code: String = "", // Added code field with default value
    val latitude: Double = 0.0, // Added latitude with default value
    val longitude: Double = 0.0 // Added longitude with default value
)

/**
 * Represents a path between two metro stations
 */
data class MetroPath(
    val stations: List<String> = listOf(),
    val lines: List<String> = listOf(),
    val interchanges: List<String> = listOf(),
    val distance: Double = 0.0,
    val time: Double = 0.0,
    val interchangeCount: Int = 0
) {

    fun isValid(): Boolean = stations.size >= 2
} 