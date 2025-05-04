package com.example.opendelhitransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opendelhitransit.data.AppTheme
import com.example.opendelhitransit.data.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {
    
    // Expose the theme as StateFlow for Compose
    val currentTheme: StateFlow<AppTheme> = themePreferences.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )
    
    // Update the theme
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themePreferences.setTheme(theme)
        }
    }
    
    // Get a user-friendly name for display
    fun getThemeName(theme: AppTheme): String {
        return when (theme) {
            AppTheme.SYSTEM -> "System Default"
            AppTheme.LIGHT -> "Light"
            AppTheme.DARK -> "Dark"
            AppTheme.BLUE -> "Blue"
            AppTheme.GREEN -> "Green"
            AppTheme.HIGH_CONTRAST -> "High Contrast"
            AppTheme.COLOR_BLIND_DEUTERANOPIA -> "Color Blind (Green Deficiency)"
            AppTheme.COLOR_BLIND_PROTANOPIA -> "Color Blind (Red Deficiency)"
            AppTheme.COLOR_BLIND_TRITANOPIA -> "Color Blind (Blue Deficiency)"
        }
    }
    
    // Get a description for the theme
    fun getThemeDescription(theme: AppTheme): String {
        return when (theme) {
            AppTheme.SYSTEM -> "Follows your device system settings"
            AppTheme.LIGHT -> "Light theme for daytime use"
            AppTheme.DARK -> "Dark theme for nighttime use and battery saving"
            AppTheme.BLUE -> "Blue-focused theme"
            AppTheme.GREEN -> "Green-focused theme"
            AppTheme.HIGH_CONTRAST -> "High contrast for better visibility"
            AppTheme.COLOR_BLIND_DEUTERANOPIA -> "Optimized for green-blindness (deuteranopia)"
            AppTheme.COLOR_BLIND_PROTANOPIA -> "Optimized for red-blindness (protanopia)"
            AppTheme.COLOR_BLIND_TRITANOPIA -> "Optimized for blue-blindness (tritanopia)"
        }
    }
} 