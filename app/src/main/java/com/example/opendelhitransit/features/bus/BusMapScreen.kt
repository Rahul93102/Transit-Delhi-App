package com.example.opendelhitransit.features.bus

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.opendelhitransit.ui.components.BusMarker
import com.example.opendelhitransit.viewmodel.BusMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@Composable
fun BusMapScreen(
    viewModel: BusMapViewModel = hiltViewModel()
) {
    val busLocations by viewModel.busLocations.collectAsState()
    val selectedBus by viewModel.selectedBus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isColorBlindMode by viewModel.isColorBlindMode.collectAsState()
    val isAutoRefreshEnabled by viewModel.isAutoRefreshEnabled.collectAsState()
    val mapCenterPosition by viewModel.mapCenterPosition.collectAsState()
    val mapZoom by viewModel.mapZoom.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenterPosition, mapZoom)
    }
    
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    // Update camera position when map center or zoom changes in ViewModel
    LaunchedEffect(mapCenterPosition, mapZoom) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapCenterPosition, mapZoom)
        cameraPositionState.animate(cameraUpdate)
    }
    
    // Update ViewModel with camera position changes from user interaction
    DisposableEffect(cameraPositionState) {
        onDispose {
            if (cameraPositionState.isMoving) {
                viewModel.updateMapCenter(cameraPositionState.position.target)
                viewModel.updateMapZoom(cameraPositionState.position.zoom)
            }
        }
    }
    
    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Auto-refresh toggle
                FloatingActionButton(
                    onClick = { viewModel.toggleAutoRefresh() },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = if (isAutoRefreshEnabled) "Disable Auto-refresh" else "Enable Auto-refresh",
                        tint = if (isAutoRefreshEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Manual refresh button
                FloatingActionButton(
                    onClick = { viewModel.refreshBusLocations() },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    maxZoomPreference = 20f,
                    minZoomPreference = 5f
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission
                ),
                onMapClick = { viewModel.selectBus(null) }
            ) {
                busLocations.forEach { busLocation ->
                    BusMarker(
                        busLocation = busLocation,
                        isSelected = selectedBus?.vehicleId == busLocation.vehicleId,
                        isColorBlindMode = isColorBlindMode,
                        onClick = { viewModel.selectBus(busLocation) }
                    )
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Show bus count
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "Buses: ${busLocations.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
} 