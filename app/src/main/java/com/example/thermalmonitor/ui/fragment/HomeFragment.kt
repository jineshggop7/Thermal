package com.example.thermalmonitor.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.thermalmonitor.R
import com.example.thermalmonitor.service.AlarmNotificationManager
import com.example.thermalmonitor.utils.TemperatureUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue

class HomeFragment : Fragment() {
    
    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private lateinit var cpuTempTextView: TextView
    private lateinit var batteryTempTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var cpuProgressBar: ProgressBar
    private lateinit var batteryProgressBar: ProgressBar
    private lateinit var showNotificationButton: MaterialButton
    
    private val cpuTempHistory: Queue<Float> = LinkedList()
    private var isStatusNotificationActive = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cpuTempTextView = view.findViewById(R.id.cpu_temp_text)
        batteryTempTextView = view.findViewById(R.id.battery_temp_text)
        statusTextView = view.findViewById(R.id.status_text)
        cpuProgressBar = view.findViewById(R.id.cpu_progress)
        batteryProgressBar = view.findViewById(R.id.battery_progress)
        showNotificationButton = view.findViewById(R.id.show_notification_button)
        
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        isStatusNotificationActive = sharedPrefs.getBoolean("status_notification_active", false)
        updateNotificationButtonState()

        showNotificationButton.setOnClickListener {
            toggleStatusNotification()
        }
        
        startTemperatureUpdates()
    }
    
    private fun startTemperatureUpdates() {
        scope.launch {
            while (true) {
                try {
                    // Perform heavy I/O operations on Dispatchers.IO to prevent UI hanging
                    val (rawCpuTemp, batteryTemp) = withContext(Dispatchers.IO) {
                        val cpu = TemperatureUtils.readCpuTemperature()
                        val battery = context?.let { TemperatureUtils.readBatteryTemperature(it) } 
                                     ?: TemperatureUtils.TEMP_NOT_AVAILABLE
                        Pair(cpu, battery)
                    }
                    
                    // Processing history and UI updates on Dispatchers.Main
                    if (rawCpuTemp != TemperatureUtils.TEMP_NOT_AVAILABLE) {
                        cpuTempHistory.add(rawCpuTemp)
                        if (cpuTempHistory.size > 10) cpuTempHistory.poll()
                    }
                    
                    val avgCpuTemp = if (cpuTempHistory.isEmpty()) {
                        rawCpuTemp
                    } else {
                        cpuTempHistory.average().toFloat()
                    }
                    
                    updateUI(avgCpuTemp, batteryTemp)
                    
                    if (isStatusNotificationActive) {
                        AlarmNotificationManager.updateStatusNotification(
                            requireContext(),
                            TemperatureUtils.formatTemperature(avgCpuTemp),
                            TemperatureUtils.formatTemperature(batteryTemp)
                        )
                    }
                    
                    delay(1000) 
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(2000) // Longer delay on error
                }
            }
        }
    }
    
    private fun updateUI(cpuTemp: Float, batteryTemp: Float) {
        if (!isAdded) return

        val maxTemp = maxOf(cpuTemp, batteryTemp)
        
        cpuTempTextView.text = TemperatureUtils.formatTemperature(cpuTemp)
        batteryTempTextView.text = TemperatureUtils.formatTemperature(batteryTemp)
        
        val status = TemperatureUtils.getTemperatureStatus(maxTemp)
        statusTextView.text = "System Status: $status"
        
        updateProgress(cpuProgressBar, cpuTemp)
        updateProgress(batteryProgressBar, batteryTemp)
        
        val color = when {
            maxTemp == TemperatureUtils.TEMP_NOT_AVAILABLE -> R.color.text_secondary
            maxTemp < 40 -> R.color.temp_normal
            maxTemp < 55 -> R.color.temp_warm
            else -> R.color.temp_hot
        }
        statusTextView.setTextColor(ContextCompat.getColor(requireContext(), color))
        
        updateTextColor(cpuTempTextView, cpuTemp, R.color.temp_cool)
        updateTextColor(batteryTempTextView, batteryTemp, R.color.temp_normal)
    }

    private fun updateProgress(progressBar: ProgressBar, temp: Float) {
        val targetProgress = if (temp == TemperatureUtils.TEMP_NOT_AVAILABLE) 0 
                            else ((temp / 80f) * 100).toInt().coerceIn(0, 100)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBar.setProgress(targetProgress, true)
        } else {
            progressBar.progress = targetProgress
        }
    }

    private fun updateTextColor(textView: TextView, temp: Float, activeColor: Int) {
        context?.let { ctx ->
            if (temp == TemperatureUtils.TEMP_NOT_AVAILABLE) {
                textView.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            } else {
                textView.setTextColor(ContextCompat.getColor(ctx, activeColor))
            }
        }
    }

    private fun toggleStatusNotification() {
        isStatusNotificationActive = !isStatusNotificationActive
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("status_notification_active", isStatusNotificationActive).apply()
        
        if (!isStatusNotificationActive) {
            AlarmNotificationManager.cancelStatusNotification(requireContext())
        }
        updateNotificationButtonState()
    }

    private fun updateNotificationButtonState() {
        // Use consistent color for both states as requested
        showNotificationButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue))
        
        if (isStatusNotificationActive) {
            showNotificationButton.text = "Hide Status"
            showNotificationButton.setIconResource(R.drawable.ic_hide)
        } else {
            showNotificationButton.text = "Show Status"
            showNotificationButton.setIconResource(R.drawable.ic_show)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        serviceJob.cancel()
    }
}
