package com.example.opendelhitransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.model.BusLocation
import com.example.opendelhitransit.data.preferences.UserPreferences
import com.example.opendelhitransit.data.repository.BusRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusMapViewModel @Inject constructor(
    private val busRepository: BusRepository,
    userPreferences: UserPreferences
) : ViewModel() {
    
    // Auto-refresh interval (in milliseconds)
    private val REFRESH_INTERVAL = 30000L
    
    private val _busLocations = MutableStateFlow<List<BusLocation>>(emptyList())
    val busLocations: StateFlow<List<BusLocation>> = _busLocations.asStateFlow()
    
    private val _selectedBus = MutableStateFlow<BusLocation?>(null)
    val selectedBus: StateFlow<BusLocation?> = _selectedBus.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _mapCenterPosition = MutableStateFlow(LatLng(28.6139, 77.2090)) // Default: Delhi center
    val mapCenterPosition: StateFlow<LatLng> = _mapCenterPosition.asStateFlow()
    
    private val _mapZoom = MutableStateFlow(12f)
    val mapZoom: StateFlow<Float> = _mapZoom.asStateFlow()
    
    // Color blind mode from UserPreferences
    val isColorBlindMode = userPreferences.colorBlindModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    private val _isAutoRefreshEnabled = MutableStateFlow(false)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()
    
    private var autoRefreshJob: Job? = null
    
    init {
        refreshBusLocations()
        
        // Initialize with data from repository
        viewModelScope.launch {
            busRepository.getAllBusLocations().collect { locationsList ->
                _busLocations.value = locationsList
            }
        }
    }
    
    fun refreshBusLocations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                busRepository.refreshBusLocations()
                
            } catch (e: Exception) {
                _error.value = "Error loading bus locations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectBus(bus: BusLocation?) {
        _selectedBus.value = bus
        bus?.let {
            _mapCenterPosition.value = LatLng(it.latitude, it.longitude)
            _mapZoom.value = 15f
        }
    }
    
    fun updateMapCenter(latLng: LatLng) {
        _mapCenterPosition.value = latLng
    }
    
    fun updateMapZoom(zoom: Float) {
        _mapZoom.value = zoom
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
                refreshBusLocations()
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
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
} 