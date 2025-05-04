package com.example.opendelhitransit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.opendelhitransit.data.model.BusLocation

@Database(
    entities = [BusLocation::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun busLocationDao(): BusLocationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "open_delhi_transit_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 