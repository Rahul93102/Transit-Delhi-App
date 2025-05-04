package com.example.opendelhitransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    val colorBlindMode: StateFlow<Boolean> = userPreferences.colorBlindModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    suspend fun setColorBlindMode(enabled: Boolean) {
        userPreferences.setColorBlindMode(enabled)
    }
} 