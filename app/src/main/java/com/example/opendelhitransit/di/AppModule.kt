package com.example.opendelhitransit.di

import android.content.Context
import com.example.opendelhitransit.data.ThemePreferences
import com.example.opendelhitransit.data.network.GoMapsFuelStationApiService
import com.example.opendelhitransit.data.network.TransitApiService
import com.example.opendelhitransit.data.preferences.UserPreferences
import com.example.opendelhitransit.data.preferences.UserPreferencesImpl
import com.example.opendelhitransit.data.repository.GoMapsFuelStationRepository
import com.example.opendelhitransit.data.repository.MetroRepository
import com.example.opendelhitransit.data.repository.TransitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMetroRepository(@ApplicationContext context: Context): MetroRepository {
        return MetroRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideGoMapsFuelStationApiService(): GoMapsFuelStationApiService {
        return GoMapsFuelStationApiService.create()
    }
    
    @Provides
    @Singleton
    fun provideGoMapsFuelStationRepository(apiService: GoMapsFuelStationApiService): GoMapsFuelStationRepository {
        return GoMapsFuelStationRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideTransitRepository(apiService: TransitApiService): TransitRepository {
        return TransitRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferencesImpl(context)
    }
    
    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }
} 