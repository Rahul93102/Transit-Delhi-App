package com.example.opendelhitransit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.opendelhitransit.data.model.BusLocation
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BusMarker(
    busLocation: BusLocation,
    isSelected: Boolean = false,
    isColorBlindMode: Boolean = false,
    onClick: () -> Unit = {}
) {
    val position = LatLng(busLocation.latitude, busLocation.longitude)
    val markerState = MarkerState(position = position)
    
    // Different colors for color blind mode (converted to hue values)
    val hue = when {
        isSelected && isColorBlindMode -> 240f // Blue
        isSelected && !isColorBlindMode -> 120f // Green
        !isSelected && isColorBlindMode -> 200f // Blue-cyan
        else -> 0f // Red
    }
    
    Marker(
        state = markerState,
        title = "Bus ${busLocation.vehicleId}",
        snippet = "Route: ${busLocation.routeId}",
        onClick = {
            onClick()
            true
        },
        icon = BitmapDescriptorFactory.defaultMarker(hue)
    )
}

@Composable
fun BusInfoContent(
    busLocation: BusLocation,
    isColorBlindMode: Boolean
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val speedText = String.format("%.1f km/h", busLocation.speed)
    
    // Different colors for color blind mode
    val busColor = if (isColorBlindMode) {
        Color(0xFF0000FF) // Blue for color blind mode
    } else {
        Color(0xFF4CAF50) // Green for normal mode
    }
    
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(busColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = "Bus ID: ${busLocation.vehicleId}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Route: ${busLocation.routeId}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = speedText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Updated: ${dateFormat.format(busLocation.lastUpdated)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 