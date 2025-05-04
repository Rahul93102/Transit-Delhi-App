package com.example.opendelhitransit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for DataStore
val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

// Define available themes
enum class AppTheme {
    SYSTEM, // Follow system theme (light/dark)
    LIGHT,  // Light theme
    DARK,   // Dark theme 
    BLUE,   // Blue theme variation
    GREEN,  // Green theme variation
    HIGH_CONTRAST, // High contrast theme
    COLOR_BLIND_DEUTERANOPIA, // Color blind - green deficiency
    COLOR_BLIND_PROTANOPIA,   // Color blind - red deficiency
    COLOR_BLIND_TRITANOPIA    // Color blind - blue deficiency
}

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys for DataStore
    private val themeKey = stringPreferencesKey("selected_theme")
    
    // Get current selected theme
    val themeFlow: Flow<AppTheme> = context.themeDataStore.data.map { preferences ->
        val themeName = preferences[themeKey] ?: AppTheme.SYSTEM.name
        try {
            // Convert string to enum
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            // If theme name not found, default to system
            AppTheme.SYSTEM
        }
    }
    
    // Save theme preference
    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
} 