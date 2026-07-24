package com.example.thermalmonitor.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TemperatureUtils {
    
    const val TEMP_NOT_AVAILABLE = -999.0f
    
    // Cache the working paths to avoid expensive broad scans every second
    private var cachedCpuPath: String? = null
    
    private fun isDummyValue(temp: Float): Boolean {
        return abs(temp - 25.0f) < 0.1f || 
               abs(temp - 30.0f) < 0.1f || 
               abs(temp - 0.0f) < 0.1f ||
               abs(temp - 40.0f) < 0.1f
    }

    /**
     * Read CPU temperature with path caching and broad scan fallback
     */
    fun readCpuTemperature(): Float {
        // Try the cached path first for efficiency
        cachedCpuPath?.let { path ->
            val temp = readFileTemp(path)
            if (temp != TEMP_NOT_AVAILABLE && !isDummyValue(temp)) {
                return temp
            }
        }

        // Broad scan if cache fails or is empty
        val thermalFiles = mutableListOf<String>()
        for (i in 0..30) {
            thermalFiles.add("/sys/class/thermal/thermal_zone$i/temp")
            thermalFiles.add("/sys/devices/virtual/thermal/thermal_zone$i/temp")
        }
        thermalFiles.addAll(listOf(
            "/sys/class/hwmon/hwmon0/temp1_input",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp"
        ))

        for (path in thermalFiles) {
            val temp = readFileTemp(path)
            if (temp != TEMP_NOT_AVAILABLE && !isDummyValue(temp)) {
                cachedCpuPath = path // Cache the first working non-dummy path
                return temp
            }
        }
        
        return TEMP_NOT_AVAILABLE
    }

    private fun readFileTemp(path: String): Float {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                val content = file.readText().trim()
                val rawTemp = content.toFloatOrNull() ?: return TEMP_NOT_AVAILABLE
                
                // Normalize to Celsius
                when {
                    rawTemp > 100000 -> rawTemp / 100000f
                    rawTemp > 1000 -> rawTemp / 1000f
                    rawTemp > 150 -> rawTemp / 10f
                    else -> rawTemp
                }
            } else TEMP_NOT_AVAILABLE
        } catch (e: Exception) {
            TEMP_NOT_AVAILABLE
        }
    }
    
    fun readBatteryTemperature(context: Context): Float {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (intent != null && intent.hasExtra(BatteryManager.EXTRA_TEMPERATURE)) {
                val batTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                if (batTemp != -1) {
                    val temp = batTemp / 10f
                    return if (isDummyValue(temp)) TEMP_NOT_AVAILABLE else temp
                }
            }
            TEMP_NOT_AVAILABLE
        } catch (e: Exception) {
            TEMP_NOT_AVAILABLE
        }
    }
    
    fun getGradientColor(temperature: Float): Int {
        if (temperature <= -100f) return Color.LTGRAY
        val minTemp = 20f
        val maxTemp = 75f
        val normalizedTemp = max(minTemp, min(maxTemp, temperature))
        val ratio = (normalizedTemp - minTemp) / (maxTemp - minTemp)
        return Color.rgb((ratio * 255).toInt(), ((1 - ratio) * 100).toInt(), ((1 - ratio) * 255).toInt())
    }
    
    fun getTemperatureStatus(temperature: Float): String {
        if (temperature <= -100f) return "Data N/A"
        return when {
            temperature < 35 -> "Cool"
            temperature < 45 -> "Normal"
            temperature < 55 -> "Warm"
            temperature < 65 -> "Hot"
            else -> "Critical"
        }
    }
    
    fun formatTemperature(temperature: Float): String {
        return if (temperature <= -100f) "N/A" else String.format("%.1f°C", temperature)
    }
}
