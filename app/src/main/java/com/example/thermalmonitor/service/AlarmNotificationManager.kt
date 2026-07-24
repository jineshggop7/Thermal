package com.example.thermalmonitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.thermalmonitor.MainActivity
import com.example.thermalmonitor.R
import com.example.thermalmonitor.model.AlarmType
import com.example.thermalmonitor.utils.TemperatureUtils

object AlarmNotificationManager {
    
    private const val CHANNEL_ID_ALERT = "temperature_alert_channel"
    private const val CHANNEL_ID_STATUS = "temperature_status_channel"
    private const val NOTIFICATION_ID_ALERT = 1
    private const val NOTIFICATION_ID_STATUS = 2
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel for Alerts (with Sound)
            if (notificationManager.getNotificationChannel(CHANNEL_ID_ALERT) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_ALERT,
                    "Thermal Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Beep alerts for high temperature thresholds"
                    enableVibration(true)
                    
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Channel for Status (Silent/Low priority)
            if (notificationManager.getNotificationChannel(CHANNEL_ID_STATUS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_STATUS,
                    "Temperature Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Persistent temperature display in notification bar"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    fun showAlarmNotification(
        context: Context,
        alarmType: AlarmType,
        cpuTemp: Float,
        batteryTemp: Float
    ) {
        createNotificationChannels(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("show_alarm", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val cpuStr = TemperatureUtils.formatTemperature(cpuTemp)
        val batteryStr = TemperatureUtils.formatTemperature(batteryTemp)

        val contentText = when (alarmType) {
            AlarmType.CPU -> "CPU reached $cpuStr"
            AlarmType.BATTERY -> "Battery reached $batteryStr"
            AlarmType.BOTH -> "Threshold exceeded (CPU: $cpuStr | Battery: $batteryStr)"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERT)
            .setContentTitle("Thermal Threshold Crossed")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_thermometer)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    fun updateStatusNotification(context: Context, cpuTemp: String, batteryTemp: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setContentTitle("Thermal Status")
            .setContentText("CPU: $cpuTemp | Battery: $batteryTemp")
            .setSmallIcon(R.drawable.ic_thermometer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_STATUS, notification)
    }

    fun cancelStatusNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_STATUS)
    }
}
