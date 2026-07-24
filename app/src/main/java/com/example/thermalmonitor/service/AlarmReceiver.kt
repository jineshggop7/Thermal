package com.example.thermalmonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.thermalmonitor.model.AlarmType

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmTypeName = intent.getStringExtra("alarm_type") ?: return
        val alarmType = try { AlarmType.valueOf(alarmTypeName) } catch (e: Exception) { return }
        val cpuTemp = intent.getFloatExtra("cpu_temp", -999f)
        val batteryTemp = intent.getFloatExtra("battery_temp", -999f)
        
        // Trigger notification with sound
        AlarmNotificationManager.showAlarmNotification(context, alarmType, cpuTemp, batteryTemp)
    }
}
