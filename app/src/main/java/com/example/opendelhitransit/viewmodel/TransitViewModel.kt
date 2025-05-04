package com.example.opendelhitransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.model.VehicleData
import com.example.opendelhitransit.data.repository.TransitRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransitViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {
    
    // Auto-refresh interval (in milliseconds)
    private val REFRESH_INTERVAL = 30000L // 30 seconds
    private val MAX_RETRY_ATTEMPTS = 3
    private val RETRY_DELAY = 5000L // 5 seconds
    
    private val _vehicles = MutableStateFlow<List<VehicleData>>(emptyList())
    val vehicles: StateFlow<List<VehicleData>> = _vehicles.asStateFlow()
    
    private val _nearbyVehicles = MutableStateFlow<List<VehicleData>>(emptyList())
    val nearbyVehicles: StateFlow<List<VehicleData>> = _nearbyVehicles.asStateFlow()
    
    private val _selectedVehicle = MutableStateFlow<VehicleData?>(null)
    val selectedVehicle: StateFlow<VehicleData?> = _selectedVehicle.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
    
    private val _isAutoRefreshEnabled = MutableStateFlow(false)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()
    
    private var autoRefreshJob: Job? = null
    private var retryCount = 0
    
    fun loadVehicles(retry: Boolean = true) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = repository.getAllVehicles()
                _vehicles.value = result
                retryCount = 0 // Reset retry count on success
                
                // If user location is set, also update nearby vehicles
                _userLocation.value?.let { updateNearbyVehicles(it) }
                
            } catch (e: Exception) {
                _error.value = "Error loading vehicles: ${e.message}"
                
                // Auto-retry logic for transient errors
                if (retry && retryCount < MAX_RETRY_ATTEMPTS) {
                    retryCount++
                    delay(RETRY_DELAY)
                    loadVehicles(true)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setUserLocation(location: LatLng) {
        _userLocation.value = location
        updateNearbyVehicles(location)
    }
    
    private fun updateNearbyVehicles(location: LatLng) {
        viewModelScope.launch {
            try {
                val nearby = repository.getVehiclesNear(location)
                _nearbyVehicles.value = nearby
            } catch (e: Exception) {
                _error.value = "Error finding nearby vehicles: ${e.message}"
                // We don't retry this because it depends on the main vehicles list being loaded
            }
        }
    }
    
    fun selectVehicle(vehicleId: String) {
        _selectedVehicle.value = _vehicles.value.find { it.id == vehicleId }
    }
    
    fun clearSelectedVehicle() {
        _selectedVehicle.value = null
    }
    
    fun toggleAutoRefresh() {
        _isAutoRefreshEnabled.value = !_isAutoRefreshEnabled.value
        
        if (_isAutoRefreshEnabled.value) {
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
    }
    
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadVehicles(retry = false) // Don't auto-retry within the refresh cycle
                delay(REFRESH_INTERVAL)
            }
        }
    }
    
    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun retryLoadVehicles() {
        retryCount = 0 // Reset retry count for manual retry
        loadVehicles()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
} 