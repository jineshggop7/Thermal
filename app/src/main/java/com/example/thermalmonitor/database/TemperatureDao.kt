package com.example.thermalmonitor.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.thermalmonitor.model.AlarmSettings
import com.example.thermalmonitor.model.TemperatureData
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureDao {
    
    // Temperature Data
    @Insert
    suspend fun insertTemperature(temperature: TemperatureData)
    
    @Query("SELECT * FROM temperature_data ORDER BY timestamp DESC")
    fun getAllTemperatures(): Flow<List<TemperatureData>>
    
    @Query("SELECT * FROM temperature_data WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTemperaturesAfter(startTime: Long): Flow<List<TemperatureData>>
    
    // Alerts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlarmSettings)

    @Update
    suspend fun updateAlert(alert: AlarmSettings)
    
    @Query("SELECT * FROM alerts WHERE isEnabled = 1")
    fun getAllEnabledAlerts(): Flow<List<AlarmSettings>>

    @Query("SELECT * FROM alerts")
    fun getAllAlerts(): Flow<List<AlarmSettings>>

    @Query("SELECT * FROM alerts WHERE isEnabled = 1")
    suspend fun getEnabledAlertsList(): List<AlarmSettings>
    
    @Delete
    suspend fun deleteAlert(alert: AlarmSettings)

    @Query("DELETE FROM alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: Int)
}
