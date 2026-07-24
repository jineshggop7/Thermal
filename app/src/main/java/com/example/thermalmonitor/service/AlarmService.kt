package com.example.thermalmonitor.service

import android.content.Context
import com.example.thermalmonitor.database.TemperatureDatabase
import com.example.thermalmonitor.model.AlarmSettings
import com.example.thermalmonitor.model.AlarmType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.floor

class AlarmService(private val context: Context) {
    
    private val database = TemperatureDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun setAlarm(alarmType: AlarmType, threshold: Float) {
        scope.launch {
            val alert = AlarmSettings(
                alarmType = alarmType,
                threshold = threshold,
                lastAlertedInteger = -1
            )
            database.temperatureDao().insertAlert(alert)
        }
    }
    
    fun getThreshold(alarmType: AlarmType): Float {
        return 60f 
    }
    
    fun checkAndTriggerAlarm(cpuTemp: Float, batteryTemp: Float) {
        scope.launch {
            val alerts = database.temperatureDao().getEnabledAlertsList()
            alerts.forEach { alert ->
                val currentTemp = when (alert.alarmType) {
                    AlarmType.CPU -> cpuTemp
                    AlarmType.BATTERY -> batteryTemp
                    AlarmType.BOTH -> maxOf(cpuTemp, batteryTemp)
                }

                val currentInt = floor(currentTemp).toInt()

                if (currentTemp >= alert.threshold) {
                    // Trigger alert only if we are moving to a HIGHER whole number
                    if (currentInt > alert.lastAlertedInteger) {
                        triggerAlarm(alert, cpuTemp, batteryTemp)
                    }
                    
                    // Track the current integer level to detect future increases
                    if (currentInt != alert.lastAlertedInteger) {
                        val updatedAlert = alert.copy(lastAlertedInteger = currentInt)
                        database.temperatureDao().updateAlert(updatedAlert)
                    }
                } else {
                    // Temperature dropped below threshold. Reset so it can trigger at the threshold level again.
                    if (alert.lastAlertedInteger != -1) {
                        val updatedAlert = alert.copy(lastAlertedInteger = -1)
                        database.temperatureDao().updateAlert(updatedAlert)
                    }
                }
            }
        }
    }
    
    private fun triggerAlarm(alert: AlarmSettings, cpuTemp: Float, batteryTemp: Float) {
        AlarmNotificationManager.showAlarmNotification(context, alert.alarmType, cpuTemp, batteryTemp)
    }
}
