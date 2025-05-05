package com.example.opendelhitransit.features.metro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.opendelhitransit.viewmodel.MetroViewModel
import kotlin.math.roundToInt
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.example.opendelhitransit.data.model.MetroLine

@Composable
fun MetroMapScreen(viewModel: MetroViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load data if not already loaded
    LaunchedEffect(Unit) {
        if (uiState.stationNames.isEmpty()) {
            viewModel.initializeMetroGraph(context.assets)
        }
    }

    // Get all stations and lines from the ViewModel
    val stations = remember(uiState.stationNames) { viewModel.getAllStations() }

    // Changed: Make lines depend on stationNames as well to ensure it's recalculated when data loads
    val lines = remember(uiState.stationNames) {
        Log.d("MetroMapScreen", "Getting lines after stations are loaded")
        viewModel.getAllLines()
    }

    // Build a map of stationId to lines it belongs to (for interchange detection)
    // KEY FIX: Make stationToLines depend on both stations and lines
    val stationToLines = remember(stations, lines) {
        mutableMapOf<Int, MutableSet<Int>>().apply {
            lines.forEach { line ->
                val stationsForLine = viewModel.getStationsForLine(line.id)
                Log.d("MetroMapScreen", "Line ${line.id} (${line.name}): ${stationsForLine.size} stations")

                stationsForLine.forEach { stationId ->
                    getOrPut(stationId) { mutableSetOf() }.add(line.id)
                }
            }
        }.also {
            // Log stats about the mapping we built
            val totalMappings = it.values.sumOf { set -> set.size }
            val interchanges = it.count { entry -> entry.value.size > 1 }
            Log.d("MetroMapScreen", "Built station-to-line map with $totalMappings mappings, " +
                "${it.size} stations, $interchanges interchanges")
        }
    }

    // Find min/max lat/lon for normalization
    val minLat = stations.minOfOrNull { it.latitude } ?: 0.0
    val maxLat = stations.maxOfOrNull { it.latitude } ?: 1.0
    val minLon = stations.minOfOrNull { it.longitude } ?: 0.0
    val maxLon = stations.maxOfOrNull { it.longitude } ?: 1.0

    // Helper to project lat/lon to screen coordinates
    fun project(lat: Double, lon: Double, width: Float, height: Float): Offset {
        val x = ((lon - minLon) / (maxLon - minLon + 1e-6)) * width
        val y = ((maxLat - lat) / (maxLat - minLat + 1e-6)) * height
        return Offset(x.toFloat(), y.toFloat())
    }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Delhi Metro Map",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 2.5f)
                        offset += pan
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw each line with its color
                lines.forEach { line ->
                    val stationIds = viewModel.getStationsForLine(line.id)
                    val lineStations = stationIds.mapNotNull { id -> viewModel.getStationById(id) }

                    // Get proper color for this line
                    val lineColor = getProperLineColor(line)

                    for (i in 0 until lineStations.size - 1) {
                        val s1 = lineStations[i]
                        val s2 = lineStations[i + 1]
                        val p1 = (project(s1.latitude, s1.longitude, w, h) * scale) + offset
                        val p2 = (project(s2.latitude, s2.longitude, w, h) * scale) + offset
                        drawLine(
                            color = lineColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw stations, highlight interchanges
                stations.forEach { station ->
                    val pos = (project(station.latitude, station.longitude, w, h) * scale) + offset
                    val linesForStation = stationToLines[station.id] ?: emptySet()

                    // Get the actual line objects for this station
                    val lineObjects = linesForStation.mapNotNull { viewModel.getLineById(it) }

                    // A station is an interchange only if it has UNIQUE metro lines running through it
                    // We determine uniqueness by line name rather than ID, as some lines may have multiple IDs
                    val uniqueLineNames = lineObjects.map { it.name.substringBefore(" ") }.toSet()
                    val isInterchange = uniqueLineNames.size > 1

                    val stationColor = if (isInterchange) {
                        // Keep interchanges as red
                        Color.Red
                    } else if (linesForStation.isNotEmpty()) {
                        // For regular stations, use their line color
                        val lineId = linesForStation.first()
                        val line = viewModel.getLineById(lineId)
                        getProperLineColor(line ?: MetroLine(lineId, "Unknown Line"))
                    } else {
                        // Fallback color for stations with no line association
                        Color.White
                    }

                    drawCircle(
                        color = stationColor,
                        radius = if (isInterchange) 16f else 10f,
                        center = pos
                    )

                    // Add white border to make stations more visible against colored lines
                    drawCircle(
                        color = Color.White,
                        radius = if (isInterchange) 16f else 10f,
                        center = pos,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pinch to zoom and drag to pan. Interchange stations are shown in red.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Get a proper color for a line, trying multiple fallback methods
 */
private fun getProperLineColor(line: MetroLine): Color {
    // Method 1: Try parsing the color directly
    if (line.color.isNotEmpty()) {
        try {
            // Make sure the color has a # prefix if it doesn't already
            val colorHex = if (line.color.startsWith("#")) line.color else "#${line.color}"
            return Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Log.e("MetroMapScreen", "Failed to parse color: ${line.color} for line ${line.name}", e)
            // Continue to next method
        }
    }

    // Method 2: Check the line name for color hints
    val colorByName = getLineColorForName(line.name)
    Log.d("MetroViewModel", "Here we are $colorByName")
    if (colorByName != Color.Gray) {
        return colorByName
    }

    // Method 3: Get a color based on line ID
    return when (line.id % 8) {
        0 -> Color(0xFFE53935)  // Red
        1 -> Color(0xFFFDD835)  // Yellow
        2 -> Color(0xFF1E88E5)  // Blue
        3 -> Color(0xFF43A047)  // Green
        4 -> Color(0xFF8E24AA)  // Violet
        5 -> Color(0xFFEC407A)  // Pink
        6 -> Color(0xFF9C27B0)  // Magenta
        7 -> Color(0xFFFF9800)  // Orange
        else -> Color(0xFF9E9E9E)  // Gray
    }
}