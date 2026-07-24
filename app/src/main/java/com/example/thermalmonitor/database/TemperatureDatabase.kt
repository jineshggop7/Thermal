package com.example.thermalmonitor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thermalmonitor.model.AlarmSettings
import com.example.thermalmonitor.model.TemperatureData

@Database(entities = [TemperatureData::class, AlarmSettings::class], version = 3, exportSchema = false)
abstract class TemperatureDatabase : RoomDatabase() {
    
    abstract fun temperatureDao(): TemperatureDao
    
    companion object {
        @Volatile
        private var Instance: TemperatureDatabase? = null
        
        fun getDatabase(context: Context): TemperatureDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TemperatureDatabase::class.java,
                    "temperature_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
