package com.example.opendelhitransit.di

import android.content.Context
import com.example.opendelhitransit.data.local.AppDatabase
import com.example.opendelhitransit.data.local.BusLocationDao
import com.example.opendelhitransit.data.network.TransitApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideBusLocationDao(database: AppDatabase): BusLocationDao {
        return database.busLocationDao()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTransitApiService(okHttpClient: OkHttpClient): TransitApiService {
        return Retrofit.Builder()
            .baseUrl(TransitApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TransitApiService::class.java)
    }
} 