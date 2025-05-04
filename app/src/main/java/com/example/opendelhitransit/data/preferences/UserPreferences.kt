package com.example.opendelhitransit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Interface for managing user preferences
 */
interface UserPreferences {
    val colorBlindModeEnabled: Flow<Boolean>
    
    suspend fun setColorBlindMode(enabled: Boolean)
}

/**
 * Implementation of UserPreferences interface using DataStore
 */
@Singleton
class UserPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferences {
    
    companion object {
        private val COLOR_BLIND_MODE = booleanPreferencesKey("color_blind_mode")
    }
    
    override val colorBlindModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[COLOR_BLIND_MODE] ?: false
        }
    
    override suspend fun setColorBlindMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_BLIND_MODE] = enabled
        }
    }
} 