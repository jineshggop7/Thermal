package com.example.thermalmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temperature_data")
data class TemperatureData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cpuTemperature: Float,
    val batteryTemperature: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlarmSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val alarmType: AlarmType,
    val threshold: Float,
    val lastAlertedInteger: Int = -1,
    val isEnabled: Boolean = true
)

enum class AlarmType {
    CPU, BATTERY, BOTH
}
