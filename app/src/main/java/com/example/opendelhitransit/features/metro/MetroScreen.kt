package com.example.opendelhitransit.features.metro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.opendelhitransit.R
import com.example.opendelhitransit.data.model.MetroPath
import com.example.opendelhitransit.data.model.MetroStation
import com.example.opendelhitransit.viewmodel.MetroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroScreen(viewModel: MetroViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    
    var searchQuery by remember { mutableStateOf("") }
    var sourceStation by remember { mutableStateOf("") }
    var destinationStation by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf<String?>(null) } // "source" or "destination"
    
    val context = LocalContext.current
    
    // Initialize metro data
    LaunchedEffect(Unit) {
        viewModel.initializeMetroGraph(context.assets)
    }
    
    // Show error in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    if (uiState.currentPath != null && !uiState.isLoading && uiState.currentPath!!.isValid()) {
                        // Show journey details in the top bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Delhi Metro Journey", style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_distance),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format("%.1f km", uiState.currentPath!!.distance),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_time),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format("%d min", uiState.currentPath!!.time.toInt()),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_station),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${uiState.currentPath!!.stations.size}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_interchange),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${uiState.currentPath!!.interchangeCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Delhi Metro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selection mode indicator
            if (selectionMode != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select ${if (selectionMode == "source") "source" else "destination"} station",
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { selectionMode = null }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Cancel selection"
                            )
                        }
                    }
                }
            }
            
            // Search Station
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    // In a real implementation, this would filter stations based on searchQuery
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (selectionMode != null) "Search station" else "Search for any station") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Route Planner (only show if not in selection mode)
            if (selectionMode == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Plan Your Journey",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Source Station
                        OutlinedTextField(
                            value = sourceStation,
                            onValueChange = { /* Only updated via selection */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectionMode = "source"
                                    searchQuery = ""
                                },
                            label = { Text("From") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = "From", tint = MaterialTheme.colorScheme.primary) },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Destination Station
                        OutlinedTextField(
                            value = destinationStation,
                            onValueChange = { /* Only updated via selection */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectionMode = "destination"
                                    searchQuery = ""
                                },
                            label = { Text("To") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = "To", tint = MaterialTheme.colorScheme.primary) },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Find Route Button
                        Button(
                            onClick = {
                                if (sourceStation.isNotEmpty() && destinationStation.isNotEmpty()) {
                                    viewModel.findPath(sourceStation, destinationStation, true)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = sourceStation.isNotEmpty() && destinationStation.isNotEmpty()
                        ) {
                            Text("Find Route")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display station list or the route
            if ((selectionMode != null || searchQuery.isNotEmpty()) && !uiState.isLoading) {
                // Determine which stations to show - either filtered by search or all
                val stationsToShow = if (searchQuery.isNotEmpty()) {
                    uiState.stationNames.filter { 
                        it.contains(searchQuery, ignoreCase = true) 
                    }
                } else {
                    uiState.stationNames
                }
                
                if (stationsToShow.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                Text(
                            text = if (searchQuery.isNotEmpty()) "Matching Stations" else "All Stations",
                    fontWeight = FontWeight.Bold,
                        )
                        
                        Text(
                            text = "${stationsToShow.size} stations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Group stations by their first letter
                    val groupedStations = stationsToShow.groupBy { 
                        it.firstOrNull()?.uppercaseChar() ?: '#' 
                    }.toSortedMap()
                
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        // For each letter group
                        groupedStations.forEach { (letter, stations) ->
                            // Add section header
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Add stations in this section
                            items(stations) { stationName ->
                                val isSelected = stationName == sourceStation || stationName == destinationStation
                                
                        StationItem(
                                    stationName = stationName,
                                    isSelected = isSelected,
                                    selectionType = when(stationName) {
                                        sourceStation -> "source"
                                        destinationStation -> "destination"
                                        else -> null
                                    },
                            onClick = {
                                when (selectionMode) {
                                    "source" -> {
                                                sourceStation = stationName
                                        selectionMode = null
                                    }
                                    "destination" -> {
                                                destinationStation = stationName
                                        selectionMode = null
                                    }
                                    else -> {
                                        // Just view station info
                                    }
                                }
                                searchQuery = ""
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
                    }
                } else {
                    Text("No stations found matching '$searchQuery'")
                }
            } else if (uiState.currentPath != null && !uiState.isLoading) {
                // Show route details in a scrollable column
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        RouteDetails(path = uiState.currentPath!!)
                    }
                }
            } else if (uiState.isLoading) {
                // Show loading
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading metro data...")
                }
            } else if (uiState.stationNames.isNotEmpty()) {
                // Show a "Browse Stations" card when neither searching nor showing a path
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectionMode = "browse" },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_station),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Browse All Stations",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${uiState.stationNames.size} stations available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StationItem(
    stationName: String,
    isSelected: Boolean = false,
    selectionType: String? = null,
    onClick: () -> Unit
) {
    // Try to detect lines from the station name
    val stationLines = detectLinesFromStationName(stationName)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = when(selectionType) {
                    "source" -> MaterialTheme.colorScheme.primaryContainer
                    "destination" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station icon with circle background 
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) {
                            when(selectionType) {
                                "source" -> MaterialTheme.colorScheme.primary
                                "destination" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                        text = stationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) {
                            when(selectionType) {
                                "source" -> MaterialTheme.colorScheme.primary
                                "destination" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                Text(
                            text = when(selectionType) {
                                "source" -> "(From)"
                                "destination" -> "(To)"
                                else -> ""
                            },
                    fontSize = 12.sp,
                            color = when(selectionType) {
                                "source" -> MaterialTheme.colorScheme.primary
                                "destination" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                }
                
                if (stationLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Line indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stationLines.forEach { line ->
                            val lineColor = getLineColorForName(line)
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        color = lineColor,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = line,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Try to detect which lines a station is on based on name patterns or known interchange points
 */
private fun detectLinesFromStationName(stationName: String): List<String> {
    val name = stationName.lowercase()
    val lines = mutableListOf<String>()
    
    // Check for common interchange points
    when {
        name.contains("rajiv chowk") || name.contains("kashmere gate") || 
        name.contains("central secretariat") || name.contains("welcome") -> {
            lines.add("Blue")
            lines.add("Yellow")
        }
        name.contains("mandi house") || name.contains("yamuna bank") -> {
            lines.add("Blue")
            lines.add("Violet")
        }
        name.contains("kalkaji") || name.contains("botanical garden") -> {
            lines.add("Magenta")
            lines.add("Blue")
            }
        name.contains("dhaula kuan") || name.contains("new delhi") -> {
            lines.add("Orange")
            lines.add("Yellow")
        }
        name.contains("inderlok") || name.contains("netaji subhash place") -> {
            lines.add("Red")
            lines.add("Pink")
        }
        name.contains("ashok park") || name.contains("kirti nagar") -> {
            lines.add("Green")
            lines.add("Blue")
        }
    }
    
    // Check for keywords in station names that suggest lines
    if (lines.isEmpty()) {
        if (name.contains("vishwavidyalaya") || name.contains("samaypur") || 
            name.contains("hauz khas") || name.contains("huda city")) {
            lines.add("Yellow")
        } else if (name.contains("vaishali") || name.contains("dwarka") || 
                  name.contains("rajouri") || name.contains("noida")) {
            lines.add("Blue")
        } else if (name.contains("dilshad") || name.contains("rithala") ||
                  name.contains("shaheed sthal")) {
            lines.add("Red")
        } else if (name.contains("badarpur") || name.contains("escorts") ||
                  name.contains("ip extension")) {
            lines.add("Violet")
        } else if (name.contains("airport") || name.contains("aerocity") ||
                  name.contains("dhaula kuan")) {
            lines.add("Orange")
        }
    }
    
    // Return unique lines
    return lines.distinct()
}

@Composable
fun RouteDetails(path: MetroPath) {
    val viewModel: MetroViewModel = hiltViewModel()
    
    if (!path.isValid()) {
        // If path is not valid, show an error message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_metro),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Route Found",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "We couldn't find a path between these stations. Please try different stations.",
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Now directly using station and line names from the path
    val stationNames = path.stations
    val lineNames = path.lines
    val interchangeStations = path.interchanges.toSet()
    
    val sourceStationName = stationNames.firstOrNull() ?: "Unknown"
    val targetStationName = stationNames.lastOrNull() ?: "Unknown"
    
    // Format distance, time, and fare for better display
    val distanceFormatted = String.format("%.1f km", path.distance)
    val timeFormatted = String.format("%d min", path.time.toInt())
    val fareEstimate = getFareEstimate(path.distance)
    val fareFormatted = String.format("₹%.0f", fareEstimate)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with metro icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_metro),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Journey Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Divider
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Source and destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(sourceStationName, style = MaterialTheme.typography.bodyLarge)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("To:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(targetStationName, style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Route stats with improved formatting
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_station),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${stationNames.size}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Stations", fontSize = 12.sp)
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_interchange),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${path.interchangeCount}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Interchanges", fontSize = 12.sp)
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_time),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timeFormatted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Travel Time", fontSize = 12.sp)
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_fare),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = fareFormatted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("Est. Fare", fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    
                    // Distance information in another row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                            painter = painterResource(id = R.drawable.ic_distance),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                                        )
                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                            text = "Total Distance: $distanceFormatted",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Station list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                        text = "ROUTE DETAILS",
                            fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (stationNames.isNotEmpty()) {
                        // Display the full station list with proper formatting
                        LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                                .heightIn(max = 350.dp)
                    ) {
                            itemsIndexed(stationNames) { index, stationName ->
                                // Check if this is an interchange station
                                val isInterchange = interchangeStations.contains(stationName)
                                val isFirstOrLast = index == 0 || index == stationNames.size - 1
                                
                                // Get line for this station
                                val lineName = if (index < lineNames.size) lineNames[index] else ""
                                val lineColor = getLineColorForName(lineName)
                                
                                // Station item with enhanced styling
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                    // Station bullet with line color
                                Box(
                                    modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (isInterchange) MaterialTheme.colorScheme.primary 
                                                else if (isFirstOrLast) MaterialTheme.colorScheme.tertiary
                                                else lineColor, 
                                                CircleShape
                                            )
                                            .border(
                                                width = if (isInterchange || isFirstOrLast) 2.dp else 1.dp,
                                                color = if (isInterchange) MaterialTheme.colorScheme.onPrimary
                                                       else if (isFirstOrLast) MaterialTheme.colorScheme.onTertiary
                                                       else Color.Black,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isInterchange) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_interchange),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else if (index == 0) {
                                            Text("S", color = MaterialTheme.colorScheme.onTertiary)
                                        } else if (index == stationNames.size - 1) {
                                            Text("D", color = MaterialTheme.colorScheme.onTertiary)
                                        }
                                }
                                
                                    Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                            text = stationName,
                                            fontWeight = if (isInterchange || isFirstOrLast) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 16.sp,
                                            color = if (isInterchange) MaterialTheme.colorScheme.primary
                                                   else if (isFirstOrLast) MaterialTheme.colorScheme.tertiary
                                                   else MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        if (lineName.isNotEmpty()) {
                                    Text(
                                                text = lineName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                        
                                        // Show interchange information
                                        if (isInterchange && index < stationNames.size - 1) {
                                            val nextIndex = index + 1
                                            val nextLineName = if (nextIndex < lineNames.size) lineNames[nextIndex] else ""
                                            
                                            if (nextLineName != lineName && nextLineName.isNotEmpty()) {
                                        Text(
                                                    text = "Change to $nextLineName",
                                            fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                                
                                // Line connector (don't show after the last station)
                                if (index < stationNames.size - 1) {
                                    // Get the color for the next part of the journey
                                    val nextLineName = if (index + 1 < lineNames.size) lineNames[index + 1] else lineName
                                    val nextLineColor = getLineColorForName(nextLineName)
                                    
                                    // Use current station's color up to interchange, next station's color after
                                    val lineConnectorColor = if (isInterchange) nextLineColor else lineColor
                                    
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                            .height(20.dp)
                                            .width(2.dp)
                                            .background(lineConnectorColor)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No stations data available")
                    }
                }
            }
        }
    }
}

/**
 * Get estimated fare based on distance
 */
fun getFareEstimate(distance: Double): Double {
    return when {
        distance <= 2.0 -> 10.0
        distance <= 5.0 -> 20.0
        distance <= 12.0 -> 30.0
        distance <= 21.0 -> 40.0
        distance <= 32.0 -> 50.0
        else -> 60.0
    }
}

/**
 * Get a color for a line based on its name
 */
fun getLineColorForName(lineName: String): Color {
    val lineNameLower = lineName.lowercase()
    return when {
        lineNameLower.contains("yellow") -> Color(0xFFFFD700)
        lineNameLower.contains("blue") -> Color(0xFF0000FF)
        lineNameLower.contains("red") -> Color(0xFFFF0000)
        lineNameLower.contains("green") -> Color(0xFF008000)
        lineNameLower.contains("violet") -> Color(0xFF8A2BE2) 
        lineNameLower.contains("orange") -> Color(0xFFFF8C00)
        lineNameLower.contains("magenta") -> Color(0xFFFF00FF)
        lineNameLower.contains("pink") -> Color(0xFFFFC0CB)
        lineNameLower.contains("aqua") -> Color(0xFF00FFFF)
        lineNameLower.contains("grey") || lineNameLower.contains("gray") -> Color(0xFF808080)
        lineNameLower.contains("rapid") -> Color(0xFF4682B4)
        else -> Color.Gray
    }
} 